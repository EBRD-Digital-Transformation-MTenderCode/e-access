package com.procurement.access.service

import com.procurement.access.application.model.organization.GetOrganization
import com.procurement.access.application.model.responder.check.structure.CheckPersonesStructure
import com.procurement.access.application.model.responder.processing.ResponderProcessing
import com.procurement.access.application.model.responder.verify.VerifyRequirementResponse
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.application.service.requirement.ValidateRequirementResponsesParams
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.BadRequestErrors
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.enums.BusinessFunctionDocumentType
import com.procurement.access.domain.model.enums.BusinessFunctionType
import com.procurement.access.domain.model.enums.CriteriaRelatesToEnum
import com.procurement.access.domain.model.enums.CriteriaSource
import com.procurement.access.domain.model.enums.LocationOfPersonsType
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.Result.Companion.failure
import com.procurement.access.domain.util.Result.Companion.success
import com.procurement.access.domain.util.ValidationResult
import com.procurement.access.domain.util.asSuccess
import com.procurement.access.domain.util.extension.getDuplicate
import com.procurement.access.domain.util.extension.toSetBy
import com.procurement.access.infrastructure.dto.cn.criteria.Requirement
import com.procurement.access.infrastructure.dto.converter.get.organization.convert
import com.procurement.access.infrastructure.dto.converter.toReference
import com.procurement.access.infrastructure.dto.converter.validate.convert
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.entity.FEEntity
import com.procurement.access.infrastructure.handler.get.organization.GetOrganizationResult
import com.procurement.access.infrastructure.handler.processing.responder.ResponderProcessingResult
import com.procurement.access.infrastructure.handler.validate.ValidateRequirementResponsesResult
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toDate
import com.procurement.access.utils.toJson
import com.procurement.access.utils.tryToObject
import org.springframework.stereotype.Service

interface ResponderService {
    fun responderProcessing(params: ResponderProcessing.Params): Result<ResponderProcessingResult, Fail>
    fun checkPersonesStructure(params: CheckPersonesStructure.Params): ValidationResult<Fail.Error>
    fun verifyRequirementResponse(params: VerifyRequirementResponse.Params): ValidationResult<Fail>
    fun validateRequirementResponses(params: ValidateRequirementResponsesParams): Result<ValidateRequirementResponsesResult, Fail>
    fun getOrganization(params: GetOrganization.Params): Result<GetOrganizationResult, Fail>
}

