package com.procurement.access.service

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.cn.*
import com.procurement.access.model.dto.ocds.*
import com.procurement.access.model.dto.ocds.TenderStatus.ACTIVE
import com.procurement.access.model.dto.ocds.TenderStatusDetails.EMPTY
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toDate
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service
import java.math.RoundingMode
import java.time.LocalDateTime

interface CnUpdateService {

    fun updateCn(
            cpId: String,
            stage: String,
            owner: String,
            token: String,
            dateTime: LocalDateTime,
            cnDto: CnUpdate): ResponseDto
}

@Service
class CnUpdateServiceImpl(private val generationService: GenerationService,
                          private val tenderProcessDao: TenderProcessDao) : CnUpdateService {

    override fun updateCn(cpId: String,
                          stage: String,
                          owner: String,
                          token: String,
                          dateTime: LocalDateTime,
                          cnDto: CnUpdate): ResponseDto {
        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage)
                ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        if (entity.owner != owner) throw ErrorException(ErrorType.INVALID_OWNER)
        if (entity.token.toString() != token) throw ErrorException(ErrorType.INVALID_TOKEN)
        val tenderProcess = toObject(TenderProcess::class.java, entity.jsonData)
        checkLotsCurrency(cnDto)
        checkLotsContractPeriod(cnDto)

        validateLotsValue(cnDto.tender.lots, tenderProcess.planning.budget.amount)

        val itemsId = tenderProcess.tender.items.asSequence().map { it.id }.toSet()
        val itemsDtoId = cnDto.tender.items.asSequence().map { it.id }.toSet()
        val newItemsId = itemsDtoId - itemsId
        setNewItemsId(cnDto.tender.items, newItemsId)

        validateRelatedLots(cnDto.tender)

        val lotsId = tenderProcess.tender.lots.asSequence().map { it.id }.toSet()
        val lotsDtoId = cnDto.tender.lots.asSequence().map { it.id }.toSet()
        val newLotsId = lotsDtoId - lotsId
        setLotsIdAndItemsAndDocumentsRelatedLots(cnDto.tender, newLotsId)
        val oldLotsId = lotsId - lotsDtoId
        tenderProcess.tender.items = addNewItems(tenderProcess.tender.items, cnDto.tender.items, newItemsId)
        tenderProcess.tender.lots = addNewLots(tenderProcess.tender.lots, cnDto.tender.lots, newLotsId)
        setOldLotStatuses(tenderProcess.tender.lots, oldLotsId)
        tenderProcess.tender.contractPeriod = setContractPeriod(cnDto.tender.lots, cnDto.planning.budget)
        tenderProcessDao.save(getEntity(tenderProcess, entity, dateTime))
        return ResponseDto(data = tenderProcess)
    }

    private fun addNewLots(lots: List<Lot>, lotsDto: List<LotCnUpdate>, newLotsId: Set<String>): List<Lot> {
        val newLots = lotsDto.asSequence()
                .filter { it.id in newLotsId }
                .map { convertDtoLotToCnLot(it) }.toList()
        return lots + newLots
    }

    private fun addNewItems(items: List<Item>, itemsDto: List<ItemCnUpdate>, newItemsId: Set<String?>): List<Item> {
        val newItems = itemsDto.asSequence()
                .filter { it.id in newItemsId }
                .map { convertDtoItemToCnItem(it) }.toList()
        return items + newItems
    }

    private fun checkLotsCurrency(cn: CnUpdate) {
        val budgetCurrency = cn.planning.budget.amount.currency
        cn.tender.lots.asSequence().firstOrNull { it.value.currency != budgetCurrency }?.let {
            throw ErrorException(ErrorType.INVALID_LOT_CURRENCY)
        }
    }

    private fun checkLotsContractPeriod(cn: CnUpdate) {
        cn.tender.lots.forEach { lot ->
            if (lot.contractPeriod.startDate >= lot.contractPeriod.endDate) {
                throw ErrorException(ErrorType.INVALID_LOT_CONTRACT_PERIOD)
            }
            if (lot.contractPeriod.startDate <= cn.tender.tenderPeriod.endDate) {
                throw ErrorException(ErrorType.INVALID_LOT_CONTRACT_PERIOD)
            }
        }
    }

    private fun setNewItemsId(itemsDto: List<ItemCnUpdate>, newItemsId: Set<String?>) {
        if (newItemsId.isNotEmpty())
            itemsDto.asSequence()
                    .filter { it.id in newItemsId }
                    .forEach { it.id = generationService.generateTimeBasedUUID().toString() }
    }


    private fun setLotsIdAndItemsAndDocumentsRelatedLots(tenderDto: TenderCnUpdate, newLotsId: Set<String>) {
        if (newLotsId.isNotEmpty()) {
            tenderDto.lots.asSequence()
                    .filter { it.id in newLotsId }
                    .forEach { lot ->
                        val id = generationService.generateTimeBasedUUID().toString()
                        tenderDto.items.asSequence()
                                .filter { it.relatedLot == lot.id }
                                .forEach { it.relatedLot = id }
                        tenderDto.documents.forEach { document ->
                            document.relatedLots?.let { relatedLots ->
                                if (relatedLots.contains(lot.id)) {
                                    relatedLots.remove(lot.id)
                                    relatedLots.add(id)
                                }
                            }
                        }
                        lot.id = id
                    }
        }
    }

    private fun validateRelatedLots(tender: TenderCnUpdate) {
        val lotsFromCn = tender.lots.asSequence().map { it.id }.toHashSet()
        val lotsFromDocuments = tender.documents.asSequence()
                .filter { it.relatedLots != null }
                .flatMap { it.relatedLots!!.asSequence() }.toHashSet()
        if (lotsFromDocuments.isNotEmpty()) {
            if (lotsFromDocuments.size > lotsFromCn.size) throw ErrorException(ErrorType.INVALID_DOCS_RELATED_LOTS)
            if (!lotsFromCn.containsAll(lotsFromDocuments)) throw ErrorException(ErrorType.INVALID_DOCS_RELATED_LOTS)
        }
        val lotsFromItems = tender.items.asSequence()
                .map { it.relatedLot }.toHashSet()
        if (lotsFromItems.size != lotsFromCn.size) throw ErrorException(ErrorType.INVALID_ITEMS_RELATED_LOTS)
        if (!lotsFromCn.containsAll(lotsFromItems)) throw ErrorException(ErrorType.INVALID_ITEMS_RELATED_LOTS)
    }

    private fun setOldLotStatuses(lots: List<Lot>, oldLotsId: Set<String>) {
        lots.asSequence()
                .filter { it.id in oldLotsId }
                .forEach { lot ->
                    lot.status = TenderStatus.CANCELLED
                    lot.statusDetails = TenderStatusDetails.EMPTY
                }
    }


    private fun validateLotsValue(lotsDto: List<LotCnUpdate>, budgetValue: Value) {
        val totalAmount = lotsDto.asSequence()
                .sumByDouble { it.value.amount.toDouble() }
                .toBigDecimal().setScale(2, RoundingMode.HALF_UP)
        if (totalAmount > budgetValue.amount) throw ErrorException(ErrorType.INVALID_LOT_AMOUNT)
    }

    private fun setContractPeriod(lotsDto: List<LotCnUpdate>, budget: BudgetCnUpdate): Period {
        val startDate: LocalDateTime = lotsDto.asSequence().minBy { it.contractPeriod.startDate }?.contractPeriod?.startDate!!
        val endDate: LocalDateTime = lotsDto.asSequence().maxBy { it.contractPeriod.endDate }?.contractPeriod?.endDate!!
        budget.budgetBreakdown.forEach { bb ->
            if (startDate > bb.period.endDate) throw ErrorException(ErrorType.INVALID_LOT_CONTRACT_PERIOD)
            if (endDate < bb.period.startDate) throw ErrorException(ErrorType.INVALID_LOT_CONTRACT_PERIOD)
        }
        return Period(startDate, endDate)
    }

    private fun convertDtoLotToCnLot(lotDto: LotCnUpdate): Lot {
        return Lot(
                id = lotDto.id,
                title = lotDto.title,
                description = lotDto.description,
                status = ACTIVE,
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

    private fun convertDtoItemToCnItem(itemDto: ItemCnUpdate): Item {
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
