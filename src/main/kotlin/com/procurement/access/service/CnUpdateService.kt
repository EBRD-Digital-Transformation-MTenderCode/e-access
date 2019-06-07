package com.procurement.access.service

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType.AUCTIONS_CONTAIN_DUPLICATE_RELATED_LOTS
import com.procurement.access.exception.ErrorType.AUCTION_LOT_HAS_INVALID_AMOUNT_VALUE
import com.procurement.access.exception.ErrorType.AUCTION_LOT_HAS_INVALID_CURRENCY_VALUE
import com.procurement.access.exception.ErrorType.CONTEXT
import com.procurement.access.exception.ErrorType.DATA_NOT_FOUND
import com.procurement.access.exception.ErrorType.INVALID_AUCTION_IS_EMPTY
import com.procurement.access.exception.ErrorType.INVALID_AUCTION_IS_NON_EMPTY
import com.procurement.access.exception.ErrorType.INVALID_DOCS_ID
import com.procurement.access.exception.ErrorType.INVALID_DOCS_RELATED_LOTS
import com.procurement.access.exception.ErrorType.INVALID_ITEMS
import com.procurement.access.exception.ErrorType.INVALID_ITEMS_RELATED_LOTS
import com.procurement.access.exception.ErrorType.INVALID_LOT_AMOUNT
import com.procurement.access.exception.ErrorType.INVALID_LOT_CONTRACT_PERIOD
import com.procurement.access.exception.ErrorType.INVALID_LOT_CURRENCY
import com.procurement.access.exception.ErrorType.INVALID_OWNER
import com.procurement.access.exception.ErrorType.INVALID_TOKEN
import com.procurement.access.exception.ErrorType.IS_SUSPENDED
import com.procurement.access.exception.ErrorType.LOT_ID_NOT_MATCH_TO_RELATED_LOT_IN_AUCTIONS
import com.procurement.access.exception.ErrorType.NO_ACTIVE_LOTS
import com.procurement.access.exception.ErrorType.TENDER_CONTAIN_DUPLICATE_LOT_ID
import com.procurement.access.model.dto.bpe.CommandMessage
import com.procurement.access.model.dto.bpe.ResponseDto
import com.procurement.access.model.dto.cn.CnUpdate
import com.procurement.access.model.dto.cn.ItemCnUpdate
import com.procurement.access.model.dto.cn.LotCnUpdate
import com.procurement.access.model.dto.cn.TenderCnUpdate
import com.procurement.access.model.dto.cn.validate
import com.procurement.access.model.dto.ocds.Amendment
import com.procurement.access.model.dto.ocds.Budget
import com.procurement.access.model.dto.ocds.ContractPeriod
import com.procurement.access.model.dto.ocds.Document
import com.procurement.access.model.dto.ocds.ElectronicAuctions
import com.procurement.access.model.dto.ocds.ElectronicAuctionsDetails
import com.procurement.access.model.dto.ocds.Item
import com.procurement.access.model.dto.ocds.Lot
import com.procurement.access.model.dto.ocds.LotStatus
import com.procurement.access.model.dto.ocds.LotStatusDetails
import com.procurement.access.model.dto.ocds.Option
import com.procurement.access.model.dto.ocds.RecurrentProcurement
import com.procurement.access.model.dto.ocds.Renewal
import com.procurement.access.model.dto.ocds.Tender
import com.procurement.access.model.dto.ocds.TenderProcess
import com.procurement.access.model.dto.ocds.TenderStatusDetails.SUSPENDED
import com.procurement.access.model.dto.ocds.Variant
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
class CnUpdateService(private val generationService: GenerationService,
                      private val tenderProcessDao: TenderProcessDao) {

    companion object{
        private val persentEligibleMinimumDifferenceAmount = BigDecimal(0.1)
    }

    fun updateCn(cm: CommandMessage): ResponseDto {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val token = cm.context.token ?: throw ErrorException(CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)
        val owner = cm.context.owner ?: throw ErrorException(CONTEXT)
        val dateTime = cm.context.startDate?.toLocal() ?: throw ErrorException(CONTEXT)
        val cnDto = toObject(CnUpdate::class.java, cm.data).validate()

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(DATA_NOT_FOUND)
        if (entity.owner != owner) throw ErrorException(INVALID_OWNER)
        if (entity.token.toString() != token) throw ErrorException(INVALID_TOKEN)
        val tenderProcess = toObject(TenderProcess::class.java, entity.jsonData)
        validateTenderStatus(tenderProcess)
        val lotsDto = cnDto.tender.lots
        val itemsDto = cnDto.tender.items
        val documentsDto = cnDto.tender.documents
        val lotsDb = tenderProcess.tender.lots
        validateAuctionsDto(tenderProcess.tender, cnDto)
        checkLotsCurrency(lotsDto, tenderProcess.tender.value.currency)
        checkLotsContractPeriod(cnDto)
        val lotsDtoId = lotsDto.asSequence().map { it.id }.toSet()
        val lotsDbId = lotsDb.asSequence().map { it.id }.toSet()
        var newLotsId = lotsDtoId - lotsDbId
        val oldCanceledLotsDbId = lotsDb.asSequence().filter { it.status == LotStatus.CANCELLED }.map { it.id }.toSet()
        val allCanceledLotsId = lotsDbId - lotsDtoId
        val newCanceledLots = allCanceledLotsId - oldCanceledLotsDbId
        validateRelatedLots(lotsDbId, lotsDtoId, itemsDto)
        val activeLots: List<Lot>
        val canceledLots: List<Lot>
        val updatedLots: List<Lot>
        val updatedItems: List<Item>
        newLotsId = setLotsId(cnDto.tender, newLotsId)
        activeLots = getActiveLots(lotsDto = lotsDto, lotsTender = lotsDb, newLotsId = newLotsId)
        setContractPeriod(tenderProcess.tender, activeLots, tenderProcess.planning.budget)
        setTenderValueByActiveLots(tenderProcess.tender, activeLots)
        canceledLots = getCanceledLots(lotsDb, allCanceledLotsId)
        updatedLots = activeLots + canceledLots
        updatedItems = updateItems(tenderProcess.tender.items, itemsDto)
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
            lots = updatedLots
            documents = updateDocuments(this, documentsDto)
            tenderPeriod = cnDto.tender.tenderPeriod
            enquiryPeriod = cnDto.tender.enquiryPeriod
            if (electronicAuctions != null && cnDto.tender.electronicAuctions != null) {
                procurementMethodModalities = cnDto.tender.procurementMethodModalities
                electronicAuctions = cnDto.tender.electronicAuctions
            }
        }
        if (!tenderProcess.tender.lots.any { it.status == LotStatus.ACTIVE }) throw ErrorException(NO_ACTIVE_LOTS)
        tenderProcessDao.save(getEntity(tenderProcess, entity, dateTime))
        if (newCanceledLots.isNotEmpty()) {
            tenderProcess.amendment = Amendment(relatedLots = newCanceledLots)
        }
        tenderProcess.isLotsChanged = (newLotsId.isNotEmpty() || newCanceledLots.isNotEmpty())

        return ResponseDto(data = tenderProcess)
    }

    private fun validateAuctionsDto(tender: Tender, cnDto: CnUpdate) {
        if (tender.electronicAuctions == null) {
            if (cnDto.tender.electronicAuctions != null) throw ErrorException(INVALID_AUCTION_IS_NON_EMPTY)
        } else {
            if (cnDto.tender.electronicAuctions == null) throw ErrorException(INVALID_AUCTION_IS_EMPTY)

            //VR-3.7.15
            val tenderLotsIds = getTenderLotsIds(cnDto.tender)
            val auctionLotsByRelatedLot = getAuctionLotsByRelatedLot(cnDto.tender.electronicAuctions)

            val auctionLotsIds: Set<String> = auctionLotsByRelatedLot.keys
            if (tenderLotsIds.size != auctionLotsIds.size || !tenderLotsIds.all { auctionLotsIds.contains(it) })
                throw ErrorException(LOT_ID_NOT_MATCH_TO_RELATED_LOT_IN_AUCTIONS)

            cnDto.tender.lots.forEach { lot ->
                val auctionLot = auctionLotsByRelatedLot.getValue(lot.id)

                auctionLot.electronicAuctionModalities.forEach {
                    val acceptableEligibleMinimumDifferenceAmount =
                        lot.value.amount.times(persentEligibleMinimumDifferenceAmount)
                            .setScale(2, RoundingMode.HALF_UP)

                    if (it.eligibleMinimumDifference.amount <= BigDecimal.ZERO
                        || it.eligibleMinimumDifference.amount > acceptableEligibleMinimumDifferenceAmount
                    )
                        throw ErrorException(error = AUCTION_LOT_HAS_INVALID_AMOUNT_VALUE)

                    if (it.eligibleMinimumDifference.currency != lot.value.currency)
                        throw ErrorException(error = AUCTION_LOT_HAS_INVALID_CURRENCY_VALUE)
                }
            }
        }
    }

    private fun getTenderLotsIds(tender: TenderCnUpdate): Set<String> = mutableSetOf<String>()
        .apply {
            tender.lots.forEach {
                if (add(it.id) == false)
                    throw ErrorException(TENDER_CONTAIN_DUPLICATE_LOT_ID)

            }
        }

    private fun getAuctionLotsByRelatedLot(
        electronicAuctions: ElectronicAuctions
    ): Map<String, ElectronicAuctionsDetails> = mutableMapOf<String, ElectronicAuctionsDetails>()
        .apply {
            electronicAuctions.details.forEach {
                if (put(it.relatedLot, it) != null)
                    throw ErrorException(AUCTIONS_CONTAIN_DUPLICATE_RELATED_LOTS)
            }
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
        val contractPeriodSet = activeLots.asSequence().map { it.contractPeriod }.toSet()
        val startDate = contractPeriodSet.minBy { it!!.startDate }!!.startDate
        val endDate = contractPeriodSet.maxBy { it!!.endDate }!!.endDate
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

    private fun setLotsId(tender: TenderCnUpdate, newLotsId: Set<String>): Set<String> {
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
                    tender.electronicAuctions?.let { auctions ->
                        auctions.details.asSequence().filter { it.relatedLot == lot.id }.forEach { it.relatedLot = id }
                    }
                    lot.id = id
                    newLotsIdSet.add(id)
                }
        return newLotsIdSet
    }

    private fun validateRelatedLots(lotsDbId: Set<String>, lotsDtoId: Set<String>, items: List<ItemCnUpdate>) {
        val newLotsId = lotsDtoId - lotsDbId
        val lotsFromItems = items.asSequence().map { it.relatedLot }.toHashSet()
        if (!lotsFromItems.containsAll(lotsDtoId)) throw ErrorException(INVALID_ITEMS_RELATED_LOTS) //is all lots have related items
        if (!(lotsDbId + newLotsId).containsAll(lotsFromItems)) throw ErrorException(INVALID_ITEMS_RELATED_LOTS) //is all items have related lots
    }

    private fun validateDocumentsRelatedLots(lots: List<Lot>, documentsDto: List<Document>) {
        val lotsId = lots.asSequence().map { it.id }.toHashSet()
        val lotsFromDocuments = documentsDto.asSequence()
                .filter { it.relatedLots != null }.flatMap { it.relatedLots!!.asSequence() }.toHashSet()
        if (lotsFromDocuments.isNotEmpty()) {
            if (!lotsId.containsAll(lotsFromDocuments)) throw ErrorException(INVALID_DOCS_RELATED_LOTS)
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
        val itemsDtoId = itemsDto.asSequence().map { it.id }.toHashSet()
        if (itemsDtoId.size != itemsDto.size) throw ErrorException(INVALID_ITEMS)
        val itemsDbId = itemsTender.asSequence().map { it.id }.toHashSet()
        if (itemsDtoId.size != itemsDbId.size) throw ErrorException(INVALID_ITEMS)
        if (!itemsDbId.containsAll(itemsDtoId)) throw ErrorException(INVALID_ITEMS)
        //update
        itemsTender.forEach { itemDb -> itemDb.updateItem(itemsDto.first { it.id == itemDb.id }) }
        return itemsTender
    }

    private fun Item.updateItem(itemDto: ItemCnUpdate) {
        this.description = itemDto.description
        this.relatedLot = itemDto.relatedLot
    }

    private fun updateDocuments(tender: Tender, documentsDto: List<Document>): List<Document> {
        val docsId = documentsDto.asSequence().map { it.id }.toHashSet()
        if (docsId.size != documentsDto.size) throw ErrorException(INVALID_DOCS_ID)
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
