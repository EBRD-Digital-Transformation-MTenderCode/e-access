package com.procurement.access.service

import com.procurement.access.application.model.errors.GetOrganizationsErrors
import com.procurement.access.application.model.errors.PersonesProcessingErrors
import com.procurement.access.application.model.organization.GetOrganizations
import com.procurement.access.application.model.params.PersonesProcessingParams
import com.procurement.access.application.model.responder.check.structure.CheckPersonesStructure
import com.procurement.access.application.model.responder.processing.ResponderProcessing
import com.procurement.access.application.model.responder.verify.VerifyRequirementResponse
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.application.service.requirement.ValidateRequirementResponsesParams
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.enums.BusinessFunctionDocumentType
import com.procurement.access.domain.model.enums.BusinessFunctionType
import com.procurement.access.domain.model.enums.CriteriaRelatesTo
import com.procurement.access.domain.model.enums.CriteriaSource
import com.procurement.access.domain.model.enums.LocationOfPersonsType
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.domain.model.enums.PartyRole
import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.domain.model.requirement.Requirement
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.entity.FEEntity
import com.procurement.access.infrastructure.handler.v1.converter.convert
import com.procurement.access.infrastructure.handler.v1.converter.toReference
import com.procurement.access.infrastructure.handler.v2.model.response.GetOrganizationsResult
import com.procurement.access.infrastructure.handler.v2.model.response.PersonesProcessingResult
import com.procurement.access.infrastructure.handler.v2.model.response.ResponderProcessingResult
import com.procurement.access.infrastructure.handler.v2.model.response.ValidateRequirementResponsesResult
import com.procurement.access.lib.extension.getDuplicate
import com.procurement.access.lib.extension.toSet
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.Result.Companion.failure
import com.procurement.access.lib.functional.Result.Companion.success
import com.procurement.access.lib.functional.ValidationResult
import com.procurement.access.lib.functional.asFailure
import com.procurement.access.lib.functional.asSuccess
import com.procurement.access.lib.functional.asValidationFailure
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toJson
import com.procurement.access.utils.trySerialization
import com.procurement.access.utils.tryToObject
import org.springframework.stereotype.Service

interface ResponderService {
    fun responderProcessing(params: ResponderProcessing.Params): Result<ResponderProcessingResult, Fail>
    fun checkPersonesStructure(params: CheckPersonesStructure.Params): ValidationResult<Fail.Error>
    fun verifyRequirementResponse(params: VerifyRequirementResponse.Params): ValidationResult<Fail>
    fun validateRequirementResponses(params: ValidateRequirementResponsesParams): Result<ValidateRequirementResponsesResult, Fail>
    fun getOrganizations(params: GetOrganizations.Params): Result<GetOrganizationsResult, Fail>
    fun personesProcessing(params: PersonesProcessingParams): Result<PersonesProcessingResult, Fail>
}