@Service
class ResponderServiceImpl(
    private val tenderProcessRepository: TenderProcessRepository
) : ResponderService {

    override fun responderProcessing(params: ResponderProcessing.Params): Result<ResponderProcessingResult, Fail> {
        val stage = params.ocid.stage

        val entity = getTenderProcessEntityByCpIdAndStage(cpid = params.cpid, stage = stage)
            .orForwardFail { error -> return error }

        val updatedTenderJson = when (stage) {
            Stage.FE -> {
                val fe = entity.jsonData
                    .tryToObject(FEEntity::class.java)
                    .doReturn { error -> return failure(Fail.Incident.DatabaseIncident(exception = error.exception)) }

                val responder = params.responder
                val dbPersons = fe.tender.procuringEntity?.persons.orEmpty()

                /**
                 * BR-1.0.1.15.3
                 * BR-1.0.1.15.4
                 * BR-1.0.1.5.3
                 */
                val updatedPersons = updateStrategy(
                    receivedElements = listOf(responder),
                    keyExtractorForReceivedElement = responderPersonKeyExtractor,
                    availableElements = dbPersons,
                    keyExtractorForAvailableElement = dbFEPersonKeyExtractor,
                    updateBlock = FEEntity.Tender.ProcuringEntity.Person::update,
                    createBlock = ::createFEPerson
                )

                val updatedFe = fe.copy(
                    tender = fe.tender.copy(
                        procuringEntity = fe.tender.procuringEntity!!.copy(persons = updatedPersons)
                    )
                )

                success(toJson(updatedFe))
            }

            Stage.EV,
            Stage.NP,
            Stage.TP -> {
                val cn = entity.jsonData
                    .tryToObject(CNEntity::class.java)
                    .doReturn { error -> return failure(Fail.Incident.DatabaseIncident(exception = error.exception)) }

                val responder = params.responder
                val dbPersons = cn.tender.procuringEntity.persones

                /**
                 * BR-1.0.1.15.3
                 * BR-1.0.1.15.4
                 * BR-1.0.1.5.3
                 */
                val updatedPersons = updateStrategy(
                    receivedElements = listOf(responder),
                    keyExtractorForReceivedElement = responderPersonKeyExtractor,
                    availableElements = dbPersons.orEmpty(),
                    keyExtractorForAvailableElement = dbCNPersonKeyExtractor,
                    updateBlock = CNEntity.Tender.ProcuringEntity.Persone::update,
                    createBlock = ::createPerson
                )

                val updatedCn = cn.copy(
                    tender = cn.tender.copy(
                        procuringEntity = cn.tender.procuringEntity.copy(
                            persones = updatedPersons
                        )
                    )
                )

                success(toJson(updatedCn))
            }

            Stage.AC,
            Stage.AP,
            Stage.EI,
            Stage.FS,
            Stage.PN ->
                failure(
                    ValidationErrors.UnexpectedStageForResponderProcessing(stage = params.ocid.stage)
                )
        }

        tenderProcessRepository.save(
            TenderProcessEntity(
                cpId = params.cpid.toString(),
                token = entity.token,
                stage = stage.toString(),
                owner = entity.owner,
                createdDate = params.date.toDate(),
                jsonData = toJson(updatedTenderJson)
            )
        )
            .doOnError { error ->
                return failure(Fail.Incident.DatabaseIncident(exception = error.exception))
            }

        // FR-10.1.4.12
        return success(params.responder.toReference())
    }

    override fun checkPersonesStructure(params: CheckPersonesStructure.Params): ValidationResult<Fail.Error> {

        val validDocumentTypes = getValidDocumentTypesForPersons(params)
        val validBusinessFunctions = getValidBusinessFunctionTypesForPersons(params)

        params.persones
            .asSequence()
            .flatMap { it.businessFunctions.asSequence() }
            .also { businessFunctions ->
                businessFunctions.forEach { businessFunction ->
                    if (businessFunction.type !in validBusinessFunctions)
                        return ValidationResult.error(
                            ValidationErrors.InvalidBusinessFunctionType(
                                id = businessFunction.id,
                                allowedValues = validBusinessFunctions.map { it.toString() }
                            )
                        )
                }
            }
            .flatMap { it.documents.asSequence() }
            .also { documents ->
                documents.forEach { document ->
                    if (document.documentType !in validDocumentTypes)
                        return ValidationResult.error(
                            ValidationErrors.InvalidDocumentType(
                                id = document.id,
                                allowedValues = validDocumentTypes.map { it.toString() }
                            )
                        )
                }
            }

        return ValidationResult.ok()
    }

    override fun verifyRequirementResponse(params: VerifyRequirementResponse.Params): ValidationResult<Fail> {

        val cnEntity = tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage)
            .doReturn { error ->
                return ValidationResult.error(Fail.Incident.DatabaseIncident(exception = error.exception))
            }
            ?: return ValidationResult.error(
                ValidationErrors.RequirementsNotFoundOnVerifyRequirementResponse(cpid = params.cpid, ocid = params.ocid)
            )

        val cn = cnEntity.jsonData
            .tryToObject(CNEntity::class.java)
            .doReturn { error ->
                return ValidationResult.error(Fail.Incident.DatabaseIncident(exception = error.exception))
            }

        val requirementIdFromRequest = params.requirementId.toString()
        val foundedRequirement = cn.tender.criteria
            ?.asSequence()
            ?.flatMap { it.requirementGroups.asSequence() }
            ?.flatMap { it.requirements.asSequence() }
            ?.find { it.id == requirementIdFromRequest }
            ?: return ValidationResult.error(
                ValidationErrors.RequirementNotFoundOnVerifyRequirementResponse(cpid = params.cpid, ocid = params.ocid)
            )

        if (params.value.dataType != foundedRequirement.dataType)
            return ValidationResult.error(
                ValidationErrors.RequirementDataTypeMismatchOnValidateRequirementResponse(
                    id = params.requirementResponseId,
                    received = params.value.dataType,
                    available = foundedRequirement.dataType
                )
            )

        val requirementsToProcuringEntity = cn.tender.criteria
            .asSequence()
            .filter { it.source == CriteriaSource.PROCURING_ENTITY }
            .flatMap { it.requirementGroups.asSequence() }
            .flatMap { it.requirements.asSequence() }
            .filter { it.id == foundedRequirement.id }
            .toList()

        if (requirementsToProcuringEntity.isEmpty())
            return ValidationResult.error(
                ValidationErrors.InvalidCriteriaSourceOnVerifyRequirementResponse(foundedRequirement)
            )

        return ValidationResult.ok()
    }

    override fun validateRequirementResponses(params: ValidateRequirementResponsesParams): Result<ValidateRequirementResponsesResult, Fail> {

        val tenderProcessEntity = tenderProcessRepository
            .getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage)
            .doReturn { error ->
                return failure(Fail.Incident.DatabaseIncident(exception = error.exception))
            }
            ?: return success(ValidateRequirementResponsesResult(emptyList()))

        val filteredRequirement = when (params.operationType) {
            OperationType.CREATE_SUBMISSION -> getRequirementToTenderer(tenderProcessEntity, params.ocid.stage)

            OperationType.AMEND_FE,
            OperationType.APPLY_QUALIFICATION_PROTOCOL,
            OperationType.COMPLETE_QUALIFICATION,
            OperationType.CREATE_CN,
            OperationType.CREATE_CN_ON_PIN,
            OperationType.CREATE_CN_ON_PN,
            OperationType.CREATE_FE,
            OperationType.CREATE_NEGOTIATION_CN_ON_PN,
            OperationType.CREATE_PCR,
            OperationType.CREATE_PIN,
            OperationType.CREATE_PIN_ON_PN,
            OperationType.CREATE_PN,
            OperationType.OUTSOURCING_PN,
            OperationType.QUALIFICATION,
            OperationType.QUALIFICATION_CONSIDERATION,
            OperationType.QUALIFICATION_PROTOCOL,
            OperationType.RELATION_AP,
            OperationType.START_SECONDSTAGE,
            OperationType.SUBMISSION_PERIOD_END,
            OperationType.TENDER_PERIOD_END,
            OperationType.UPDATE_AP,
            OperationType.UPDATE_CN,
            OperationType.UPDATE_PN,
            OperationType.WITHDRAW_QUALIFICATION_PROTOCOL -> getAllRequirement(tenderProcessEntity, params.ocid.stage)
        }
            .orForwardFail { fail -> return fail }

        val organizationIdsSet = params.organizationIds.toSet()

        // VR.COM-1.10.1
        val requirementResponsesForTenderer = params.requirementResponses
            .filter { it.requirement.id.toString() in filteredRequirement }

        requirementResponsesForTenderer.forEach { requirementResponseRq ->
            val id = requirementResponseRq.requirement.id.toString()
            val foundedRequirement = filteredRequirement.getValue(id)

            // VR.COM-1.10.3
            if (requirementResponseRq.value.dataType != foundedRequirement.dataType)
                return failure(
                    ValidationErrors.RequirementDataTypeMismatchOnValidateRequirementResponses(
                        id = requirementResponseRq.id,
                        received = requirementResponseRq.value.dataType,
                        available = foundedRequirement.dataType
                    )
                )

            // VR.COM-1.10.4
            if (requirementResponseRq.relatedCandidate.id !in organizationIdsSet)
                return failure(
                    ValidationErrors.OrganizationIdNotPassedOnValidateRequirementResponses(
                        candidateId = requirementResponseRq.relatedCandidate.id,
                        requirementResponseId = requirementResponseRq.id
                    )
                )

        }

        // VR.COM-1.10.5
        validateOneAnswerOnRequirementByCandidate(requirementResponsesForTenderer)
            .orForwardFail { error -> return error }

        return success(requirementResponsesForTenderer.convert())
    }

    override fun getOrganization(params: GetOrganization.Params): Result<GetOrganizationResult, Fail> {
        val stage = params.ocid.stage

        val entity = tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = stage)
            .orForwardFail { error -> return error }
            ?: return failure(
                ValidationErrors.TenderNotFoundOnGetOrganization(cpid = params.cpid, ocid = params.ocid)
            )

        // FR.COM-1.9.1
        val result = when (stage) {
            Stage.FE -> {
                val fe = entity.jsonData
                    .tryToObject(FEEntity::class.java)
                    .doReturn { error -> return failure(Fail.Incident.DatabaseIncident(exception = error.exception)) }

                val organization = when (params.role) {
                    GetOrganization.Params.OrganizationRole.PROCURING_ENTITY -> convert(fe.tender.procuringEntity!!)
                }
                success(organization)
            }

            Stage.EV,
            Stage.NP,
            Stage.TP -> {
                val cn = entity.jsonData
                    .tryToObject(CNEntity::class.java)
                    .doReturn { error -> return failure(Fail.Incident.DatabaseIncident(exception = error.exception)) }

                val organization = when (params.role) {
                    GetOrganization.Params.OrganizationRole.PROCURING_ENTITY -> convert(cn.tender.procuringEntity)
                }
                success(organization)
            }

            Stage.AC,
            Stage.AP,
            Stage.EI,
            Stage.FS,
            Stage.PN -> failure(ValidationErrors.UnexpectedStageForGetOrganization(stage = stage))
        }
            .orForwardFail { fail -> return fail }

        return success(result)
    }

    private fun getValidBusinessFunctionTypesForPersons(params: CheckPersonesStructure.Params) =
        when (params.locationOfPersones) {
            LocationOfPersonsType.AWARD ->
                BusinessFunctionType.allowedElements
                    .filter {
                        when (it) {
                            BusinessFunctionType.CHAIRMAN,
                            BusinessFunctionType.PROCURMENT_OFFICER,
                            BusinessFunctionType.CONTACT_POINT,
                            BusinessFunctionType.TECHNICAL_EVALUATOR,
                            BusinessFunctionType.TECHNICAL_OPENER,
                            BusinessFunctionType.PRICE_OPENER,
                            BusinessFunctionType.PRICE_EVALUATOR -> true
                            BusinessFunctionType.AUTHORITY -> false
                        }
                    }.toSet()
            LocationOfPersonsType.PROCURING_ENTITY ->
                BusinessFunctionType.allowedElements
                    .filter {
                        when (it) {
                            BusinessFunctionType.CHAIRMAN,
                            BusinessFunctionType.PROCURMENT_OFFICER,
                            BusinessFunctionType.CONTACT_POINT,
                            BusinessFunctionType.TECHNICAL_EVALUATOR,
                            BusinessFunctionType.TECHNICAL_OPENER,
                            BusinessFunctionType.PRICE_OPENER,
                            BusinessFunctionType.PRICE_EVALUATOR,
                            BusinessFunctionType.AUTHORITY -> false
                        }
                    }
        }

    private fun getValidDocumentTypesForPersons(params: CheckPersonesStructure.Params) =
        when (params.locationOfPersones) {
            LocationOfPersonsType.AWARD ->
                BusinessFunctionDocumentType.allowedElements
                    .filter {
                        when (it) {
                            BusinessFunctionDocumentType.REGULATORY_DOCUMENT -> true
                        }
                    }.toSet()
            LocationOfPersonsType.PROCURING_ENTITY ->
                BusinessFunctionDocumentType.allowedElements
                    .filter {
                        when (it) {
                            BusinessFunctionDocumentType.REGULATORY_DOCUMENT -> false
                        }
                    }.toSet()
        }

    private fun getTenderProcessEntityByCpIdAndStage(
        cpid: Cpid,
        stage: Stage
    ): Result<TenderProcessEntity, Fail> {
        val entity = tenderProcessRepository.getByCpIdAndStage(cpid = cpid, stage = stage)
            .doOnError { error -> return failure(error) }
            .get
            ?: return failure(
                BadRequestErrors.EntityNotFound(
                    entityName = "TenderProcessEntity",
                    by = "by cpid = '$cpid' and stage = '$stage'"
                )
            )

        return success(entity)
    }
}

