package com.procurement.access.service

import com.procurement.access.application.model.responder.check.structure.CheckPersonesStructureParams
import com.procurement.access.application.model.responder.processing.ResponderProcessingParams
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.application.service.Logger
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.BadRequestErrors
import com.procurement.access.domain.fail.error.ValidationError
import com.procurement.access.domain.model.enums.BusinessFunctionDocumentType
import com.procurement.access.domain.model.enums.BusinessFunctionType
import com.procurement.access.domain.model.enums.LocationOfPersonsType
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.ValidationResult
import com.procurement.access.domain.util.extension.toSetBy
import com.procurement.access.infrastructure.dto.converter.convert
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.handler.processing.responder.ResponderProcessingResponse
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.getStageFromOcid
import com.procurement.access.utils.toDate
import com.procurement.access.utils.toJson
import com.procurement.access.utils.tryToObject
import org.springframework.stereotype.Service

interface ResponderService {
    fun responderProcessing(params: ResponderProcessingParams): Result<ResponderProcessingResponse, Fail>
    fun checkPersonesStructure(params: CheckPersonesStructureParams): ValidationResult<Fail.Error>
}

@Service
class ResponderServiceImpl(
    private val logger: Logger,
    private val tenderProcessRepository: TenderProcessRepository
) : ResponderService {

    override fun responderProcessing(params: ResponderProcessingParams): Result<ResponderProcessingResponse, Fail> {

        val stage = params.ocid.getStageFromOcid()

        val entity = getTenderProcessEntityByCpIdAndStage(cpId = params.cpid, stage = stage)
            .doOnError { error -> return Result.failure(error) }
            .get

        val cnEntity = entity.jsonData.tryToObject(CNEntity::class.java)
            .doOnError { error ->
                error.logging(logger)
                return Result.failure(Fail.Incident.DatabaseIncident())
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
                cpId = params.cpid,
                token = entity.token,
                stage = stage,
                owner = entity.owner,
                createdDate = params.startDate.toDate(),
                jsonData = toJson(updatedCnEntity)
            )
        )
            .doOnError { error ->
                error.logging(logger)
                return Result.failure(Fail.Incident.DatabaseIncident())
            }

        return Result.success(updatedCnEntity.tender.procuringEntity.convert())
    }

    override fun checkPersonesStructure(params: CheckPersonesStructureParams): ValidationResult<Fail.Error> {
        when (params.locationOfPersones) {
            LocationOfPersonsType.REQUIREMENT_RESPONSE -> {
                params.persons
                    .flatMap { it.businessFunctions }
                    .apply {
                        val result = validateRequirementResponseBusinessFunctionfType() // VR-10.5.5.2
                        if (result.isError) return result
                    }
                    .flatMap { it.documents }
                    .apply {
                        val result = validateRequirementResponseDocumentType() // // VR-10.5.5.1
                        if (result.isError) return result
                    }
            }
        }

        return ValidationResult.ok()
    }

    private fun List<CheckPersonesStructureParams.Person.BusinessFunction>.validateRequirementResponseBusinessFunctionfType()
        : ValidationResult<Fail.Error> {
        this.forEach {
            when (it.type) {
                BusinessFunctionType.CHAIRMAN,
                BusinessFunctionType.PROCURMENT_OFFICER,
                BusinessFunctionType.CONTACT_POINT,
                BusinessFunctionType.TECHNICAL_EVALUATOR,
                BusinessFunctionType.TECHNICAL_OPENER,
                BusinessFunctionType.PRICE_OPENER,
                BusinessFunctionType.PRICE_EVALUATOR -> Unit
                BusinessFunctionType.AUTHORITY       -> return ValidationResult.error(
                    ValidationError.InvalidBusinessFunctionType(it.id)
                )
            }
        }
        return ValidationResult.ok()
    }

    private fun List<CheckPersonesStructureParams.Person.BusinessFunction.Document>.validateRequirementResponseDocumentType()
        : ValidationResult<Fail.Error> {
        this.forEach {
            when (it.documentType) {
                BusinessFunctionDocumentType.REGULATORY_DOCUMENT -> Unit
            }
        }
        return ValidationResult.ok()
    }

    private fun getTenderProcessEntityByCpIdAndStage(cpId: String, stage: String): Result<TenderProcessEntity, Fail> {
        val entity = tenderProcessRepository.getByCpIdAndStage(cpId = cpId, stage = stage)
            .doOnError { error -> return Result.failure(error) }
            .get
            ?: return Result.failure(
                BadRequestErrors.EntityNotFound(
                    entityName = "TenderProcessEntity",
                    by = "by cpid = '$cpId' and stage = '$stage'"
                )
            )

        return Result.success(entity)
    }
}