@Service
class ResponderServiceImpl(
    private val tenderProcessRepository: TenderProcessRepository
) : ResponderService {

    override fun responderProcessing(params: ResponderProcessing.Params): Result<ResponderProcessingResult, Fail> {
        val stage = params.ocid.stage

        val entity = getTenderProcessEntityByCpIdAndStage(cpid = params.cpid, stage = stage)
            .onFailure { error -> return error }

        val updatedTenderJson = when (stage) {
            Stage.FE -> {
                val fe = entity.jsonData
                    .tryToObject(FEEntity::class.java)
                    .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
                    .onFailure { return it }

                val responder = params.responder

                val procuringEntityParty = fe.parties
                    .firstOrNull { it.roles.contains(PartyRole.PROCURING_ENTITY) }
                    ?: return ValidationErrors.ProcuringEntityPartyNotFoundForResponderProcessing().asFailure()

                val dbPersons = procuringEntityParty.persones.orEmpty()

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
                    updateBlock = FEEntity.Party.Person::update,
                    createBlock = ::createFEPerson
                )

                val updatedParties = fe.parties.map { party ->
                    if (party.id == procuringEntityParty.id)
                        party.copy(persones = updatedPersons)
                    else party
                }

                val updatedFe = fe.copy(parties = updatedParties)

                success(toJson(updatedFe))
            }

            Stage.EV,
            Stage.NP,
            Stage.TP -> {
                val cn = entity.jsonData
                    .tryToObject(CNEntity::class.java)
                    .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
                    .onFailure { return it }

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
            Stage.PC,
            Stage.PN,
            Stage.RQ ->
                failure(
                    ValidationErrors.UnexpectedStageForResponderProcessing(stage = params.ocid.stage)
                )
        }
            .onFailure { error -> return error }

        tenderProcessRepository.save(
            TenderProcessEntity(
                cpId = params.cpid.value,
                token = entity.token,
                stage = stage.toString(),
                owner = entity.owner,
                createdDate = params.date,
                jsonData = updatedTenderJson
            )
        )
            .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
            .onFailure { return it }

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
            .onFailure { return Fail.Incident.DatabaseIncident(exception = it.reason.exception).asValidationFailure() }
            ?: return ValidationErrors.RequirementsNotFoundOnVerifyRequirementResponse(
                cpid = params.cpid,
                ocid = params.ocid
            )
                .asValidationFailure()

        val cn = cnEntity.jsonData
            .tryToObject(CNEntity::class.java)
            .onFailure { return ValidationResult.error(Fail.Incident.DatabaseIncident(exception = it.reason.exception)) }

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
            .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
            .onFailure { return it }
            ?: return success(ValidateRequirementResponsesResult(emptyList()))

        val filteredRequirement = when (params.operationType) {
            OperationType.CREATE_SUBMISSION -> getRequirementToTenderer(tenderProcessEntity, params.ocid.stage)

            OperationType.AMEND_FE,
            OperationType.APPLY_CONFIRMATIONS,
            OperationType.APPLY_QUALIFICATION_PROTOCOL,
            OperationType.AWARD_CONSIDERATION,
            OperationType.COMPLETE_QUALIFICATION,
            OperationType.CREATE_AWARD,
            OperationType.CREATE_CN,
            OperationType.CREATE_CN_ON_PIN,
            OperationType.CREATE_CN_ON_PN,
            OperationType.CREATE_CONFIRMATION_RESPONSE_BY_BUYER,
            OperationType.CREATE_CONFIRMATION_RESPONSE_BY_INVITED_CANDIDATE,
            OperationType.CREATE_FE,
            OperationType.CREATE_NEGOTIATION_CN_ON_PN,
            OperationType.CREATE_PCR,
            OperationType.CREATE_PIN,
            OperationType.CREATE_PIN_ON_PN,
            OperationType.CREATE_PN,
            OperationType.CREATE_RFQ,
            OperationType.DECLARE_NON_CONFLICT_OF_INTEREST,
            OperationType.DIVIDE_LOT,
            OperationType.ISSUING_FRAMEWORK_CONTRACT,
            OperationType.OUTSOURCING_PN,
            OperationType.QUALIFICATION,
            OperationType.QUALIFICATION_CONSIDERATION,
            OperationType.QUALIFICATION_DECLARE_NON_CONFLICT_OF_INTEREST,
            OperationType.QUALIFICATION_PROTOCOL,
            OperationType.RELATION_AP,
            OperationType.START_SECONDSTAGE,
            OperationType.SUBMISSION_PERIOD_END,
            OperationType.SUBMIT_BID,
            OperationType.TENDER_PERIOD_END,
            OperationType.UPDATE_AP,
            OperationType.UPDATE_AWARD,
            OperationType.UPDATE_CN,
            OperationType.UPDATE_PN,
            OperationType.WITHDRAW_BID,
            OperationType.WITHDRAW_QUALIFICATION_PROTOCOL -> getAllRequirement(tenderProcessEntity, params.ocid.stage)
        }
            .onFailure { fail -> return fail }

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
            .onFailure { error -> return error }

        return success(requirementResponsesForTenderer.convert())
    }

    override fun getOrganizations(params: GetOrganizations.Params): Result<GetOrganizationsResult, Fail> {
        val stage = params.ocid.stage

        val entity = tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = stage)
            .onFailure { error -> return error }
            ?: return failure(
                GetOrganizationsErrors.TenderNotFound(cpid = params.cpid, ocid = params.ocid)
            )

        // FR.COM-1.9.1
        val parties = when (stage) {
            Stage.FE -> {
                val fe = entity.jsonData
                    .tryToObject(FEEntity::class.java)
                    .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
                    .onFailure { return it }

                val organizations = when (params.role) {
                    GetOrganizations.Params.OrganizationRole.PROCURING_ENTITY -> fe.parties.filter {
                        it.roles.contains(
                            PartyRole.PROCURING_ENTITY
                        )
                    }
                    GetOrganizations.Params.OrganizationRole.BUYER -> fe.parties.filter { it.roles.contains(PartyRole.BUYER) }
                }
                val convertedOrganizations = organizations.map { convert(it) }

                success(convertedOrganizations)
            }

            Stage.EV,
            Stage.NP,
            Stage.TP -> {
                val cn = entity.jsonData
                    .tryToObject(CNEntity::class.java)
                    .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
                    .onFailure { return it }

                val organization = when (params.role) {
                    GetOrganizations.Params.OrganizationRole.PROCURING_ENTITY -> convert(cn.tender.procuringEntity)
                    GetOrganizations.Params.OrganizationRole.BUYER -> return failure(
                        GetOrganizationsErrors.OrganizationByRoleNotFound(
                            params.role
                        )
                    )
                }
                success(listOf(organization))
            }

            Stage.AC,
            Stage.AP,
            Stage.EI,
            Stage.FS,
            Stage.PC,
            Stage.PN,
            Stage.RQ -> failure(GetOrganizationsErrors.UnexpectedStage(stage = stage))
        }
            .onFailure { fail -> return fail }

        if (parties.isEmpty())
            return failure(GetOrganizationsErrors.OrganizationByRoleNotFound(params.role))

        return success(GetOrganizationsResult(parties = parties))
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
            .onFailure { return it }
            ?: return ValidationErrors.TenderNotFoundForResponderProcessing(cpid = cpid, stage = stage)
                .asFailure()

        return success(entity)
    }

    override fun personesProcessing(params: PersonesProcessingParams): Result<PersonesProcessingResult, Fail> {
        val entity = tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid)
            .onFailure { error -> return error }
            ?: return PersonesProcessingErrors.TenderNotFound(cpid = params.cpid, ocid = params.ocid).asFailure()

        val fe = entity.jsonData.tryToObject(FEEntity::class.java)
            .onFailure { return it }

        val receivedParty = params.parties.first()

        return when (params.role) {
            PartyRole.BUYER -> {
                val party = fe.parties.firstOrNull { it.id == receivedParty.id }
                    ?: return PersonesProcessingErrors.OrganizationNotFound(params.role, receivedParty.id).asFailure()
                val updatedPersones = getUpdatedPersones(receivedParty, party)
                val updatedParty = party.copy(persones = updatedPersones)
                val updatedParties = fe.parties.map { party ->
                    if (party.id == receivedParty.id) updatedParty
                    else party
                }
                val updatedFe = fe.copy(parties = updatedParties)

                trySerialization(updatedFe)
                    .map { json -> entity.copy(jsonData = json) }
                    .flatMap { updatedEntity -> tenderProcessRepository.update(updatedEntity) }
                    .onFailure { fail -> return fail }

                updatedParty.toPersonesProcessingResult().asSuccess()
            }
            PartyRole.PROCURING_ENTITY,
            PartyRole.CLIENT,
            PartyRole.CENTRAL_PURCHASING_BODY,
            PartyRole.AUTHOR,
            PartyRole.CANDIDATE,
            PartyRole.ENQUIRER,
            PartyRole.FUNDER,
            PartyRole.INVITED_CANDIDATE,
            PartyRole.INVITED_TENDERER,
            PartyRole.PAYEE,
            PartyRole.PAYER,
            PartyRole.REVIEW_BODY,
            PartyRole.SUPPLIER,
            PartyRole.TENDERER -> throw ErrorException(ErrorType.INVALID_ROLE)
        }
    }

    private fun getUpdatedPersones(
        receivedParty: PersonesProcessingParams.Party,
        party: FEEntity.Party
    ): List<FEEntity.Party.Person> =
        updateStrategy(
            receivedElements = receivedParty.persones,
            keyExtractorForReceivedElement = { it.id },
            availableElements = party.persones.orEmpty(),
            keyExtractorForAvailableElement = { it.id },
            updateBlock = { received -> this.updateBy(received) },
            createBlock = { received -> received.toDomain() }
        )

    private fun FEEntity.Party.Person.updateBy(
        receivedPerson: PersonesProcessingParams.Party.Persone
    ): FEEntity.Party.Person {
        val updatedBusinessFunctions = updateStrategy(
            receivedElements = receivedPerson.businessFunctions,
            keyExtractorForReceivedElement = { it.id },
            availableElements = businessFunctions,
            keyExtractorForAvailableElement = { it.id },
            updateBlock = { received -> this.updateBy(received) },
            createBlock = { received -> received.toDomain() }
        )

        return this.copy(
            title = receivedPerson.title.key,
            name = receivedPerson.name,
            identifier = receivedPerson.identifier.let { identifier ->
                FEEntity.Party.Person.Identifier(
                    id = identifier.id,
                    scheme = identifier.scheme,
                    uri = identifier.uri ?: this.identifier.uri
                )
            },
            businessFunctions = updatedBusinessFunctions
        )
    }

    private fun FEEntity.Party.Person.BusinessFunction.updateBy(receivedBusinessFunction: PersonesProcessingParams.Party.Persone.BusinessFunction): FEEntity.Party.Person.BusinessFunction {
        val updatedDocuments = updateStrategy(
            receivedElements = receivedBusinessFunction.documents.orEmpty(),
            keyExtractorForReceivedElement = { it.id },
            availableElements = documents.orEmpty(),
            keyExtractorForAvailableElement = { it.id },
            updateBlock = { received -> this.updateBy(received) },
            createBlock = { received -> received.toDomain() }
        )

        return this.copy(
            type = receivedBusinessFunction.type,
            jobTitle = receivedBusinessFunction.jobTitle,
            period = receivedBusinessFunction.period.let {
                FEEntity.Party.Person.BusinessFunction.Period(it.startDate)
            },
            documents = updatedDocuments
        )
    }

    private fun FEEntity.Party.Person.BusinessFunction.Document.updateBy(receivedDocument: PersonesProcessingParams.Party.Persone.BusinessFunction.Document) = this.copy(
        documentType = receivedDocument.documentType,
        description = receivedDocument.description ?: this.description,
        title = receivedDocument.title
    )

    private fun PersonesProcessingParams.Party.Persone.toDomain() =
        FEEntity.Party.Person(
            id = id,
            name = name,
            title = title.key,
            identifier = FEEntity.Party.Person.Identifier(
                id = identifier.id,
                scheme = identifier.scheme,
                uri = identifier.uri
            ),
            businessFunctions = businessFunctions.map { businessFunction -> businessFunction.toDomain() }
        )

    private fun PersonesProcessingParams.Party.Persone.BusinessFunction.Document.toDomain() =
        FEEntity.Party.Person.BusinessFunction.Document(
            id = id,
            title = title,
            documentType = documentType,
            description = description
        )

    private fun PersonesProcessingParams.Party.Persone.BusinessFunction.toDomain() =
        FEEntity.Party.Person.BusinessFunction(
            id = id,
            period = period.let { FEEntity.Party.Person.BusinessFunction.Period(startDate = it.startDate) },
            jobTitle = jobTitle,
            type = type,
            documents = documents?.map { document ->
                document.toDomain()
            }
        )

    private fun FEEntity.Party.toPersonesProcessingResult() = PersonesProcessingResult(
        parties = listOf(
            PersonesProcessingResult.Party(
            id = id,
            name = name,
            identifier = identifier
                .let { identifier ->
                    PersonesProcessingResult.Party.Identifier(
                        scheme = identifier.scheme,
                        id = identifier.id,
                        legalName = identifier.legalName,
                        uri = identifier.uri
                    )
                },
            additionalIdentifiers = additionalIdentifiers
                ?.map { additionalIdentifier ->
                    PersonesProcessingResult.Party.AdditionalIdentifier(
                        scheme = additionalIdentifier.scheme,
                        id = additionalIdentifier.id,
                        legalName = additionalIdentifier.legalName,
                        uri = additionalIdentifier.uri
                    )
                },
            address = address
                .let { address ->
                    PersonesProcessingResult.Party.Address(
                        streetAddress = address.streetAddress,
                        postalCode = address.postalCode,
                        addressDetails = address.addressDetails
                            .let { addressDetails ->
                                PersonesProcessingResult.Party.Address.AddressDetails(
                                    country = addressDetails.country
                                        .let { country ->
                                            PersonesProcessingResult.Party.Address.AddressDetails.Country(
                                                scheme = country.scheme,
                                                id = country.id,
                                                description = country.description,
                                                uri = country.uri
                                            )
                                        },
                                    region = addressDetails.region
                                        .let { region ->
                                            PersonesProcessingResult.Party.Address.AddressDetails.Region(
                                                scheme = region.scheme,
                                                id = region.id,
                                                description = region.description,
                                                uri = region.uri
                                            )
                                        },
                                    locality = addressDetails.locality
                                        .let { locality ->
                                            PersonesProcessingResult.Party.Address.AddressDetails.Locality(
                                                scheme = locality.scheme,
                                                id = locality.id,
                                                description = locality.description,
                                                uri = locality.uri
                                            )
                                        }
                                )
                            }
                    )
                },
            contactPoint = contactPoint
                .let { contactPoint ->
                    PersonesProcessingResult.Party.ContactPoint(
                        name = contactPoint.name,
                        email = contactPoint.email,
                        telephone = contactPoint.telephone,
                        faxNumber = contactPoint.faxNumber,
                        url = contactPoint.url
                    )
                },
            roles = roles,
            persones = persones?.map { person ->
                PersonesProcessingResult.Party.Persone(
                    id = person.id,
                    title = person.title,
                    name = person.name,
                    identifier = person.identifier
                        .let { identifier ->
                            PersonesProcessingResult.Party.Persone.Identifier(
                                id = identifier.id,
                                scheme = identifier.scheme,
                                uri = identifier.uri
                            )
                        },
                    businessFunctions = person.businessFunctions
                        .map { businessFunctions ->
                            PersonesProcessingResult.Party.Persone.BusinessFunction(
                                id = businessFunctions.id,
                                jobTitle = businessFunctions.jobTitle,
                                type = businessFunctions.type,
                                period = businessFunctions.period
                                    .let { period ->
                                        PersonesProcessingResult.Party.Persone.BusinessFunction.Period(
                                            startDate = period.startDate
                                        )
                                    },
                                documents = businessFunctions.documents
                                    ?.map { document ->
                                        PersonesProcessingResult.Party.Persone.BusinessFunction.Document(
                                            id = document.id,
                                            title = document.title,
                                            description = document.description,
                                            documentType = document.documentType
                                        )
                                    }
                            )
                        }
                )
            },
            details = details
                ?.let { details ->
                    PersonesProcessingResult.Party.Details(
                        typeOfBuyer = details.typeOfBuyer,
                        mainGeneralActivity = details.mainGeneralActivity,
                        mainSectoralActivity = details.mainSectoralActivity
                    )
                }
            ))
    )
}


