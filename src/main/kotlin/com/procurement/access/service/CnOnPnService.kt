package com.procurement.access.service

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.exception.ErrorType.*
import com.procurement.access.model.bpe.CommandMessage
import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.cn.*
import com.procurement.access.model.dto.ocds.*
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toDate
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toLocal
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

@Service
class CnOnPnService(private val generationService: GenerationService,
                    private val tenderProcessDao: TenderProcessDao,
                    private val rulesService: RulesService) {


    fun checkCnOnPn(cm: CommandMessage): ResponseDto {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val token = cm.context.token ?: throw ErrorException(CONTEXT)
        val previousStage = cm.context.prevStage ?: throw ErrorException(CONTEXT)
        val owner = cm.context.owner ?: throw ErrorException(CONTEXT)
        val country = cm.context.country ?: throw ErrorException(CONTEXT)
        val pmd = cm.context.pmd ?: throw ErrorException(CONTEXT)
        val cnDto = toObject(CnUpdate::class.java, cm.data).validate()
        val entity = tenderProcessDao.getByCpIdAndStage(cpId, previousStage) ?: throw ErrorException(DATA_NOT_FOUND)
        if (entity.owner != owner) throw ErrorException(INVALID_OWNER)
        if (entity.token.toString() != token) throw ErrorException(INVALID_TOKEN)
        val tenderProcess = toObject(TenderProcess::class.java, entity.jsonData)
        val tenderDto = cnDto.tender
        checkAuctionsDto(country, pmd, cnDto, tenderProcess.tender.mainProcurementCategory)
        checkItems(cnDto.tender.items)
        if (tenderProcess.tender.items.isEmpty()) {
            checkLotsValue(cnDto, tenderProcess.planning.budget)
            checkLotsAndTenderContractPeriod(cnDto, tenderProcess.planning.budget)
            checkDtoRelatedLots(tenderDto)
        } else {
            checkLotsContractPeriod(cnDto)
            tenderDto.electronicAuctions?.let { checkAuctions(tenderProcess.tender.lots, it) }
        }
        checkDocuments(tender = tenderProcess.tender, documentsDto = cnDto.tender.documents)
        checkDocumentsRelatedLots(cnDto.tender)
        return ResponseDto(data = "ok")
    }

    fun createCnOnPn(cm: CommandMessage): ResponseDto {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)
        val previousStage = cm.context.prevStage ?: throw ErrorException(CONTEXT)
        val dateTime = cm.context.startDate?.toLocal() ?: throw ErrorException(CONTEXT)
        val phase = cm.context.phase ?: throw ErrorException(CONTEXT)
        val cnDto = toObject(CnUpdate::class.java, cm.data).validate()

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, previousStage) ?: throw ErrorException(DATA_NOT_FOUND)
        val tenderProcess = toObject(TenderProcess::class.java, entity.jsonData)
        val tenderDto = cnDto.tender
        if (tenderProcess.tender.items.isEmpty()) {
            setItemsId(tenderDto.items)
            setLotsId(tenderDto)
            tenderProcess.tender.apply {
                lots = getLots(tenderDto.lots)
                items = getItems(tenderDto.items)
                tenderDto.classification?.let { classification = it }
                value = getValueFromLots(tenderDto.lots)
                contractPeriod = getContractPeriod(tenderDto.lots)
            }
        } else {
            updatedLots(tenderProcess.tender.lots)
        }
        tenderProcess.tender.apply {
            documents = updateDocuments(tender = this, documentsDto = cnDto.tender.documents)
            status = TenderStatus.ACTIVE
            statusDetails = TenderStatusDetails.fromValue(phase)
            additionalProcurementCategories = null
            tenderPeriod = tenderDto.tenderPeriod
            enquiryPeriod = tenderDto.enquiryPeriod
            procurementMethodRationale = tenderDto.procurementMethodRationale
            procurementMethodAdditionalInfo = tenderDto.procurementMethodAdditionalInfo
            if (tenderDto.electronicAuctions != null) {
                procurementMethodModalities = tenderDto.procurementMethodModalities
                electronicAuctions = tenderDto.electronicAuctions
            }
            awardCriteria = tenderDto.awardCriteria ?: AwardCriteria.PRICE_ONLY
        }
        tenderProcessDao.save(getEntity(tenderProcess, entity, stage, dateTime))
        return ResponseDto(data = tenderProcess)
    }

    private fun checkItems(items: List<ItemCnUpdate>) {
        val itemsId = items.asSequence().map { it.id }.toHashSet()
        if (itemsId.size != items.size) throw ErrorException(INVALID_ITEMS)
    }

    private fun checkAuctionsDto(country: String, pmd: String, cnDto: CnUpdate, mainProcurementCategory: MainProcurementCategory) {
        if (rulesService.isAuctionRequired(country, pmd, mainProcurementCategory.value)) {
            cnDto.tender.procurementMethodModalities ?: throw ErrorException(ErrorType.INVALID_PMM)
            if (cnDto.tender.procurementMethodModalities.isEmpty()) throw ErrorException(ErrorType.INVALID_PMM)
            cnDto.tender.electronicAuctions ?: throw ErrorException(ErrorType.INVALID_AUCTION_IS_EMPTY)
            cnDto.tender.electronicAuctions.validate()
        }
    }

    private fun checkDtoRelatedLots(tender: TenderCnUpdate) {
        val lotsIdSet = tender.lots.asSequence().map { it.id }.toSet()
        if (lotsIdSet.size != tender.lots.size) throw ErrorException(INVALID_LOT_ID)
        val lotsFromItemsSet = tender.items.asSequence().map { it.relatedLot }.toHashSet()
        if (lotsFromItemsSet.size != lotsIdSet.size) throw ErrorException(INVALID_ITEMS_RELATED_LOTS)
        if (!lotsIdSet.containsAll(lotsFromItemsSet)) throw ErrorException(INVALID_ITEMS_RELATED_LOTS)
        tender.electronicAuctions?.let { auctions ->
            val auctionIds = auctions.details.asSequence().map { it.id }.toHashSet()
            if (auctionIds.size != auctions.details.size) throw ErrorException(INVALID_AUCTION_ID)
            val lotsFromAuctions = auctions.details.asSequence().map { it.relatedLot }.toHashSet()
            if (lotsFromAuctions.size != auctions.details.size) throw ErrorException(INVALID_AUCTION_RELATED_LOTS)
            if (lotsFromAuctions.size != lotsIdSet.size) throw ErrorException(INVALID_AUCTION_RELATED_LOTS)
            if (!lotsIdSet.containsAll(lotsFromAuctions)) throw ErrorException(INVALID_AUCTION_RELATED_LOTS)
        }
    }

    private fun checkLotsValue(cn: CnUpdate, budget: Budget) {
        cn.tender.lots.asSequence().firstOrNull { it.value.currency != budget.amount.currency }?.let {
            throw ErrorException(INVALID_LOT_CURRENCY)
        }
        val totalAmount = cn.tender.lots.asSequence()
                .sumByDouble { it.value.amount.toDouble() }
                .toBigDecimal().setScale(2, RoundingMode.HALF_UP)
        if (totalAmount > budget.amount.amount) throw ErrorException(INVALID_LOT_AMOUNT)
    }

    private fun checkLotsAndTenderContractPeriod(cn: CnUpdate, budget: Budget) {
        cn.tender.lots.forEach { lot ->
            if (lot.contractPeriod.startDate >= lot.contractPeriod.endDate) {
                throw ErrorException(INVALID_LOT_CONTRACT_PERIOD)
            }
            if (lot.contractPeriod.startDate <= cn.tender.tenderPeriod.endDate) {
                throw ErrorException(INVALID_LOT_CONTRACT_PERIOD)
            }
        }
        val contractPeriodSet = cn.tender.lots.asSequence().map { it.contractPeriod }.toSet()
        val startDate = contractPeriodSet.minBy { it.startDate }!!.startDate
        val endDate = contractPeriodSet.maxBy { it.endDate }!!.endDate
        budget.budgetBreakdown.forEach { bb ->
            if (startDate > bb.period.endDate) throw ErrorException(INVALID_LOT_CONTRACT_PERIOD)
            if (endDate < bb.period.startDate) throw ErrorException(INVALID_LOT_CONTRACT_PERIOD)
        }
    }

    private fun checkLotsContractPeriod(cn: CnUpdate) {
        cn.tender.lots.forEach { lot ->
            if (lot.contractPeriod.startDate <= cn.tender.tenderPeriod.endDate) {
                throw ErrorException(INVALID_LOT_CONTRACT_PERIOD)
            }
        }
    }

    private fun checkDocuments(tender: Tender, documentsDto: List<Document>) {
        val docsId = documentsDto.asSequence().map { it.id }.toHashSet()
        if (docsId.size != documentsDto.size) throw ErrorException(INVALID_DOCS_ID)
        if (tender.documents != null && tender.documents!!.isNotEmpty()) {
            val documentsDb = tender.documents!!
            val documentsDtoId = documentsDto.asSequence().map { it.id }.toSet()
            val documentsDbId = documentsDb.asSequence().map { it.id }.toSet()
            if (!documentsDtoId.containsAll(documentsDbId)) throw ErrorException(INVALID_DOCS_ID)
        }
    }

    private fun checkDocumentsRelatedLots(tender: TenderCnUpdate) {
        val lotsId = tender.lots.asSequence().map { it.id }.toHashSet()
        val lotsFromDocuments = tender.documents.asSequence()
                .filter { it.relatedLots != null }
                .flatMap { it.relatedLots!!.asSequence() }
                .toHashSet()
        if (lotsFromDocuments.isNotEmpty()) {
            if (!lotsId.containsAll(lotsFromDocuments)) throw ErrorException(INVALID_DOCS_RELATED_LOTS)
        }
    }

    private fun checkAuctions(lots: List<Lot>, auctions: ElectronicAuctions) {
        val activeLots = lots.asSequence().filter { it.status == LotStatus.PLANNING }.toList()
        val activeLotsIdSet = activeLots.asSequence().map { it.id }.toSet()
        val lotsFromAuctions = auctions.details.asSequence().map { it.relatedLot }.toHashSet()
        if (lotsFromAuctions.size != auctions.details.size) throw ErrorException(INVALID_AUCTION_RELATED_LOTS)
        if (lotsFromAuctions.size != activeLotsIdSet.size) throw ErrorException(INVALID_AUCTION_RELATED_LOTS)
        if (!activeLotsIdSet.containsAll(lotsFromAuctions)) throw ErrorException(INVALID_AUCTION_RELATED_LOTS)
        activeLots.forEach { lot ->
            auctions.details.asSequence().filter { it.relatedLot == lot.id }.forEach { auction ->
                checkAuctionMinimum(lot.value.amount, lot.value.currency, auction)
            }
        }
    }

    private fun checkAuctionMinimum(lotAmount: BigDecimal, lotCurrency: String, auction: ElectronicAuctionsDetails) {
        val lotAmountMinimum = lotAmount.div(BigDecimal(10))
        for (modality in auction.electronicAuctionModalities) {
            if (modality.eligibleMinimumDifference.amount > lotAmountMinimum)
                throw ErrorException(INVALID_AUCTION_MINIMUM)
            if (modality.eligibleMinimumDifference.currency != lotCurrency)
                throw ErrorException(INVALID_AUCTION_CURRENCY)
        }
    }

    private fun updateDocuments(tender: Tender, documentsDto: List<Document>): List<Document> {
        return if (tender.documents != null && tender.documents!!.isNotEmpty()) {
            val documentsDb = tender.documents!!
            val documentsDtoId = documentsDto.asSequence().map { it.id }.toSet()
            val documentsDbId = documentsDb.asSequence().map { it.id }.toSet()
            documentsDb.forEach { docDb -> docDb.updateDocument(documentsDto.first { it.id == docDb.id }) }
            val newDocumentsId = documentsDtoId - documentsDbId
            val newDocuments = documentsDto.asSequence().filter { it.id in newDocumentsId }.toList()
            documentsDb + newDocuments
        } else {
            documentsDto
        }
    }

    private fun Document.updateDocument(documentDto: Document) {
        this.title = documentDto.title
        this.description = documentDto.description
        this.relatedLots = documentDto.relatedLots
    }

    private fun getLots(lotsDto: List<LotCnUpdate>): List<Lot> {
        return lotsDto.asSequence().map { convertDtoLotToCnLot(it) }.toList()
    }

    private fun setItemsId(items: List<ItemCnUpdate>) {
        items.forEach { it.id = generationService.getTimeBasedUUID() }
    }

    private fun setLotsId(tender: TenderCnUpdate) {
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
            tender.electronicAuctions?.let { auctions ->
                auctions.details.asSequence().filter { it.relatedLot == lot.id }.forEach { auction ->
                    auction.relatedLot = id
                }
            }
            lot.id = id
        }
    }

    private fun getValueFromLots(lotsDto: List<LotCnUpdate>): Value {
        val currency = lotsDto.elementAt(0).value.currency
        val totalAmount = lotsDto.asSequence()
                .sumByDouble { it.value.amount.toDouble() }
                .toBigDecimal().setScale(2, RoundingMode.HALF_UP)
        return Value(totalAmount, currency)
    }

    private fun getItems(itemsDto: List<ItemCnUpdate>): List<Item> {
        return itemsDto.asSequence().map { convertDtoItemToCnItem(it) }.toList()
    }

    private fun getContractPeriod(lotsDto: List<LotCnUpdate>): ContractPeriod {
        val contractPeriodSet = lotsDto.asSequence().map { it.contractPeriod }.toSet()
        val startDate = contractPeriodSet.minBy { it.startDate }!!.startDate
        val endDate = contractPeriodSet.maxBy { it.endDate }!!.endDate
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

    private fun updatedLots(lots: List<Lot>) {
        lots.asSequence()
                .filter { it.status == LotStatus.PLANNING }
                .forEach { lot ->
                    lot.status = LotStatus.ACTIVE
                    lot.statusDetails = LotStatusDetails.EMPTY
                }
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