private val responderPersonKeyExtractor: (ResponderProcessing.Params.Responder) -> String =
    { it.identifier.id + it.identifier.scheme }

private val dbCNPersonKeyExtractor: (CNEntity.Tender.ProcuringEntity.Persone) -> String =
    { it.identifier.id + it.identifier.scheme }

private val dbFEPersonKeyExtractor: (FEEntity.Tender.ProcuringEntity.Person) -> String =
    { it.identifier.id + it.identifier.scheme }

private fun CNEntity.Tender.ProcuringEntity.Persone.update(
    received: ResponderProcessing.Params.Responder
)
    : CNEntity.Tender.ProcuringEntity.Persone {
    return CNEntity.Tender.ProcuringEntity.Persone(
        id = received.id,
        title = received.title,
        name = received.name,
        identifier = this.identifier.update(received.identifier),
        businessFunctions = updateStrategy(
            receivedElements = received.businessFunctions,
            keyExtractorForReceivedElement = responderBusinessFunctionKeyExtractor,
            availableElements = this.businessFunctions,
            keyExtractorForAvailableElement = dbBusinessFunctionKeyExtractor,
            updateBlock = CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction::update,
            createBlock = ::createBusinessFunction
        )
    )
}

private val responderBusinessFunctionKeyExtractor
    : (ResponderProcessing.Params.Responder.BusinessFunction)