private val responderPersonKeyExtractor: (ResponderProcessing.Params.Responder) -> String =
    { it.identifier.id + it.identifier.scheme }

private val dbCNPersonKeyExtractor: (CNEntity.Tender.ProcuringEntity.Persone) -> String =
    { it.identifier.id + it.identifier.scheme }

private val dbFEPersonKeyExtractor: (FEEntity.Party.Person) -> String =
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

private fun FEEntity.Party.Person.update(received: ResponderProcessing.Params.Responder): FEEntity.Party.Person =
    FEEntity.Party.Person(
        id = received.id,
        title = received.title,
        name = received.name,
        identifier = this.identifier.update(received.identifier),
        businessFunctions = updateStrategy(
            receivedElements = received.businessFunctions,
            keyExtractorForReceivedElement = responderBusinessFunctionKeyExtractor,
            availableElements = this.businessFunctions,
            keyExtractorForAvailableElement = dbFEBusinessFunctionKeyExtractor,
            updateBlock = FEEntity.Party.Person.BusinessFunction::update,
            createBlock = ::createFEBusinessFunction
        )
    )

private val dbFEBusinessFunctionKeyExtractor: (FEEntity.Party.Person.BusinessFunction) -> String =
    { it.id }

private fun FEEntity.Party.Person.Identifier.update(received: ResponderProcessing.Params.Responder.Identifier) =
    FEEntity.Party.Person.Identifier(
        id = received.id,
        scheme = received.scheme,
        uri = received.uri ?: this.uri
    )

