package com.procurement.access.service

import com.procurement.access.application.service.fe.update.AmendFEContext
import com.procurement.access.application.service.fe.update.AmendFEData
import com.procurement.access.application.service.fe.update.AmendFEResult
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.enums.DocumentType
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.persone.PersonId
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.entity.FEEntity
import com.procurement.access.infrastructure.handler.v1.converter.AmendFeEntityConverter
import com.procurement.access.lib.extension.getElementsForUpdate
import com.procurement.access.lib.extension.getMissingElements
import com.procurement.access.lib.extension.getNewElements
import com.procurement.access.lib.takeIfNotNullOrDefault
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

interface FeAmendService {
    fun amendFe(context: AmendFEContext, request: AmendFEData): AmendFEResult
}

@Service
class FeAmendServiceImpl(private val tenderProcessDao: TenderProcessDao) : FeAmendService {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(FeCreateService::class.java)
    }

    override fun amendFe(context: AmendFEContext, request: AmendFEData): AmendFEResult {
        val cpid = context.cpid
        val stage = context.stage

        val entity = tenderProcessDao.getByCpIdAndStage(cpId = cpid, stage = stage)
            ?: throw ErrorException(
                error = ErrorType.ENTITY_NOT_FOUND,
                message = "Cannot find tender by cpid='$cpid' and stage='$stage'."
            )

        val storedFE = toObject(FEEntity::class.java, entity.jsonData)

        // VR-1.0.1.1.4
        validateStatus(storedFE.tender.status)

        val updatedProcuringEntity = request.tender.procuringEntity
            ?.let { receivedProcuringEntity ->
                storedFE.tender.procuringEntity?.updatePersons(receivedProcuringEntity.persons)
            }
            ?: storedFE.tender.procuringEntity

        // BR-1.0.1.5.4
        val updatedTenderDocuments = storedFE.tender.documents
            .orEmpty()
            .updateTenderDocuments(request.tender.documents)

        val updatedFE = storedFE.copy(
            tender = storedFE.tender.copy(
                title = request.tender.title,
                description = request.tender.description,
                procurementMethodRationale = request.tender.procurementMethodRationale,
                procuringEntity = updatedProcuringEntity,
                documents = updatedTenderDocuments
            )
        )

        val result = AmendFeEntityConverter.fromEntity(updatedFE);

        tenderProcessDao.save(
            TenderProcessEntity(
                cpId = cpid,
                token = entity.token,
                stage = stage,
                owner = entity.owner,
                createdDate = context.startDate,
                jsonData = toJson(updatedFE)
            )
        )

        return result
    }

    private fun validateStatus(status: TenderStatus) =
        when (status) {
            TenderStatus.ACTIVE -> Unit

            TenderStatus.CANCELLED,
            TenderStatus.COMPLETE,
            TenderStatus.PLANNED,
            TenderStatus.PLANNING,
            TenderStatus.UNSUCCESSFUL ->
                throw ErrorException(
                    error = ErrorType.INVALID_TENDER_STATUS,
                    message = "Framework Establishment can not be updated"
                )
        }

    private fun FEEntity.Tender.ProcuringEntity.updatePersons(
        persons: List<AmendFEData.Tender.ProcuringEntity.Person>
    ): FEEntity.Tender.ProcuringEntity {
        val receivedPersonsById = persons.associateBy { it.identifier.id }
        val savedPersonsById = this.persons.associateBy { it.identifier.id }

        val receivedPersonsIds = receivedPersonsById.keys
        val savedPersonsIds = savedPersonsById.keys

        val idsAllPersons = receivedPersonsIds.union(savedPersonsIds)
        val idsNewPersons = getNewElements(receivedPersonsIds, savedPersonsIds)
        val idsUpdatePersons = getElementsForUpdate(receivedPersonsIds, savedPersonsIds)
        val idsOldPersons = getMissingElements(receivedPersonsIds, savedPersonsIds)

        val updatedPersons = idsAllPersons
            .map { id ->
                when (id) {
                    in idsNewPersons -> createPerson(receivedPersonsById.getValue(id))
                    in idsUpdatePersons -> savedPersonsById.getValue(id).update(receivedPersonsById.getValue(id))
                    in idsOldPersons -> savedPersonsById.getValue(id)
                    else -> throw IllegalStateException()
                }
            }

        return this.copy(persons = updatedPersons)
    }

    private fun createPerson(person: AmendFEData.Tender.ProcuringEntity.Person): FEEntity.Tender.ProcuringEntity.Person =
        FEEntity.Tender.ProcuringEntity.Person(
            id = PersonId.generate(scheme = person.identifier.scheme, id = person.identifier.id).toString(),
            title = person.title,
            name = person.name,
            identifier = createIdentifier(person.identifier),
            businessFunctions = person.businessFunctions.map { createBusinessFunction(it) }
        )

    private fun createIdentifier(identifier: AmendFEData.Tender.ProcuringEntity.Person.Identifier): FEEntity.Tender.ProcuringEntity.Person.Identifier =
        FEEntity.Tender.ProcuringEntity.Person.Identifier(
            scheme = identifier.scheme,
            id = identifier.id,
            uri = identifier.uri
        )

    private fun FEEntity.Tender.ProcuringEntity.Person.update(person: AmendFEData.Tender.ProcuringEntity.Person) =
        this.copy(
            title = person.title,
            name = person.name,
            businessFunctions = updateBusinessFunctions(person.businessFunctions, this.businessFunctions)
        )

    private fun updateBusinessFunctions(
        receivedBusinessFunctions: List<AmendFEData.Tender.ProcuringEntity.Person.BusinessFunction>,
        savedBusinessFunctions: List<FEEntity.Tender.ProcuringEntity.Person.BusinessFunction>
    ): List<FEEntity.Tender.ProcuringEntity.Person.BusinessFunction> {
        val receivedBusinessFunctionsByIds = receivedBusinessFunctions.associateBy { it.id }
        val savedBusinessFunctionsByIds = savedBusinessFunctions.associateBy { it.id }

        val receivedBusinessFunctionsIds = receivedBusinessFunctionsByIds.keys
        val savedBusinessFunctionsIds = savedBusinessFunctionsByIds.keys

        val idsAllPersons = receivedBusinessFunctionsIds.union(savedBusinessFunctionsIds)
        val idsNewBusinessFunctions = getNewElements(receivedBusinessFunctionsIds, savedBusinessFunctionsIds)
        val idsUpdateBusinessFunctions = getElementsForUpdate(receivedBusinessFunctionsIds, savedBusinessFunctionsIds)

        return idsAllPersons
            .map { id ->
                when (id) {
                    in idsNewBusinessFunctions -> createBusinessFunction(receivedBusinessFunctionsByIds.getValue(id))
                    in idsUpdateBusinessFunctions ->
                        savedBusinessFunctionsByIds.getValue(id)
                            .update(receivedBusinessFunctionsByIds.getValue(id))
                    else -> savedBusinessFunctionsByIds.getValue(id)
                }
            }
    }

    private fun createBusinessFunction(businessFunction: AmendFEData.Tender.ProcuringEntity.Person.BusinessFunction) =
        FEEntity.Tender.ProcuringEntity.Person.BusinessFunction(
            id = businessFunction.id,
            type = businessFunction.type,
            jobTitle = businessFunction.jobTitle,
            period = businessFunction.period.convert(),
            documents = businessFunction.documents.map { it.convert() }
        )

    private fun AmendFEData.Tender.ProcuringEntity.Person.BusinessFunction.Period.convert() =
        FEEntity.Tender.ProcuringEntity.Person.BusinessFunction.Period(startDate = this.startDate)

    private fun AmendFEData.Tender.ProcuringEntity.Person.BusinessFunction.Document.convert() =
        FEEntity.Tender.ProcuringEntity.Person.BusinessFunction.Document(
            documentType = this.documentType,
            id = this.id,
            title = this.title,
            description = this.description
        )

    private fun FEEntity.Tender.ProcuringEntity.Person.BusinessFunction.update(
        businessFunction: AmendFEData.Tender.ProcuringEntity.Person.BusinessFunction
    ) = this.copy(
        type = businessFunction.type,
        jobTitle = businessFunction.jobTitle,
        period = this.period.copy(startDate = businessFunction.period.startDate),
        documents = updateBusinessFunctionDocuments(businessFunction.documents, this.documents ?: emptyList())
    )

    private fun updateBusinessFunctionDocuments(
        receivedDocuments: List<AmendFEData.Tender.ProcuringEntity.Person.BusinessFunction.Document>,
        savedDocuments: List<FEEntity.Tender.ProcuringEntity.Person.BusinessFunction.Document>
    ): List<FEEntity.Tender.ProcuringEntity.Person.BusinessFunction.Document> {
        val receivedDocumentsByIds = receivedDocuments.associateBy { it.id }
        val savedDocumentsByIds = savedDocuments.associateBy { it.id }

        val receivedBusinessFunctionsIds = receivedDocumentsByIds.keys
        val savedBusinessFunctionsIds = savedDocumentsByIds.keys

        val idsAllPersons = receivedBusinessFunctionsIds.union(savedBusinessFunctionsIds)
        val idsNewBusinessFunctions = getNewElements(receivedBusinessFunctionsIds, savedBusinessFunctionsIds)
        val idsUpdateBusinessFunctions = getElementsForUpdate(receivedBusinessFunctionsIds, savedBusinessFunctionsIds)

        return idsAllPersons
            .map { id ->
                when (id) {
                    in idsNewBusinessFunctions -> createBusinessFunctionDocument(receivedDocumentsByIds.getValue(id))
                    in idsUpdateBusinessFunctions ->
                        savedDocumentsByIds.getValue(id)
                            .update(receivedDocumentsByIds.getValue(id))
                    else -> savedDocumentsByIds.getValue(id)
                }
            }
    }

    private fun createBusinessFunctionDocument(
        document: AmendFEData.Tender.ProcuringEntity.Person.BusinessFunction.Document
    ) = FEEntity.Tender.ProcuringEntity.Person.BusinessFunction.Document(
        documentType = document.documentType,
        id = document.id,
        title = document.title,
        description = document.description
    )

    private fun FEEntity.Tender.ProcuringEntity.Person.BusinessFunction.Document.update(
        document: AmendFEData.Tender.ProcuringEntity.Person.BusinessFunction.Document
    ) = this.copy(
        title = document.title,
        description = document.description.takeIfNotNullOrDefault(this.description)
    )

    private fun List<FEEntity.Tender.Document>.updateTenderDocuments(documentsFromRequest: List<AmendFEData.Tender.Document>): List<FEEntity.Tender.Document> {
        val documentsById = this.associateBy { it.id }
        val documentsFromRequestById = documentsFromRequest.associateBy { it.id }

        val allDocumentsIds = documentsById.keys + documentsFromRequestById.keys
        return allDocumentsIds.map { id ->
            documentsFromRequestById[id]
                ?.let { document ->
                    documentsById[id]
                        ?.copy(
                            title = document.title,
                            description = document.description
                        )
                        ?: FEEntity.Tender.Document(
                            id = document.id,
                            documentType = DocumentType.creator(document.documentType.key),
                            title = document.title,
                            description = document.description
                        )
                }
                ?: documentsById.getValue(id)
        }
    }
}
