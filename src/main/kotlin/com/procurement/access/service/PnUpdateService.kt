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
        //dto lots validation
        validateRelatedLots(pnDto.tender)
        //new items
        var updatedItems: List<Item> = listOf()
        pnDto.tender.items?.let { itemsDto ->
            val itemsDbId = tenderProcess.tender.items.asSequence().map { it.id }.toSet()
            val itemsDtoId = itemsDto.asSequence().map { it.id }.toSet()
            val newItemsId = itemsDtoId - itemsDbId
            setNewItemsId(pnDto.tender.items, newItemsId)
            updatedItems = getItems(pnDto.tender.items)
        }
        //new, old lots
        val lotsDbId = tenderProcess.tender.lots.asSequence().map { it.id }.toSet()
        var activeLots: List<Lot> = listOf()
        var canceledLots: List<Lot>
        if (pnDto.tender.lots != null) {
            checkLotsCurrency(pnDto.tender.lots, tenderProcess.tender.value.currency)
            checkLotsContractPeriod(pnDto.tender.lots)
            val lotsDtoId = pnDto.tender.lots.asSequence().map { it.id }.toSet()
            val newLotsId = getNewLotsIdAndSetItemsAndDocumentsRelatedLots(pnDto.tender, lotsDtoId - lotsDbId)
            val canceledLotsId = lotsDbId - lotsDtoId
            activeLots = getActiveLots(pnDto.tender.lots, tenderProcess.tender.lots, newLotsId)
            canceledLots = getCanceledLots(tenderProcess.tender.lots, canceledLotsId)
        } else {
            canceledLots = getCanceledLots(tenderProcess.tender.lots, lotsDbId)
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
        }
        if (pnDto.tender.lots != null) {
            setContractPeriod(tenderProcess.tender, activeLots, tenderProcess.planning.budget)
            setTenderValueByActiveLots(tenderProcess.tender, activeLots)
        }

        tenderProcessDao.save(getEntity(tenderProcess, entity, dateTime))
        return ResponseDto(data = tenderProcess)
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

    private fun getActiveLots(lotsDto: List<LotPnUpdate>, lotsTender: List<Lot>, newLotsId: Set<String>): List<Lot> {
        val activeLots = mutableListOf<Lot>()
        lotsDto.forEach { lotDto ->
            if (lotDto.id in newLotsId) {
                activeLots.add(convertDtoLotToLot(lotDto))
            } else {
                val updatableTenderLot = lotsTender.asSequence().first { it.id == lotDto.id }
                activeLots.add(updateLot(updatableTenderLot, lotDto))
            }
        }
        return activeLots
    }

    private fun getCanceledLots(lotsTender: List<Lot>, canceledLotsId: Set<String>): List<Lot> {
        val canceledLots = lotsTender.asSequence().filter { it.id in canceledLotsId }.toList()
        canceledLots.asSequence().forEach {
            it.status = TenderStatus.CANCELLED
            it.statusDetails = TenderStatusDetails.EMPTY
        }
        return canceledLots
    }

    private fun getItems(itemsDto: List<ItemPnUpdate>): List<Item> {
        return itemsDto.asSequence().map { convertDtoItemToItem(it) }.toList()
    }

    private fun checkLotsCurrency(lotsDto: List<LotPnUpdate>, budgetCurrency: String) {
        lotsDto.asSequence().firstOrNull { it.value.currency != budgetCurrency }?.let {
            throw ErrorException(ErrorType.INVALID_LOT_CURRENCY)
        }
    }

    private fun checkLotsContractPeriod(lotsDto: List<LotPnUpdate>) {
        lotsDto.forEach { lot ->
            if (lot.contractPeriod.startDate >= lot.contractPeriod.endDate) {
                throw ErrorException(ErrorType.INVALID_LOT_CONTRACT_PERIOD)
            }
        }
        TODO()
    }

    private fun setNewItemsId(itemsDto: List<ItemPnUpdate>, newItemsId: Set<String?>) {
        if (newItemsId.isNotEmpty())
            itemsDto.asSequence()
                    .filter { it.id in newItemsId }
                    .forEach { it.id = generationService.generateTimeBasedUUID().toString() }
    }

    private fun getNewLotsIdAndSetItemsAndDocumentsRelatedLots(tenderDto: TenderPnUpdate, newLotsId: Set<String>): Set<String> {
        val lotIds = mutableSetOf<String>()
        if (newLotsId.isNotEmpty()) {
            tenderDto.lots?.asSequence()
                    ?.filter { it.id in newLotsId }
                    ?.forEach { lot ->
                        val id = generationService.generateTimeBasedUUID().toString()
                        tenderDto.items?.asSequence()
                                ?.filter { it.relatedLot == lot.id }
                                ?.forEach { it.relatedLot = id }
                        tenderDto.documents?.forEach { document ->
                            document.relatedLots?.let { relatedLots ->
                                if (relatedLots.contains(lot.id)) {
                                    relatedLots.remove(lot.id)
                                    relatedLots.add(id)
                                }
                            }
                        }
                        lot.id = id
                        lotIds.add(id)
                    }
        }
        return lotIds
    }

    private fun validateRelatedLots(tender: TenderPnUpdate) {
        val lotsFromPn = tender.lots?.asSequence()?.map { it.id }?.toHashSet() ?: hashSetOf()
        val lotsFromDocuments = tender.documents?.asSequence()
                ?.filter { it.relatedLots != null }
                ?.flatMap { it.relatedLots!!.asSequence() }?.toHashSet()
        if (lotsFromDocuments != null) {
            if (lotsFromDocuments.size > lotsFromPn.size) throw ErrorException(ErrorType.INVALID_DOCS_RELATED_LOTS)
            if (!lotsFromPn.containsAll(lotsFromDocuments)) throw ErrorException(ErrorType.INVALID_DOCS_RELATED_LOTS)
        }
        val lotsFromItems = tender.items?.asSequence()?.map { it.relatedLot }?.toHashSet()
        if (lotsFromItems != null) {
            if (lotsFromItems.size != lotsFromPn.size) throw ErrorException(ErrorType.INVALID_ITEMS_RELATED_LOTS)
            if (!lotsFromPn.containsAll(lotsFromItems)) throw ErrorException(ErrorType.INVALID_ITEMS_RELATED_LOTS)
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

    private fun updateLot(lotTender: Lot, lotDto: LotPnUpdate): Lot {
        return Lot(
                id = lotDto.id,
                title = lotDto.title,
                description = lotDto.description,
                status = PLANNING,
                statusDetails = EMPTY,
                value = lotTender.value,
                options = listOf(Option(false)),
                recurrentProcurement = listOf(RecurrentProcurement(false)),
                renewals = listOf(Renewal(false)),
                variants = listOf(Variant(false)),
                contractPeriod = lotDto.contractPeriod,
                placeOfPerformance = lotDto.placeOfPerformance
        )
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
