package com.procurement.access.service

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.cn.CnUpdate
import com.procurement.access.model.dto.cn.ItemCnUpdate
import com.procurement.access.model.dto.cn.LotCnUpdate
import com.procurement.access.model.dto.cn.TenderCnUpdate
import com.procurement.access.model.dto.ocds.*
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toDate
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service
import java.math.RoundingMode
import java.time.LocalDateTime

interface CnOnPnService {

    fun createCnOnPn(
            cpId: String,
            previousStage: String,
            stage: String,
            owner: String,
            token: String,
            dateTime: LocalDateTime,
            cnDto: CnUpdate): ResponseDto
}

@Service
class CnOnPnServiceImpl(private val generationService: GenerationService,
                        private val tenderProcessDao: TenderProcessDao) : CnOnPnService {

    override fun createCnOnPn(cpId: String,
                              previousStage: String,
                              stage: String,
                              owner: String,
                              token: String,
                              dateTime: LocalDateTime,
                              cnDto: CnUpdate): ResponseDto {

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, previousStage)
                ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        if (entity.owner != owner) throw ErrorException(ErrorType.INVALID_OWNER)
        if (entity.token.toString() != token) throw ErrorException(ErrorType.INVALID_TOKEN)
        val tenderProcess = toObject(TenderProcess::class.java, entity.jsonData)
        if (tenderProcess.tender.items.isEmpty()) {
            checkLotsCurrency(cnDto, tenderProcess.planning.budget.amount.currency)
            checkLotsContractPeriod(cnDto)
            setItemsId(cnDto.tender)
            validateRelatedLots(cnDto.tender)
            setLotsIdAndItemsAndDocumentsRelatedLots(cnDto.tender)
            tenderProcess.tender.apply {
                lots = setLots(cnDto.tender.lots)
                items = setItems(cnDto.tender.items)
                cnDto.tender.classification?.let { classification = it }
                value = getValueFromLots(cnDto.tender.lots, tenderProcess.planning.budget.amount)
                contractPeriod = setContractPeriod(cnDto.tender.lots, tenderProcess.planning.budget)
            }
        }
        tenderProcess.tender.apply {
            status = TenderStatus.ACTIVE
            statusDetails = TenderStatusDetails.EMPTY
            awardCriteria = AwardCriteria.PRICE_ONLY
            additionalProcurementCategories = null
            tenderPeriod = null
        }
        tenderProcessDao.save(getEntity(tenderProcess, entity, stage, dateTime))
        return ResponseDto(data = tenderProcess)
    }

    private fun checkLotsCurrency(cn: CnUpdate, budgetCurrency: String) {
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

    private fun setLots(lotsDto: List<LotCnUpdate>): List<Lot> {
        return lotsDto.asSequence().map { convertDtoLotToCnLot(it) }.toList()
    }

    private fun setItemsId(tender: TenderCnUpdate) {
        tender.items.forEach { it.id = generationService.getTimeBasedUUID() }
    }

    private fun setLotsIdAndItemsAndDocumentsRelatedLots(tender: TenderCnUpdate) {
         tender.lots.forEach { lot ->
            val id = generationService.getTimeBasedUUID()
            tender.items.asSequence()
                    .filter { it.relatedLot == lot.id }
                    .forEach { it.relatedLot = id }
            tender.documents.forEach { document ->
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

    private fun getValueFromLots(lotsDto: List<LotCnUpdate>, budgetValue: Value): Value {
        val currency = lotsDto.elementAt(0).value.currency
        val totalAmount = lotsDto.asSequence()
                .sumByDouble { it.value.amount.toDouble() }
                .toBigDecimal().setScale(2, RoundingMode.HALF_UP)
        if (totalAmount > budgetValue.amount) throw ErrorException(ErrorType.INVALID_LOT_AMOUNT)
        return Value(totalAmount, currency)
    }

    private fun setItems(itemsDto: List<ItemCnUpdate>): List<Item> {
        return itemsDto.asSequence().map { convertDtoItemToCnItem(it) }.toList()
    }

    private fun setContractPeriod(lotsDto: List<LotCnUpdate>, budget: Budget): ContractPeriod {
        val startDate: LocalDateTime = lotsDto.asSequence().minBy { it.contractPeriod.startDate }?.contractPeriod?.startDate!!
        val endDate: LocalDateTime = lotsDto.asSequence().maxBy { it.contractPeriod.endDate }?.contractPeriod?.endDate!!
        budget.budgetBreakdown.forEach { bb ->
            if (startDate > bb.period.endDate) throw ErrorException(ErrorType.INVALID_LOT_CONTRACT_PERIOD)
            if (endDate < bb.period.startDate) throw ErrorException(ErrorType.INVALID_LOT_CONTRACT_PERIOD)
        }
        return ContractPeriod(startDate, endDate)
    }

    private fun convertDtoLotToCnLot(lotDto: LotCnUpdate): Lot {
        return Lot(
                id = lotDto.id,
                title = lotDto.title,
                description = lotDto.description,
                status = TenderStatus.ACTIVE,
                statusDetails = TenderStatusDetails.EMPTY,
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
                          stage: String,
                          dateTime: LocalDateTime): TenderProcessEntity {
        return TenderProcessEntity(
                cpId = entity.cpId,
                token = entity.token,
                stage = stage,
                owner = entity.owner,
                createdDate = dateTime.toDate(),
                jsonData = toJson(tp)
        )
    }
}