private fun FEEntity.Party.Person.BusinessFunction.update(
    received: ResponderProcessing.Params.Responder.BusinessFunction
): FEEntity.Party.Person.BusinessFunction =
    FEEntity.Party.Person.BusinessFunction(
        id = received.id,
        jobTitle = received.jobTitle,
        type = received.type,
        period = this.period.update(received.period),
        documents = updateStrategy(
            receivedElements = received.documents,
            keyExtractorForReceivedElement = responderDocumentKeyExtractor,
            availableElements = this.documents.orEmpty(),
            keyExtractorForAvailableElement = dbFEDocumentKeyExtractor,
            updateBlock = FEEntity.Party.Person.BusinessFunction.Document::update,
            createBlock = ::createFEDocument
        )
    )

private val dbFEDocumentKeyExtractor: (FEEntity.Party.Person.BusinessFunction.Document) -> String =
    { it.id }

private fun FEEntity.Party.Person.BusinessFunction.Period.update(
    received: ResponderProcessing.Params.Responder.BusinessFunction.Period
): FEEntity.Party.Person.BusinessFunction.Period =
    FEEntity.Party.Person.BusinessFunction.Period(startDate = received.startDate)

private fun FEEntity.Party.Person.BusinessFunction.Document.update(
    received: ResponderProcessing.Params.Responder.BusinessFunction.Document
) =
    FEEntity.Party.Person.BusinessFunction.Document(
        id = received.id,
        documentType = received.documentType,
        title = received.title,
        description = received.description ?: this.description
    )