-> String = { it.id }
private val dbBusinessFunctionKeyExtractor
    : (CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction)
-> String = { it.id }

private fun CNEntity.Tender.ProcuringEntity.Persone.Identifier.update(
    received: ResponderProcessing.Params.Responder.Identifier
)
    : CNEntity.Tender.ProcuringEntity.Persone.Identifier {
    return CNEntity.Tender.ProcuringEntity.Persone.Identifier(
        id = received.id,
        scheme = received.scheme,
        uri = received.uri ?: this.uri
    )
}

private fun CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.update(
    received: ResponderProcessing.Params.Responder.BusinessFunction
)
    : CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction {
    return CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction(
        id = received.id,
        jobTitle = received.jobTitle,
        type = received.type,
        period = this.period.update(received.period),
        documents = updateStrategy(
            receivedElements = received.documents,
            keyExtractorForReceivedElement = responderDocumentKeyExtractor,
            availableElements = this.documents.orEmpty(),
            keyExtractorForAvailableElement = dbDocumentKeyExtractor,
            updateBlock = CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Document::update,
            createBlock = ::createDocument
        )
    )
}

private val responderDocumentKeyExtractor: (ResponderProcessing.Params.Responder.BusinessFunction.Document) -> String =
    { it.id }
private val dbDocumentKeyExtractor: (CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Document) -> String =
    { it.id }

