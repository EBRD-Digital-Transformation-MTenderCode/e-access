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
        //dto lots validation
        checkLotsCurrency(cnDto, tenderProcess.tender.value.currency)
        checkLotsContractPeriod(cnDto)
        validateRelatedLots(cnDto.tender)
        //new items
        val itemsDbId = tenderProcess.tender.items.asSequence().map { it.id }.toSet()
        val itemsDtoId = cnDto.tender.items.asSequence().map { it.id }.toSet()
        val newItemsId = itemsDtoId - itemsDbId
        setNewItemsId(cnDto.tender.items, newItemsId)
        //new, old lots
        val lotsDbId = tenderProcess.tender.lots.asSequence().map { it.id }.toSet()
        val lotsDtoId = cnDto.tender.lots.asSequence().map { it.id }.toSet()
        val newLotsId = getNewLotsIdAndSetItemsAndDocumentsRelatedLots(cnDto.tender, lotsDtoId - lotsDbId)
        val canceledLotsId = lotsDbId - lotsDtoId

        val activeLots = getActiveLots(cnDto.tender.lots, tenderProcess.tender.lots, newLotsId)
        val canceledLots = getCanceledLots(tenderProcess.tender.lots, canceledLotsId)

        tenderProcess.planning.apply {
            rationale = cnDto.planning.rationale
            budget.description = cnDto.planning.budget.description
        }
        tenderProcess.tender.apply {
            title = cnDto.tender.title
            description = cnDto.tender.description
            procurementMethodRationale = cnDto.tender.procurementMethodRationale
            procurementMethodAdditionalInfo = cnDto.tender.procurementMethodAdditionalInfo
            items = setItems(cnDto.tender.items)
            lots = activeLots + canceledLots
            documents = cnDto.tender.documents
        }
        setContractPeriod(tenderProcess.tender, tenderProcess.planning.budget, activeLots)
        setTenderValueByActiveLots(tenderProcess.tender, activeLots)

        tenderProcessDao.save(getEntity(tenderProcess, entity, dateTime))
        return ResponseDto(data = tenderProcess)
    }

    private fun setContractPeriod(tender: Tender, budget: Budget, activeLots: List<Lot>) {
        val startDate: LocalDateTime = activeLots.asSequence().minBy { it.contractPeriod.startDate }?.contractPeriod?.startDate!!
        val endDate: LocalDateTime = activeLots.asSequence().maxBy { it.contractPeriod.endDate }?.contractPeriod?.endDate!!
        budget.budgetBreakdown.forEach { bb ->
            if (startDate > bb.period.endDate) throw ErrorException(ErrorType.INVALID_LOT_CONTRACT_PERIOD)
            if (endDate < bb.period.startDate) throw ErrorException(ErrorType.INVALID_LOT_CONTRACT_PERIOD)
        }
        tender.contractPeriod = ContractPeriod(startDate, endDate)
    }

    private fun setTenderValueByActiveLots(tender: Tender, activeLots: List<Lot>) {
        val totalAmount = activeLots.asSequence()
                .sumByDouble { it.value.amount.toDouble() }
                .toBigDecimal().setScale(2, RoundingMode.HALF_UP)
        if (totalAmount > tender.value.amount) throw ErrorException(ErrorType.INVALID_LOT_AMOUNT)
        tender.value.amount = totalAmount
    }

    private fun getActiveLots(lotsDto: List<LotCnUpdate>, lotsTender: List<Lot>, newLotsId: Set<String>): List<Lot> {
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

    private fun setItems(itemsDto: List<ItemCnUpdate>): List<Item> {
        return itemsDto.asSequence().map { convertDtoItemToItem(it) }.toList()
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

    private fun setNewItemsId(itemsDto: List<ItemCnUpdate>, newItemsId: Set<String?>) {
        if (newItemsId.isNotEmpty())
            itemsDto.asSequence()
                    .filter { it.id in newItemsId }
                    .forEach { it.id = generationService.generateTimeBasedUUID().toString() }
    }

    private fun getNewLotsIdAndSetItemsAndDocumentsRelatedLots(tenderDto: TenderCnUpdate, newLotsId: Set<String>):
            Set<String> {
        val lotIds = mutableSetOf<String>()
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
                        lotIds.add(id)
                    }
        }
        return lotIds
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

    private fun convertDtoLotToLot(lotDto: LotCnUpdate): Lot {
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

    private fun updateLot(lotTender: Lot, lotDto: LotCnUpdate): Lot {
        return Lot(
                id = lotDto.id,
                title = lotDto.title,
                description = lotDto.description,
                status = ACTIVE,
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

    private fun convertDtoItemToItem(itemDto: ItemCnUpdate): Item {
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