private fun createFEPerson(received: ResponderProcessing.Params.Responder) =
    FEEntity.Party.Person(
        id = received.id,
        title = received.title,
        name = received.name,
        identifier = createFEIdentifier(received.identifier),
        businessFunctions = received.businessFunctions
            .map { createFEBusinessFunction(it) }
    )

private fun createFEIdentifier(received: ResponderProcessing.Params.Responder.Identifier) =
    FEEntity.Party.Person.Identifier(
        id = received.id,
        scheme = received.scheme,
        uri = received.uri
    )

private fun createFEBusinessFunction(received: ResponderProcessing.Params.Responder.BusinessFunction) =
    FEEntity.Party.Person.BusinessFunction(
        id = received.id,
        type = received.type,
        jobTitle = received.jobTitle,
        period = createFEPeriod(received.period),
        documents = received.documents
            .map { createFEDocument(it) }
    )

private fun createFEPeriod(received: ResponderProcessing.Params.Responder.BusinessFunction.Period) =
    FEEntity.Party.Person.BusinessFunction.Period(
        startDate = received.startDate
    )

private fun createFEDocument(
    received: ResponderProcessing.Params.Responder.BusinessFunction.Document
) =
    FEEntity.Party.Person.BusinessFunction.Document(
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
        .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
        .onFailure { return it }
        .tender.criteria
        ?.asSequence()
        ?.filter { it.relatesTo == CriteriaRelatesTo.TENDERER }
        ?.flatMap { it.requirementGroups.asSequence() }
        ?.flatMap { it.requirements.asSequence() }
        ?.associateBy { it.id }
        .orEmpty()
        .asSuccess()

    Stage.EV,
    Stage.NP,
    Stage.TP -> entity.jsonData
        .tryToObject(CNEntity::class.java)
        .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
        .onFailure { return it }
        .tender.criteria
        ?.asSequence()
        ?.filter { it.relatesTo == CriteriaRelatesTo.TENDERER }
        ?.flatMap { it.requirementGroups.asSequence() }
        ?.flatMap { it.requirements.asSequence() }
        ?.associateBy { it.id }
        .orEmpty()
        .asSuccess()

    Stage.AC,
    Stage.AP,
    Stage.EI,
    Stage.FS,
    Stage.PC,
    Stage.PN,
    Stage.RQ ->
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
        .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
        .onFailure { return it }
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
        .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
        .onFailure { return it }
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
    Stage.PC,
    Stage.PN,
    Stage.RQ ->
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
    val availableElementsIds = availableElements.toSet { keyExtractorForAvailableElement(it) }

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