private fun CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Period.update(
    received: ResponderProcessing.Params.Responder.BusinessFunction.Period
): CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Period =
    CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Period(
        startDate = received.startDate
    )

private fun CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Document.update(
    received: ResponderProcessing.Params.Responder.BusinessFunction.Document
): CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Document =
    CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Document(
        id = received.id,
        documentType = received.documentType,
        title = received.title,
        description = received.description ?: this.description
    )

private fun createPerson(
    received: ResponderProcessing.Params.Responder
): CNEntity.Tender.ProcuringEntity.Persone =
    CNEntity.Tender.ProcuringEntity.Persone(
        id = received.id,
        title = received.title,
        name = received.name,
        identifier = createIdentifier(received.identifier),
        businessFunctions = received.businessFunctions
            .map { createBusinessFunction(it) }
    )

private fun createIdentifier(
    received: ResponderProcessing.Params.Responder.Identifier
): CNEntity.Tender.ProcuringEntity.Persone.Identifier =
    CNEntity.Tender.ProcuringEntity.Persone.Identifier(
        id = received.id,
        scheme = received.scheme,
        uri = received.uri
    )

private fun createBusinessFunction(
    received: ResponderProcessing.Params.Responder.BusinessFunction
): CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction =
    CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction(
        id = received.id,
        type = received.type,
        jobTitle = received.jobTitle,
        period = createPeriod(received.period),
        documents = received.documents
            .map { createDocument(it) }
    )

private fun createPeriod(
    received: ResponderProcessing.Params.Responder.BusinessFunction.Period
): CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Period =
    CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Period(
        startDate = received.startDate
    )

private fun createDocument(
    received: ResponderProcessing.Params.Responder.BusinessFunction.Document
): CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Document =
    CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Document(
        id = received.id,
        title = received.title,
        documentType = received.documentType,
        description = received.description
    )

private fun FEEntity.Tender.ProcuringEntity.Person.update(received: ResponderProcessing.Params.Responder): FEEntity.Tender.ProcuringEntity.Person =
    FEEntity.Tender.ProcuringEntity.Person(
        id = received.id.toString(),
        title = received.title,
        name = received.name,
        identifier = this.identifier.update(received.identifier),
        businessFunctions = updateStrategy(
            receivedElements = received.businessFunctions,
            keyExtractorForReceivedElement = responderBusinessFunctionKeyExtractor,
            availableElements = this.businessFunctions,
            keyExtractorForAvailableElement = dbFEBusinessFunctionKeyExtractor,
            updateBlock = FEEntity.Tender.ProcuringEntity.Person.BusinessFunction::update,
            createBlock = ::createFEBusinessFunction
        )
    )

