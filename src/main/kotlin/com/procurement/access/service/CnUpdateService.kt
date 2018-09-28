package com.procurement.access.service

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType.*
import com.procurement.access.model.bpe.CommandMessage
import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.cn.*
import com.procurement.access.model.dto.ocds.*
import com.procurement.access.model.dto.ocds.TenderStatusDetails.SUSPENDED
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toDate
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toLocal
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service
import java.math.RoundingMode
import java.time.LocalDateTime

interface CnUpdateService {

    fun updateCn(cm: CommandMessage): ResponseDto
}

@Service
class CnUpdateServiceImpl(private val generationService: GenerationService,
                          private val tenderProcessDao: TenderProcessDao) : CnUpdateService {

    override fun updateCn(cm: CommandMessage): ResponseDto {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val token = cm.context.token ?: throw ErrorException(CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)
        val owner = cm.context.owner ?: throw ErrorException(CONTEXT)
        val dateTime = cm.context.startDate?.toLocal() ?: throw ErrorException(CONTEXT)
        val cnDto = toObject(CnUpdate::class.java, cm.data).validate()

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage)
                ?: throw ErrorException(DATA_NOT_FOUND)
        if (entity.owner != owner) throw ErrorException(INVALID_OWNER)
        if (entity.token.toString() != token) throw ErrorException(INVALID_TOKEN)
        val tenderProcess = toObject(TenderProcess::class.java, entity.jsonData)
        validateTenderStatus(tenderProcess)

        val activeLots: List<Lot>
        val canceledLots: List<Lot>
        val updatedItems: List<Item>
        val updatedDocuments: List<Document>
        val lotsDto = cnDto.tender.lots
        val itemsDto = cnDto.tender.items
        val documentsDto = cnDto.tender.documents
        checkLotsCurrency(lotsDto, tenderProcess.tender.value.currency)
        checkLotsContractPeriod(cnDto)
        val lotsDtoId = cnDto.tender.lots.asSequence().map { it.id }.toSet()
        if (lotsDtoId.size < cnDto.tender.lots.size) throw ErrorException(INVALID_LOT_ID)
        val lotsDbId = tenderProcess.tender.lots.asSequence().map { it.id }.toSet()
        var newLotsId = lotsDtoId - lotsDbId
        val canceledLotsId = lotsDbId - lotsDtoId
        newLotsId = getNewLotsIdAndSetItemsAndDocumentsRelatedLots(cnDto.tender, newLotsId)
        validateRelatedLots(lotIds = lotsDbId + newLotsId, items = itemsDto, documents = documentsDto)
        /*active Lots*/
        activeLots = getActiveLots(lotsDto = cnDto.tender.lots, lotsTender = tenderProcess.tender.lots, newLotsId = newLotsId)
        setContractPeriod(tenderProcess.tender, activeLots, tenderProcess.planning.budget)
        setTenderValueByActiveLots(tenderProcess.tender, activeLots)
        /*canceled Lots*/
        canceledLots = getCanceledLots(tenderProcess.tender.lots, canceledLotsId)
        /*update Items*/
        updatedItems = updateItems(tenderProcess.tender.items, itemsDto)
        /*update Documents*/
        updatedDocuments = updateDocuments(tenderProcess.tender.documents, documentsDto)
        /*tenderProcess*/
        tenderProcess.planning.apply {
            rationale = cnDto.planning.rationale
            budget.description = cnDto.planning.budget.description
        }
        tenderProcess.tender.apply {
            title = cnDto.tender.title
            description = cnDto.tender.description
            procurementMethodRationale = cnDto.tender.procurementMethodRationale
            procurementMethodAdditionalInfo = cnDto.tender.procurementMethodAdditionalInfo
            items = updatedItems
            lots = activeLots + canceledLots
            documents = updatedDocuments
            tenderPeriod = cnDto.tender.tenderPeriod
            enquiryPeriod = cnDto.tender.enquiryPeriod
        }

        tenderProcessDao.save(getEntity(tenderProcess, entity, dateTime))
        return ResponseDto(data = tenderProcess)
    }

    private fun checkLotsCurrency(lotsDto: List<LotCnUpdate>, budgetCurrency: String) {
        lotsDto.asSequence().firstOrNull { it.value.currency != budgetCurrency }?.let {
            throw ErrorException(INVALID_LOT_CURRENCY)
        }
    }

    private fun validateTenderStatus(tenderProcess: TenderProcess) {
        if (tenderProcess.tender.statusDetails == SUSPENDED) throw ErrorException(IS_SUSPENDED)
    }

    private fun setContractPeriod(tender: Tender, activeLots: List<Lot>, budget: Budget) {
        val startDate: LocalDateTime = activeLots.asSequence().minBy { it.contractPeriod.startDate }?.contractPeriod?.startDate!!
        val endDate: LocalDateTime = activeLots.asSequence().maxBy { it.contractPeriod.endDate }?.contractPeriod?.endDate!!
        budget.budgetBreakdown.forEach { bb ->
            if (startDate > bb.period.endDate) throw ErrorException(INVALID_LOT_CONTRACT_PERIOD)
            if (endDate < bb.period.startDate) throw ErrorException(INVALID_LOT_CONTRACT_PERIOD)
        }
        tender.contractPeriod = ContractPeriod(startDate, endDate)
    }

    private fun setTenderValueByActiveLots(tender: Tender, activeLots: List<Lot>) {
        val totalAmount = activeLots.asSequence()
                .sumByDouble { it.value.amount.toDouble() }
                .toBigDecimal().setScale(2, RoundingMode.HALF_UP)
        if (totalAmount > tender.value.amount) throw ErrorException(INVALID_LOT_AMOUNT)
        tender.value.amount = totalAmount
    }

    private fun getActiveLots(lotsDto: List<LotCnUpdate>, lotsTender: List<Lot> = listOf(), newLotsId: Set<String>): List<Lot> {
        val activeLots = mutableListOf<Lot>()
        lotsDto.forEach { lotDto ->
            if (lotDto.id in newLotsId) {
                activeLots.add(convertDtoLotToLot(lotDto))
            } else {
                val updatableTenderLot = lotsTender.asSequence().first { it.id == lotDto.id }
                updatableTenderLot.updateLot(lotDto)
                activeLots.add(updatableTenderLot)
            }
        }
        return activeLots
    }

    private fun getCanceledLots(lotsTender: List<Lot>, canceledLotsId: Set<String>): List<Lot> {
        val canceledLots = mutableListOf<Lot>()
        lotsTender.asSequence()
                .filter { it.id in canceledLotsId }
                .forEach { lot ->
                    lot.status = LotStatus.CANCELLED
                    lot.statusDetails = LotStatusDetails.EMPTY
                    canceledLots.add(lot)
                }
        return canceledLots
    }

    private fun checkLotsContractPeriod(cn: CnUpdate) {
        cn.tender.lots.forEach { lot ->
            if (lot.contractPeriod.startDate >= lot.contractPeriod.endDate) {
                throw ErrorException(INVALID_LOT_CONTRACT_PERIOD)
            }
            if (lot.contractPeriod.startDate <= cn.tender.tenderPeriod.endDate) {
                throw ErrorException(INVALID_LOT_CONTRACT_PERIOD)
            }
        }
    }

    private fun getNewLotsIdAndSetItemsAndDocumentsRelatedLots(tender: TenderCnUpdate, newLotsId: Set<String>): Set<String> {
        val newLotsIdSet = mutableSetOf<String>()
        tender.lots.asSequence()
                .filter { it.id in newLotsId }
                .forEach { lot ->
                    val id = generationService.getTimeBasedUUID()
                    tender.items.asSequence()
                            .filter { it.relatedLot == lot.id }
                            .forEach { it.relatedLot = id }
                    tender.documents.asSequence()
                            .filter { it.relatedLots != null }
                            .forEach { document ->
                                if (document.relatedLots!!.contains(lot.id)) {
                                    document.relatedLots!!.remove(lot.id)
                                    document.relatedLots!!.add(id)
                                }
                            }
                    lot.id = id
                    newLotsIdSet.add(id)
                }
        return newLotsIdSet
    }

    private fun validateRelatedLots(lotIds: Set<String>, items: List<ItemCnUpdate>, documents: List<Document>) {
        val lotsFromItems = items.asSequence().map { it.relatedLot }.toHashSet()
        if (!lotIds.containsAll(lotsFromItems)) throw ErrorException(INVALID_ITEMS_RELATED_LOTS)
        val lotsFromDocuments = documents.asSequence()
                .filter { it.relatedLots != null }
                .flatMap { it.relatedLots!!.asSequence() }
                .toHashSet()
        if (lotsFromDocuments.isNotEmpty()) {
            if (!lotIds.containsAll(lotsFromDocuments)) throw ErrorException(INVALID_DOCS_RELATED_LOTS)
        }
    }

    private fun convertDtoLotToLot(lotDto: LotCnUpdate): Lot {
        return Lot(
                id = lotDto.id,
                title = lotDto.title,
                description = lotDto.description,
                status = LotStatus.ACTIVE,
                statusDetails = LotStatusDetails.EMPTY,
                value = lotDto.value,
                options = listOf(Option(false)),
                recurrentProcurement = listOf(RecurrentProcurement(false)),
                renewals = listOf(Renewal(false)),
                variants = listOf(Variant(false)),
                contractPeriod = lotDto.contractPeriod,
                placeOfPerformance = lotDto.placeOfPerformance
        )
    }

    private fun Lot.updateLot(lotDto: LotCnUpdate) {
        this.title = lotDto.title
        this.description = lotDto.description
        this.contractPeriod = lotDto.contractPeriod
        this.placeOfPerformance = lotDto.placeOfPerformance
    }

    private fun updateItems(itemsTender: List<Item>, itemsDto: List<ItemCnUpdate>): List<Item> {
        //validation
        val itemsDtoId = itemsDto.asSequence().map { it.id }.toSet()
        if (itemsDtoId.size < itemsDto.size) throw ErrorException(INVALID_ITEMS)
        val itemsDbId = itemsTender.asSequence().map { it.id }.toSet()
        if (!itemsDtoId.containsAll(itemsDbId)) throw ErrorException(INVALID_ITEMS)
        //update
        itemsTender.forEach { item ->
            val itemDto = itemsDto.asSequence().first { it.id == item.id }
            item.updateItem(itemDto)
        }
        return itemsTender
    }

    private fun Item.updateItem(itemDto: ItemCnUpdate) {
        this.description = itemDto.description
        this.relatedLot = itemDto.relatedLot
    }

    private fun updateDocuments(documentsTender: List<Document>?, documentsDto: List<Document>): List<Document> {
        return if (documentsTender != null && documentsTender.isNotEmpty()) {
            //validation
            val documentsDtoId = documentsDto.asSequence().map { it.id }.toSet()
            val documentsDbId = documentsTender.asSequence().map { it.id }.toSet()
            val newDocumentsId = documentsDtoId - documentsDbId
            if (!documentsDtoId.containsAll(documentsDbId)) throw ErrorException(INVALID_DOCS_ID)
            //update
            documentsTender.forEach { document ->
                val documentDto = documentsDto.asSequence().first { it.id == document.id }
                document.updateDocument(documentDto)
            }
            val newDocuments = documentsDto.asSequence().filter { it.id in newDocumentsId }.toList()
            documentsTender + newDocuments
        } else {
            documentsDto
        }
    }

    private fun Document.updateDocument(documentDto: Document) {
        this.title = documentDto.title
        this.description = documentDto.description
        this.relatedLots = documentDto.relatedLots
    }

    private fun getEntity(tp: TenderProcess,
                          entity: TenderProcessEntity,
                          dateTime: LocalDateTime): TenderProcessEntity {
        return TenderProcessEntity(
                cpId = entity.cpId,
                token = entity.token,
                stage = entity.stage,
                owner = entity.owner,
                createdDate = dateTime.toDate(),
                jsonData = toJson(tp)
        )
    }
}
