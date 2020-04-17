package com.procurement.access.service

import com.procurement.access.application.model.responder.check.structure.CheckPersonesStructure
import com.procurement.access.application.model.responder.processing.ResponderProcessing
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.BadRequestErrors
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.enums.BusinessFunctionDocumentType
import com.procurement.access.domain.model.enums.BusinessFunctionType
import com.procurement.access.domain.model.enums.LocationOfPersonsType
import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.ValidationResult
import com.procurement.access.domain.util.extension.toSetBy
import com.procurement.access.infrastructure.dto.converter.convert
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.handler.processing.responder.ResponderProcessingResult
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toDate
import com.procurement.access.utils.toJson
import com.procurement.access.utils.tryToObject
import org.springframework.stereotype.Service

interface ResponderService {
    fun responderProcessing(params: ResponderProcessing.Params): Result<ResponderProcessingResult, Fail>
    fun checkPersonesStructure(params: CheckPersonesStructure.Params): ValidationResult<Fail.Error>
}

@Service
class ResponderServiceImpl(
    private val tenderProcessRepository: TenderProcessRepository
) : ResponderService {

    override fun responderProcessing(params: ResponderProcessing.Params): Result<ResponderProcessingResult, Fail> {
        val stage = params.ocid.stage

        val entity = getTenderProcessEntityByCpIdAndStage(cpid = params.cpid, stage = stage)
            .doOnError { error -> return Result.failure(error) }
            .get

        val cnEntity = entity.jsonData
            .tryToObject(CNEntity::class.java)
            .doOnError { error ->
                return Result.failure(Fail.Incident.DatabaseIncident(exception = error.exception))
            }
            .get

        val responder = params.responder
        val dbPersons = cnEntity.tender.procuringEntity.persones

        /**
         * BR-1.0.1.15.3
         * BR-1.0.1.15.4
         * BR-1.0.1.5.3
         */
        val updatedPersons = updateStrategy(
            receivedElements = listOf(responder),
            keyExtractorForReceivedElement = responderPersonKeyExtractor,
            availableElements = dbPersons.orEmpty(),
            keyExtractorForAvailableElement = dbPersonKeyExtractor,
            updateBlock = CNEntity.Tender.ProcuringEntity.Persone::update,
            createBlock = ::createPerson
        )

        val updatedCnEntity = cnEntity.copy(
            tender = cnEntity.tender.copy(
                procuringEntity = cnEntity.tender.procuringEntity.copy(
                    persones = updatedPersons
                )
            )
        )

        tenderProcessRepository.save(
            TenderProcessEntity(
                cpId = params.cpid.toString(),
                token = entity.token,
                stage = stage.toString(),
                owner = entity.owner,
                createdDate = params.date.toDate(),
                jsonData = toJson(updatedCnEntity)
            )
        )
            .doOnError { error ->
                return Result.failure(Fail.Incident.DatabaseIncident(exception = error.exception))
            }

        return Result.success(updatedCnEntity.tender.procuringEntity.convert())
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

    private fun getValidBusinessFunctionTypesForPersons(params: CheckPersonesStructure.Params) =
        when (params.locationOfPersones) {
            LocationOfPersonsType.AWARD            ->
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
                            BusinessFunctionType.AUTHORITY       -> false
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
            LocationOfPersonsType.AWARD            ->
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
            .doOnError { error -> return Result.failure(error) }
            .get
            ?: return Result.failure(
                BadRequestErrors.EntityNotFound(
                    entityName = "TenderProcessEntity",
                    by = "by cpid = '$cpid' and stage = '$stage'"
                )
            )

        return Result.success(entity)
    }
}

private val responderPersonKeyExtractor
    : (ResponderProcessing.Params.Responder)
-> String = { it.identifier.id + it.identifier.scheme }
private val dbPersonKeyExtractor
    : (CNEntity.Tender.ProcuringEntity.Persone)
-> String = { it.identifier.id + it.identifier.scheme }

private fun CNEntity.Tender.ProcuringEntity.Persone.update(
    received: ResponderProcessing.Params.Responder
)
    : CNEntity.Tender.ProcuringEntity.Persone {
    return CNEntity.Tender.ProcuringEntity.Persone(
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

private val responderDocumentKeyExtractor
    : (ResponderProcessing.Params.Responder.BusinessFunction.Document)
-> String = { it.id }
private val dbDocumentKeyExtractor
    : (CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Document)
-> String = { it.id }

private fun CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Period.update(
    received: ResponderProcessing.Params.Responder.BusinessFunction.Period
)
    : CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Period =
    CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Period(
        startDate = received.startDate
    )

private fun CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Document.update(
    received: ResponderProcessing.Params.Responder.BusinessFunction.Document
)
    : CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Document =
    CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Document(
        id = received.id,
        documentType = received.documentType,
        title = received.title,
        description = received.description ?: this.description
    )

private fun createPerson(
    received: ResponderProcessing.Params.Responder
)
    : CNEntity.Tender.ProcuringEntity.Persone =
    CNEntity.Tender.ProcuringEntity.Persone(
        title = received.title,
        name = received.name,
        identifier = createIdentifier(received.identifier),
        businessFunctions = received.businessFunctions
            .map { createBusinessFunction(it) }
    )

private fun createIdentifier(
    received: ResponderProcessing.Params.Responder.Identifier
)
    : CNEntity.Tender.ProcuringEntity.Persone.Identifier =
    CNEntity.Tender.ProcuringEntity.Persone.Identifier(
        id = received.id,
        scheme = received.scheme,
        uri = received.uri
    )

private fun createBusinessFunction(
    received: ResponderProcessing.Params.Responder.BusinessFunction
)
    : CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction =
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
)
    : CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Period =
    CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Period(
        startDate = received.startDate
    )

private fun createDocument(
    received: ResponderProcessing.Params.Responder.BusinessFunction.Document
)
    : CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Document =
    CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Document(
        id = received.id,
        title = received.title,
        documentType = received.documentType,
        description = received.description
    )

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