private val responderPersonKeyExtractor: (ResponderProcessingParams.Responder) -> String = { it.identifier.id + it.identifier.scheme }
private val dbPersonKeyExtractor: (CNEntity.Tender.ProcuringEntity.Persone) -> String = { it.identifier.id + it.identifier.scheme }

private fun CNEntity.Tender.ProcuringEntity.Persone.update(received: ResponderProcessingParams.Responder): CNEntity.Tender.ProcuringEntity.Persone {
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

private val responderBusinessFunctionKeyExtractor: (ResponderProcessingParams.Responder.BusinessFunction) -> String = { it.id }
private val dbBusinessFunctionKeyExtractor: (CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction) -> String = { it.id }

private fun CNEntity.Tender.ProcuringEntity.Persone.Identifier.update(received: ResponderProcessingParams.Responder.Identifier): CNEntity.Tender.ProcuringEntity.Persone.Identifier {
    return CNEntity.Tender.ProcuringEntity.Persone.Identifier(
        id = received.id,
        scheme = received.scheme,
        uri = received.uri ?: this.uri
    )
}

private fun CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.update(received: ResponderProcessingParams.Responder.BusinessFunction): CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction {
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

private val responderDocumentKeyExtractor: (ResponderProcessingParams.Responder.BusinessFunction.Document) -> String = { it.id }
private val dbDocumentKeyExtractor: (CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Document) -> String = { it.id }

private fun CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Period.update(received: ResponderProcessingParams.Responder.BusinessFunction.Period): CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Period =
    CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Period(
        startDate = received.startDate
    )

private fun CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Document.update(received: ResponderProcessingParams.Responder.BusinessFunction.Document): CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Document =
    CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Document(
        id = received.id,
        documentType = received.documentType,
        title = received.title,
        description = received.description ?: this.description
    )

private fun createPerson(received: ResponderProcessingParams.Responder): CNEntity.Tender.ProcuringEntity.Persone =
    CNEntity.Tender.ProcuringEntity.Persone(
        title = received.title,
        name = received.name,
        identifier = createIdentifier(received.identifier),
        businessFunctions = received.businessFunctions
            .map { createBusinessFunction(it) }
    )

private fun createIdentifier(received: ResponderProcessingParams.Responder.Identifier): CNEntity.Tender.ProcuringEntity.Persone.Identifier =
    CNEntity.Tender.ProcuringEntity.Persone.Identifier(
        id = received.id,
        scheme = received.scheme,
        uri = received.uri
    )

private fun createBusinessFunction(received: ResponderProcessingParams.Responder.BusinessFunction): CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction =
    CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction(
        id = received.id,
        type = received.type,
        jobTitle = received.jobTitle,
        period = createPeriod(received.period),
        documents = received.documents
            .map { createDocument(it) }
    )

private fun createPeriod(received: ResponderProcessingParams.Responder.BusinessFunction.Period): CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Period =
    CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Period(
        startDate = received.startDate
    )

private fun createDocument(received: ResponderProcessingParams.Responder.BusinessFunction.Document): CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Document =
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