private val dbFEBusinessFunctionKeyExtractor: (FEEntity.Tender.ProcuringEntity.Person.BusinessFunction) -> String =
    { it.id }

private fun FEEntity.Tender.ProcuringEntity.Person.Identifier.update(received: ResponderProcessing.Params.Responder.Identifier) =
    FEEntity.Tender.ProcuringEntity.Person.Identifier(
        id = received.id,
        scheme = received.scheme,
        uri = received.uri ?: this.uri
    )

private fun FEEntity.Tender.ProcuringEntity.Person.BusinessFunction.update(
    received: ResponderProcessing.Params.Responder.BusinessFunction
): FEEntity.Tender.ProcuringEntity.Person.BusinessFunction =
    FEEntity.Tender.ProcuringEntity.Person.BusinessFunction(
    id = received.id,
    jobTitle = received.jobTitle,
    type = received.type,
    period = this.period.update(received.period),
    documents = updateStrategy(
        receivedElements = received.documents,
        keyExtractorForReceivedElement = responderDocumentKeyExtractor,
        availableElements = this.documents.orEmpty(),
        keyExtractorForAvailableElement = dbFEDocumentKeyExtractor,
        updateBlock = FEEntity.Tender.ProcuringEntity.Person.BusinessFunction.Document::update,
        createBlock = ::createFEDocument
    )
)

private val dbFEDocumentKeyExtractor: (FEEntity.Tender.ProcuringEntity.Person.BusinessFunction.Document) -> String =
    { it.id }

private fun FEEntity.Tender.ProcuringEntity.Person.BusinessFunction.Period.update(
    received: ResponderProcessing.Params.Responder.BusinessFunction.Period
): FEEntity.Tender.ProcuringEntity.Person.BusinessFunction.Period =
    FEEntity.Tender.ProcuringEntity.Person.BusinessFunction.Period(startDate = received.startDate)

private fun FEEntity.Tender.ProcuringEntity.Person.BusinessFunction.Document.update(
    received: ResponderProcessing.Params.Responder.BusinessFunction.Document
) =
    FEEntity.Tender.ProcuringEntity.Person.BusinessFunction.Document(
        id = received.id,
        documentType = received.documentType,
        title = received.title,
        description = received.description ?: this.description
    )

private fun createFEPerson(received: ResponderProcessing.Params.Responder) =
    FEEntity.Tender.ProcuringEntity.Person(
        id = received.id.toString(),
        title = received.title,
        name = received.name,
        identifier = createFEIdentifier(received.identifier),
        businessFunctions = received.businessFunctions
            .map { createFEBusinessFunction(it) }
    )

private fun createFEIdentifier(received: ResponderProcessing.Params.Responder.Identifier) =
    FEEntity.Tender.ProcuringEntity.Person.Identifier(
        id = received.id,
        scheme = received.scheme,
        uri = received.uri
    )

private fun createFEBusinessFunction(received: ResponderProcessing.Params.Responder.BusinessFunction) =
    FEEntity.Tender.ProcuringEntity.Person.BusinessFunction(
        id = received.id,
        type = received.type,
        jobTitle = received.jobTitle,
        period = createFEPeriod(received.period),
        documents = received.documents
            .map { createFEDocument(it) }
    )

private fun createFEPeriod(received: ResponderProcessing.Params.Responder.BusinessFunction.Period) =
    FEEntity.Tender.ProcuringEntity.Person.BusinessFunction.Period(
        startDate = received.startDate
    )

private fun createFEDocument(
    received: ResponderProcessing.Params.Responder.BusinessFunction.Document
) =
    FEEntity.Tender.ProcuringEntity.Person.BusinessFunction.Document(
        id = received.id,
        title = received.title,
        documentType = received.documentType,
        description = received.description
    )

