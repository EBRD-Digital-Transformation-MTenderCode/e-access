package com.procurement.access.service

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.ocds.*
import com.procurement.access.model.dto.ocds.TenderStatus.PLANNING
import com.procurement.access.model.dto.ocds.TenderStatusDetails.EMPTY
import com.procurement.access.model.dto.pn.ItemPnUpdate
import com.procurement.access.model.dto.pn.LotPnUpdate
import com.procurement.access.model.dto.pn.PnUpdate
import com.procurement.access.model.dto.pn.TenderPnUpdate
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toDate
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service
import java.math.RoundingMode
import java.time.LocalDateTime

interface PnUpdateService {

    fun updatePn(
            cpId: String,
            stage: String,
            owner: String,
            token: String,
            dateTime: LocalDateTime,
            pnDto: PnUpdate): ResponseDto
}

@Service
class PnUpdateServiceImpl(private val generationService: GenerationService,
                          private val tenderProcessDao: TenderProcessDao) : PnUpdateService {


    override fun updatePn(cpId: String,
                          stage: String,
                          owner: String,
                          token: String,
                          dateTime: LocalDateTime,
                          pnDto: PnUpdate): ResponseDto {

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage)
                ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        if (entity.owner != owner) throw ErrorException(ErrorType.INVALID_OWNER)
        if (entity.token.toString() != token) throw ErrorException(ErrorType.INVALID_TOKEN)
        val tenderProcess = toObject(TenderProcess::class.java, entity.jsonData)
        validateStartDate(pnDto.tender.tenderPeriod.startDate)
        var activeLots: List<Lot> = listOf()
        var canceledLots: List<Lot> = listOf()
        var updatedItems: List<Item> = listOf()
        /*first insert*/
        if (tenderProcess.tender.lots.isEmpty() && pnDto.tender.lots != null) {
            val lotsDto = pnDto.tender.lots
            val itemsDto = pnDto.tender.items ?: throw ErrorException(ErrorType.INVALID_ITEMS)
            val documentsDto = pnDto.tender.documents
            val lotsIds = lotsDto.asSequence().map { it.id }.toHashSet()
            checkLotsCurrency(lotsDto, tenderProcess.tender.value.currency)
            checkLotsContractPeriod(lotsDto, pnDto.tender.tenderPeriod.startDate)
            validateRelatedLots(lotIds = lotsIds, items = itemsDto, documents = documentsDto)
            setLotsIdAndItemsAndDocumentsRelatedLots(pnDto.tender)
            val lotsDtoId = pnDto.tender.lots.asSequence().map { it.id }.toSet()
            /*activeLots*/
            activeLots = getActiveLots(lotsDto = pnDto.tender.lots, newLotsId = lotsDtoId)
            /*updatedItems*/
            itemsDto.asSequence().forEach { it.id = generationService.getTimeBasedUUID() }
            updatedItems = convertItems(itemsDto)
        }
        /*update*/
        if (tenderProcess.tender.lots.isNotEmpty() && pnDto.tender.lots != null) {
            val lotsDto = pnDto.tender.lots
            val itemsDto = pnDto.tender.items ?: throw ErrorException(ErrorType.INVALID_ITEMS)
            val documentsDto = pnDto.tender.documents
            checkLotsCurrency(lotsDto, tenderProcess.tender.value.currency)
            checkLotsContractPeriod(lotsDto, pnDto.tender.tenderPeriod.startDate)
            val lotsDtoId = pnDto.tender.lots.asSequence().map { it.id }.toSet()
            val lotsDbId = tenderProcess.tender.lots.asSequence().map { it.id }.toSet()
            var newLotsId = lotsDtoId - lotsDbId
            val canceledLotsId = lotsDbId - lotsDtoId
            newLotsId = getNewLotsIdAndSetItemsAndDocumentsRelatedLots(pnDto.tender, newLotsId)
            /*activeLots*/
            activeLots = getActiveLots(lotsDto = pnDto.tender.lots, lotsTender = tenderProcess.tender.lots, newLotsId = newLotsId)
            val activeLotsIds = activeLots.asSequence().map { it.id }.toSet()
            validateRelatedLots(lotIds = activeLotsIds, items = itemsDto, documents = documentsDto)
            setContractPeriod(tenderProcess.tender, activeLots, tenderProcess.planning.budget)
            setTenderValueByActiveLots(tenderProcess.tender, activeLots)
            /*canceledLots*/
            canceledLots = getCanceledLots(tenderProcess.tender.lots, canceledLotsId)
            /*updatedItems*/
            updatedItems = updateItems(tenderProcess.tender.items, itemsDto)
        }
        tenderProcess.planning.apply {
            rationale = pnDto.planning.rationale
            budget.description = pnDto.planning.budget.description
        }
        tenderProcess.tender.apply {
            title = pnDto.tender.title
            description = pnDto.tender.description
            procurementMethodRationale = pnDto.tender.procurementMethodRationale
            procurementMethodAdditionalInfo = pnDto.tender.procurementMethodAdditionalInfo
            items = updatedItems
            lots = activeLots + canceledLots
            documents = pnDto.tender.documents
            tenderPeriod = Period(pnDto.tender.tenderPeriod.startDate, null)
        }

        tenderProcessDao.save(getEntity(tenderProcess, entity, dateTime))
        return ResponseDto(data = tenderProcess)
    }

    private fun validateStartDate(startDate: LocalDateTime) {
        val month = startDate.month
        if (month != month.firstMonthOfQuarter()) throw ErrorException(ErrorType.INVALID_START_DATE)
        val day = startDate.dayOfMonth
        if (day != 1) throw ErrorException(ErrorType.INVALID_START_DATE)
    }

    private fun checkLotsCurrency(lotsDto: List<LotPnUpdate>, budgetCurrency: String) {
        lotsDto.asSequence().firstOrNull { it.value.currency != budgetCurrency }?.let {
            throw ErrorException(ErrorType.INVALID_LOT_CURRENCY)
        }
    }

    private fun checkLotsContractPeriod(lotsDto: List<LotPnUpdate>, tenderPeriodStartDate: LocalDateTime) {
        lotsDto.forEach { lot ->
            if (lot.contractPeriod.startDate >= lot.contractPeriod.endDate) {
                throw ErrorException(ErrorType.INVALID_LOT_CONTRACT_PERIOD)
            }
            if (lot.contractPeriod.startDate < tenderPeriodStartDate) {
                throw ErrorException(ErrorType.INVALID_LOT_CONTRACT_PERIOD)
            }
        }
    }

    private fun validateRelatedLots(lotIds: Set<String>, items: List<ItemPnUpdate>, documents: List<Document>?) {
        val lotsFromItems = items.asSequence().map { it.relatedLot }.toHashSet()
        if (lotsFromItems.size != lotIds.size) throw ErrorException(ErrorType.INVALID_ITEMS_RELATED_LOTS)
        if (!lotIds.containsAll(lotsFromItems)) throw ErrorException(ErrorType.INVALID_ITEMS_RELATED_LOTS)
        val lotsFromDocuments = documents?.asSequence()
                ?.filter { it.relatedLots != null }
                ?.flatMap { it.relatedLots!!.asSequence() }
                ?.toHashSet()
        if (lotsFromDocuments != null && lotsFromDocuments.size > 0) {
            if (lotsFromDocuments.size > lotIds.size) throw ErrorException(ErrorType.INVALID_DOCS_RELATED_LOTS)
            if (!lotIds.containsAll(lotsFromDocuments)) throw ErrorException(ErrorType.INVALID_DOCS_RELATED_LOTS)
        }
    }

    private fun setLotsIdAndItemsAndDocumentsRelatedLots(tender: TenderPnUpdate) {
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
                            .filter { it.relatedLots!!.contains(lot.id) }
                            .forEach { it.relatedLots!!.minus(lot.id).plus(id) }
                }
                lot.id = id
            }
        }
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
                            .filter { it.relatedLots!!.contains(lot.id) }
                            .forEach { it.relatedLots!!.minus(lot.id).plus(id) }
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
                    lot.status = TenderStatus.CANCELLED
                    lot.statusDetails = TenderStatusDetails.EMPTY
                    canceledLots.add(lot)
                }
        return canceledLots
    }

    private fun convertItems(itemsDto: List<ItemPnUpdate>): List<Item> {
        return itemsDto.asSequence().map { convertDtoItemToItem(it) }.toList()
    }

    private fun updateItems(itemsTender: List<Item>, itemsDto: List<ItemPnUpdate>): List<Item> {
        itemsTender.forEach { item ->
            val itemDto = itemsDto.asSequence().first { it.id == item.id }
            item.updateItem(itemDto)
        }
        return itemsTender
    }

    private fun setContractPeriod(tender: Tender, activeLots: List<Lot>, budget: Budget) {
        val startDate: LocalDateTime = activeLots.asSequence().minBy { it.contractPeriod.startDate }?.contractPeriod?.startDate!!
        val endDate: LocalDateTime = activeLots.asSequence().maxBy { it.contractPeriod.endDate }?.contractPeriod?.endDate!!
        budget.budgetBreakdown.forEach { bb ->
            if (startDate > bb.period.endDate) throw ErrorException(ErrorType.INVALID_LOT_CONTRACT_PERIOD)
            if (endDate < bb.period.startDate) throw ErrorException(ErrorType.INVALID_LOT_CONTRACT_PERIOD)
        }
        tender.contractPeriod = ContractPeriod(startDate, endDate)
    }

    private fun setTenderValueByActiveLots(tender: Tender, activeLots: List<Lot>) {
        if (activeLots.isNotEmpty()) {
            val totalAmount = activeLots.asSequence()
                    .sumByDouble { it.value.amount.toDouble() }
                    .toBigDecimal().setScale(2, RoundingMode.HALF_UP)
            if (totalAmount > tender.value.amount) throw ErrorException(ErrorType.INVALID_LOT_AMOUNT)
            tender.value.amount = totalAmount
        }
    }

    private fun convertDtoLotToLot(lotDto: LotPnUpdate): Lot {
        return Lot(
                id = lotDto.id,
                title = lotDto.title,
                description = lotDto.description,
                status = PLANNING,
                statusDetails = EMPTY,
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
