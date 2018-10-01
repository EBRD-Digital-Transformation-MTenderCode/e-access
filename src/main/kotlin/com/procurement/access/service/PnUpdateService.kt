package com.procurement.access.service

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType.*
import com.procurement.access.model.bpe.CommandMessage
import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.ocds.*
import com.procurement.access.model.dto.pn.*
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toDate
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toLocal
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service
import java.math.RoundingMode
import java.time.LocalDateTime

interface PnUpdateService {

    fun updatePn(cm: CommandMessage): ResponseDto
}

@Service
class PnUpdateServiceImpl(private val generationService: GenerationService,
                          private val tenderProcessDao: TenderProcessDao) : PnUpdateService {


    override fun updatePn(cm: CommandMessage): ResponseDto {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)
        val token = cm.context.token ?: throw ErrorException(CONTEXT)
        val owner = cm.context.owner ?: throw ErrorException(CONTEXT)
        val dateTime = cm.context.startDate?.toLocal() ?: throw ErrorException(CONTEXT)
        val pnDto = toObject(PnUpdate::class.java, cm.data).validate()

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(DATA_NOT_FOUND)
        if (entity.owner != owner) throw ErrorException(INVALID_OWNER)
        if (entity.token.toString() != token) throw ErrorException(INVALID_TOKEN)
        val tenderProcess = toObject(TenderProcess::class.java, entity.jsonData)
        validateStartDate(pnDto.tender.tenderPeriod.startDate)
        var activeLots: List<Lot> = listOf()
        var canceledLots: List<Lot> = listOf()
        var updatedLots: List<Lot>
        var updatedItems: List<Item> = listOf()
        var updatedDocuments: List<Document>
        /*first insert*/
        if (tenderProcess.tender.lots.isEmpty() && pnDto.tender.lots != null) {
            val lotsDto = pnDto.tender.lots
            val itemsDto = pnDto.tender.items!!
            checkLotsCurrency(lotsDto, tenderProcess.tender.value.currency)
            checkLotsContractPeriod(lotsDto, pnDto.tender.tenderPeriod.startDate)
            val lotsId = lotsDto.asSequence().map { it.id }.toHashSet()
            if (lotsId.size < lotsDto.size) throw ErrorException(INVALID_LOT_ID)
            val lotsFromItems = itemsDto.asSequence().map { it.relatedLot }.toHashSet()
            if (!lotsFromItems.containsAll(lotsId)) throw ErrorException(INVALID_ITEMS_RELATED_LOTS)
            if (!lotsId.containsAll(lotsFromItems)) throw ErrorException(INVALID_ITEMS_RELATED_LOTS)
            val newLotsId = getLotsIdAndItemsAndDocumentsRelatedLots(pnDto.tender)
            activeLots = getActiveLots(lotsDto = pnDto.tender.lots, newLotsId = newLotsId)
            itemsDto.forEach { it.id = generationService.getTimeBasedUUID() }
            updatedItems = convertItems(itemsDto)
        }
        /*update*/
        if (tenderProcess.tender.lots.isNotEmpty() && pnDto.tender.lots != null) {
            val lotsDto = pnDto.tender.lots
            val itemsDto = pnDto.tender.items!!
            checkLotsCurrency(lotsDto, tenderProcess.tender.value.currency)
            checkLotsContractPeriod(lotsDto, pnDto.tender.tenderPeriod.startDate)
            val lotsDtoId = lotsDto.asSequence().map { it.id }.toSet()
            val lotsDbId = tenderProcess.tender.lots.asSequence().map { it.id }.toSet()
            var newLotsId = lotsDtoId - lotsDbId
            val canceledLotsId = lotsDbId - lotsDtoId
            val lotsFromItems = itemsDto.asSequence().map { it.relatedLot }.toHashSet()
            if (!lotsFromItems.containsAll(lotsDtoId)) throw ErrorException(INVALID_ITEMS_RELATED_LOTS) //is all active lots have related items
            if (!(lotsDbId + newLotsId).containsAll(lotsFromItems)) throw ErrorException(INVALID_ITEMS_RELATED_LOTS) //is all items have valid related lots
            newLotsId = getNewLotsIdAndSetItemsAndDocumentsRelatedLots(pnDto.tender, newLotsId)
            activeLots = getActiveLots(pnDto.tender.lots, tenderProcess.tender.lots, newLotsId)
            canceledLots = getCanceledLots(tenderProcess.tender.lots, canceledLotsId)
            updatedItems = updateItems(tenderProcess.tender.items, itemsDto)
        }
        if (activeLots.isNotEmpty()) {
            setContractPeriod(tenderProcess.tender, activeLots, tenderProcess.planning.budget)
        }
        setValueByActiveLots(tenderProcess.tender, activeLots)
        tenderProcess.planning.apply {
            rationale = pnDto.planning.rationale
            budget.description = pnDto.planning.budget.description
        }
        tenderProcess.tender.apply {
            title = pnDto.tender.title
            description = pnDto.tender.description
            procurementMethodRationale = pnDto.tender.procurementMethodRationale
            procurementMethodAdditionalInfo = pnDto.tender.procurementMethodAdditionalInfo
            pnDto.tender.classification?.let { classification = it }
            tenderPeriod = Period(pnDto.tender.tenderPeriod.startDate, null)
        }
        if (updatedItems.isNotEmpty()) {
            tenderProcess.tender.items = updatedItems
        }
        updatedLots = activeLots + canceledLots
        if (updatedLots.isNotEmpty()) {
            tenderProcess.tender.lots = updatedLots
        }
        updatedDocuments = updateDocuments(tenderProcess.tender, pnDto.tender.documents)
        if (updatedDocuments.isNotEmpty()) {
            tenderProcess.tender.documents = updatedDocuments
        }
        tenderProcessDao.save(getEntity(tenderProcess, entity, dateTime))
        return ResponseDto(data = tenderProcess)
    }


    private fun validateStartDate(startDate: LocalDateTime) {
        val month = startDate.month
        if (month != month.firstMonthOfQuarter()) throw ErrorException(INVALID_START_DATE)
        val day = startDate.dayOfMonth
        if (day != 1) throw ErrorException(INVALID_START_DATE)
    }

    private fun checkLotsCurrency(lotsDto: List<LotPnUpdate>, budgetCurrency: String) {
        lotsDto.asSequence().firstOrNull { it.value.currency != budgetCurrency }?.let {
            throw ErrorException(INVALID_LOT_CURRENCY)
        }
    }

    private fun checkLotsContractPeriod(lotsDto: List<LotPnUpdate>, tenderPeriodStartDate: LocalDateTime) {
        val contractPeriodSet = lotsDto.asSequence()
                .filter { it.contractPeriod != null }
                .mapNotNull { it.contractPeriod }.toSet()
        contractPeriodSet.forEach {
            if (it.startDate >= it.endDate) throw ErrorException(INVALID_LOT_CONTRACT_PERIOD)
            if (it.startDate < tenderPeriodStartDate) throw ErrorException(INVALID_LOT_CONTRACT_PERIOD)
        }
    }

    private fun getLotsIdAndItemsAndDocumentsRelatedLots(tender: TenderPnUpdate): Set<String> {
        val newLotsIdSet = mutableSetOf<String>()
        tender.lots?.let { lots ->
            lots.forEach { lot ->
                val id = generationService.getTimeBasedUUID()
                tender.items?.let { items ->
                    items.asSequence()
                            .filter { it.relatedLot == lot.id }
                            .forEach { it.relatedLot = id }
                }
                tender.documents?.let { documents ->
                    documents.asSequence()
                            .filter { it.relatedLots != null }
                            .forEach { document ->
                                if (document.relatedLots!!.contains(lot.id)) {
                                    document.relatedLots!!.remove(lot.id)
                                    document.relatedLots!!.add(id)
                                }
                            }
                }
                lot.id = id
                newLotsIdSet.add(id)
            }
        }
        return newLotsIdSet
    }

    private fun getNewLotsIdAndSetItemsAndDocumentsRelatedLots(tender: TenderPnUpdate, newLotsId: Set<String>): Set<String> {
        val newLotsIdSet = mutableSetOf<String>()
        tender.lots?.let { lots ->
            lots.filter { it.id in newLotsId }.forEach { lot ->
                val id = generationService.getTimeBasedUUID()
                tender.items?.let { items ->
                    items.asSequence()
                            .filter { it.relatedLot == lot.id }
                            .forEach { it.relatedLot = id }
                }
                tender.documents?.let { documents ->
                    documents.asSequence()
                            .filter { it.relatedLots != null }
                            .forEach { document ->
                                if (document.relatedLots!!.contains(lot.id)) {
                                    document.relatedLots!!.remove(lot.id)
                                    document.relatedLots!!.add(id)
                                }
                            }
                }
                lot.id = id
                newLotsIdSet.add(id)
            }
        }
        return newLotsIdSet
    }

    private fun getActiveLots(lotsDto: List<LotPnUpdate>, lotsTender: List<Lot> = listOf(), newLotsId: Set<String>): List<Lot> {
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

    private fun convertItems(itemsDto: List<ItemPnUpdate>): List<Item> {
        return itemsDto.asSequence().map { convertDtoItemToItem(it) }.toList()
    }

    private fun updateItems(itemsTender: List<Item>, itemsDto: List<ItemPnUpdate>): List<Item> {
        //validation
        val itemsDtoId = itemsDto.asSequence().map { it.id }.toHashSet()
        if (itemsDtoId.size < itemsDto.size) throw ErrorException(INVALID_ITEMS)
        val itemsDbId = itemsTender.asSequence().map { it.id }.toHashSet()
        if (itemsDtoId.size != itemsDbId.size) throw ErrorException(INVALID_ITEMS)
        if (!itemsDbId.containsAll(itemsDtoId)) throw ErrorException(INVALID_ITEMS)
        //update
        itemsTender.forEach { itemDb -> itemDb.updateItem(itemsDto.first { it.id == itemDb.id }) }
        return itemsTender
    }

    private fun updateDocuments(tender: Tender, documentsDto: List<Document>?): List<Document> {
        if (documentsDto != null && documentsDto.isNotEmpty()) {
            validateDocumentsRelatedLots(tender.lots, documentsDto)
            return if (tender.documents != null && tender.documents!!.isNotEmpty()) {
                val documentsDb = tender.documents!!
                //validation
                val documentsDtoId = documentsDto.asSequence().map { it.id }.toSet()
                val documentsDbId = documentsDb.asSequence().map { it.id }.toSet()
                if (!documentsDtoId.containsAll(documentsDbId)) throw ErrorException(INVALID_DOCS_ID)
                //update
                documentsDb.forEach { docDb -> docDb.updateDocument(documentsDto.first { it.id == docDb.id }) }
                val newDocumentsId = documentsDtoId - documentsDbId
                val newDocuments = documentsDto.asSequence().filter { it.id in newDocumentsId }.toList()
                documentsDb + newDocuments
            } else {
                documentsDto
            }
        }
        return listOf()
    }

    private fun validateDocumentsRelatedLots(lots: List<Lot>, documentsDto: List<Document>) {
        val lotsId = lots.asSequence().map { it.id }.toHashSet()
        val lotsFromDocuments = documentsDto.asSequence()
                .filter { it.relatedLots != null }.flatMap { it.relatedLots!!.asSequence() }.toHashSet()
        if (lotsFromDocuments.isNotEmpty()) {
            if (!lotsId.containsAll(lotsFromDocuments)) throw ErrorException(INVALID_DOCS_RELATED_LOTS)
        }
    }

    private fun Document.updateDocument(documentDto: Document) {
        this.title = documentDto.title
        this.description = documentDto.description
        this.relatedLots = documentDto.relatedLots
    }

    private fun setContractPeriod(tender: Tender, activeLots: List<Lot>, budget: Budget) {
        val startDate: LocalDateTime = activeLots.asSequence().asSequence()
                .filter { it.contractPeriod != null }
                .minBy { it.contractPeriod!!.startDate }
                ?.contractPeriod!!.startDate
        val endDate: LocalDateTime = activeLots.asSequence()
                .filter { it.contractPeriod != null }
                .maxBy { it.contractPeriod!!.endDate }
                ?.contractPeriod!!.endDate
        budget.budgetBreakdown.forEach { bb ->
            if (startDate > bb.period.endDate) throw ErrorException(INVALID_LOT_CONTRACT_PERIOD)
            if (endDate < bb.period.startDate) throw ErrorException(INVALID_LOT_CONTRACT_PERIOD)
        }
        tender.contractPeriod = ContractPeriod(startDate, endDate)
    }

    private fun setValueByActiveLots(tender: Tender, activeLots: List<Lot>) {
        if (activeLots.isNotEmpty()) {
            val totalAmount = activeLots.asSequence()
                    .sumByDouble { it.value.amount.toDouble() }
                    .toBigDecimal().setScale(2, RoundingMode.HALF_UP)
            if (totalAmount > tender.value.amount) throw ErrorException(INVALID_LOT_AMOUNT)
            tender.value.amount = totalAmount
        }
    }

    private fun convertDtoLotToLot(lotDto: LotPnUpdate): Lot {
        return Lot(
                id = lotDto.id,
                title = lotDto.title,
                description = lotDto.description,
                status = LotStatus.PLANNING,
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

    private fun Lot.updateLot(lotDto: LotPnUpdate) {
        this.title = lotDto.title
        this.description = lotDto.description
        this.contractPeriod = lotDto.contractPeriod
        this.placeOfPerformance = lotDto.placeOfPerformance
    }

    private fun convertDtoItemToItem(itemDto: ItemPnUpdate): Item {
        return Item(
                id = itemDto.id,
                description = itemDto.description,
                classification = itemDto.classification,
                additionalClassifications = itemDto.additionalClassifications,
                quantity = itemDto.quantity,
                unit = itemDto.unit,
                relatedLot = itemDto.relatedLot
        )
    }

    private fun Item.updateItem(itemDto: ItemPnUpdate) {
        this.description = itemDto.description
        this.relatedLot = itemDto.relatedLot
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