private fun getRequirementToTenderer(
    entity: TenderProcessEntity,
    stage: Stage
): Result<Map<String, Requirement>, Fail> = when (stage) {

    Stage.FE -> entity.jsonData
        .tryToObject(FEEntity::class.java)
        .doReturn { error -> return failure(Fail.Incident.DatabaseIncident(exception = error.exception)) }
        .tender.criteria
        ?.asSequence()
        ?.filter { it.relatesTo == CriteriaRelatesToEnum.TENDERER }
        ?.flatMap { it.requirementGroups.asSequence() }
        ?.flatMap { it.requirements.asSequence() }
        ?.associateBy { it.id }
        .orEmpty()
        .asSuccess()

    Stage.EV,
    Stage.NP,
    Stage.TP -> entity.jsonData
        .tryToObject(CNEntity::class.java)
        .doReturn { error -> return failure(Fail.Incident.DatabaseIncident(exception = error.exception)) }
        .tender.criteria
        ?.asSequence()
        ?.filter { it.relatesTo == CriteriaRelatesToEnum.TENDERER }
        ?.flatMap { it.requirementGroups.asSequence() }
        ?.flatMap { it.requirements.asSequence() }
        ?.associateBy { it.id }
        .orEmpty()
        .asSuccess()

    Stage.AC,
    Stage.AP,
    Stage.EI,
    Stage.FS,
    Stage.PN ->
        failure(
            ValidationErrors.UnexpectedStageForValidateRequirementResponse(stage = stage)
        )
}

private fun getAllRequirement(
    entity: TenderProcessEntity,
    stage: Stage
): Result<Map<String, Requirement>, Fail> = when (stage) {

    Stage.FE -> entity.jsonData
        .tryToObject(FEEntity::class.java)
        .doReturn { error -> return failure(Fail.Incident.DatabaseIncident(exception = error.exception)) }
        .tender.criteria
        ?.asSequence()
        ?.flatMap { it.requirementGroups.asSequence() }
        ?.flatMap { it.requirements.asSequence() }
        ?.associateBy { it.id }
        .orEmpty()
        .asSuccess()

    Stage.EV,
    Stage.NP,
    Stage.TP -> entity.jsonData
        .tryToObject(CNEntity::class.java)
        .doReturn { error -> return failure(Fail.Incident.DatabaseIncident(exception = error.exception)) }
        .tender.criteria
        ?.asSequence()
        ?.flatMap { it.requirementGroups.asSequence() }
        ?.flatMap { it.requirements.asSequence() }
        ?.associateBy { it.id }
        .orEmpty()
        .asSuccess()

    Stage.AC,
    Stage.AP,
    Stage.EI,
    Stage.FS,
    Stage.PN ->
        failure(
            ValidationErrors.UnexpectedStageForValidateRequirementResponse(stage = stage)
        )
}

fun <R, A, K> updateStrategy(
    receivedElements: List<R>,
    keyExtractorForReceivedElement: (R) -> K,
    availableElements: List<A>,
    keyExtractorForAvailableElement: (A) -> K,
    updateBlock: A.(R) -> A,
    createBlock: (R) -> A
): List<A> {
    val receivedElementsById = receivedElements.associateBy { keyExtractorForReceivedElement(it) }
    val availableElementsIds = availableElements.toSetBy { keyExtractorForAvailableElement(it) }

    val updatedElements: MutableList<A> = mutableListOf()

    availableElements.forEach { availableElement ->
        val id = keyExtractorForAvailableElement(availableElement)
        val element = receivedElementsById[id]
            ?.let { receivedElement ->
                availableElement.updateBlock(receivedElement)
            }
            ?: availableElement
        updatedElements.add(element)
    }

    val newIds = receivedElementsById.keys - availableElementsIds
    newIds.forEach { id ->
        val receivedElement = receivedElementsById.getValue(id)
        val element = createBlock(receivedElement)
        updatedElements.add(element)
    }

    return updatedElements
}

fun validateOneAnswerOnRequirementByCandidate(
    requirementResponsesForTenderer: List<ValidateRequirementResponsesParams.RequirementResponse>
): Result<Unit, ValidationErrors.DuplicatedAnswerOnValidateRequirementResponses> {
    requirementResponsesForTenderer
        .groupBy { it.requirement.id }
        .forEach { (requirementId, requirementResponses) ->
            val candidatesAnsweredRequirement = requirementResponses.map { it.relatedCandidate }
            val candidateAnsweredMultipleTimes = candidatesAnsweredRequirement.getDuplicate { it.id }

            if (candidateAnsweredMultipleTimes != null)
                return failure(
                    ValidationErrors.DuplicatedAnswerOnValidateRequirementResponses(
                        candidateId = candidateAnsweredMultipleTimes.id,
                        requirementId = requirementId,
                        requirementResponses = requirementResponses.filter { it.relatedCandidate.id == candidateAnsweredMultipleTimes.id }
                    )
                )
        }
    return success(Unit)
}
