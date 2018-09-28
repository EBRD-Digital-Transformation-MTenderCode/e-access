package com.procurement.access.service

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType.*
import com.procurement.access.model.bpe.CommandMessage
import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.cn.CnUpdate
import com.procurement.access.model.dto.cn.ItemCnUpdate
import com.procurement.access.model.dto.cn.LotCnUpdate
import com.procurement.access.model.dto.cn.TenderCnUpdate
import com.procurement.access.model.dto.ocds.*
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toDate
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toLocal
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service
import java.math.RoundingMode
import java.time.LocalDateTime

interface CnOnPnService {

    fun createCnOnPn(cm: CommandMessage): ResponseDto
}

@Service
class CnOnPnServiceImpl(private val generationService: GenerationService,
                        private val tenderProcessDao: TenderProcessDao) : CnOnPnService {

    override fun createCnOnPn(cm: CommandMessage): ResponseDto {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val token = cm.context.token ?: throw ErrorException(CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)
        val previousStage = cm.context.prevStage ?: throw ErrorException(CONTEXT)
        val owner = cm.context.owner ?: throw ErrorException(CONTEXT)
        val dateTime = cm.context.startDate?.toLocal() ?: throw ErrorException(CONTEXT)
        val phase = cm.context.phase ?: throw ErrorException(CONTEXT)
        val cnDto = toObject(CnUpdate::class.java, cm.data)

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, previousStage) ?: throw ErrorException(DATA_NOT_FOUND)
        if (entity.owner != owner) throw ErrorException(INVALID_OWNER)
        if (entity.token.toString() != token) throw ErrorException(INVALID_TOKEN)
        val tenderProcess = toObject(TenderProcess::class.java, entity.jsonData)
        val tenderDto = cnDto.tender
        if (tenderProcess.tender.items.isEmpty()) {
            checkLotsCurrency(cnDto, tenderProcess.planning.budget.amount.currency)
            checkLotsContractPeriod(cnDto)
            val lotsId = tenderDto.lots.asSequence().map { it.id }.toSet()
            if (lotsId.size < tenderDto.lots.size) throw ErrorException(INVALID_LOT_ID)
            validateRelatedLots(lotsId, tenderDto)
            setItemsId(tenderDto)
            setLotsIdAndItemsAndDocumentsRelatedLots(tenderDto)
            tenderProcess.tender.apply {
                lots = setLots(cnDto.tender.lots)
                items = setItems(cnDto.tender.items)
                documents = updateDocuments(documents, cnDto.tender.documents)
                cnDto.tender.classification?.let { classification = it }
                value = getValueFromLots(cnDto.tender.lots, tenderProcess.planning.budget.amount)
                contractPeriod = setContractPeriod(cnDto.tender.lots, tenderProcess.planning.budget)
            }
        } else {
            val lotsId = tenderProcess.tender.lots.asSequence().map { it.id }.toSet()
            validateRelatedLots(lotsId, tenderDto)
        }
        tenderProcess.tender.apply {
            documents = updateDocuments(documents, cnDto.tender.documents)
            status = TenderStatus.ACTIVE
            statusDetails = TenderStatusDetails.fromValue(phase)
            awardCriteria = AwardCriteria.PRICE_ONLY
            additionalProcurementCategories = null
            tenderPeriod = cnDto.tender.tenderPeriod
            enquiryPeriod = cnDto.tender.enquiryPeriod
        }
        tenderProcessDao.save(getEntity(tenderProcess, entity, stage, dateTime))
        return ResponseDto(data = tenderProcess)
    }

    private fun validateRelatedLots(lotsId: Set<String>, tender: TenderCnUpdate) {
        val lotsFromDocuments = tender.documents.asSequence()
                .filter { it.relatedLots != null }
                .flatMap { it.relatedLots!!.asSequence() }.toHashSet()
        if (lotsFromDocuments.isNotEmpty()) {
            if (!lotsId.containsAll(lotsFromDocuments)) throw ErrorException(INVALID_DOCS_RELATED_LOTS)
        }
        val lotsFromItems = tender.items.asSequence()
                .map { it.relatedLot }.toHashSet()
        if (!lotsId.containsAll(lotsFromItems)) throw ErrorException(INVALID_ITEMS_RELATED_LOTS)
    }

    private fun checkLotsCurrency(cn: CnUpdate, budgetCurrency: String) {
        cn.tender.lots.asSequence().firstOrNull { it.value.currency != budgetCurrency }?.let {
            throw ErrorException(INVALID_LOT_CURRENCY)
        }
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

    private fun updateDocuments(documentsTender: List<Document>?, documentsDto: List<Document>?): List<Document>? {
        return if (documentsTender != null && documentsTender.isNotEmpty()) {
            if (documentsDto != null) {
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
                documentsTender
            }
        } else {
            documentsDto
        }
    }

    private fun Document.updateDocument(documentDto: Document) {
        this.title = documentDto.title
        this.description = documentDto.description
        this.relatedLots = documentDto.relatedLots
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

    private fun getValueFromLots(lotsDto: List<LotCnUpdate>, budgetValue: Value): Value {
        val currency = lotsDto.elementAt(0).value.currency
        val totalAmount = lotsDto.asSequence()
                .sumByDouble { it.value.amount.toDouble() }
                .toBigDecimal().setScale(2, RoundingMode.HALF_UP)
        if (totalAmount > budgetValue.amount) throw ErrorException(INVALID_LOT_AMOUNT)
        return Value(totalAmount, currency)
    }

    private fun setItems(itemsDto: List<ItemCnUpdate>): List<Item> {
        return itemsDto.asSequence().map { convertDtoItemToCnItem(it) }.toList()
    }

    private fun setContractPeriod(lotsDto: List<LotCnUpdate>, budget: Budget): ContractPeriod {
        val startDate: LocalDateTime = lotsDto.asSequence().minBy { it.contractPeriod.startDate }?.contractPeriod?.startDate!!
        val endDate: LocalDateTime = lotsDto.asSequence().maxBy { it.contractPeriod.endDate }?.contractPeriod?.endDate!!
        budget.budgetBreakdown.forEach { bb ->
            if (startDate > bb.period.endDate) throw ErrorException(INVALID_LOT_CONTRACT_PERIOD)
            if (endDate < bb.period.startDate) throw ErrorException(INVALID_LOT_CONTRACT_PERIOD)
        }
        return ContractPeriod(startDate, endDate)
    }

    private fun convertDtoLotToCnLot(lotDto: LotCnUpdate): Lot {
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
