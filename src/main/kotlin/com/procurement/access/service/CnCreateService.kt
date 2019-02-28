package com.procurement.access.service

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.exception.ErrorType.CONTEXT
import com.procurement.access.exception.ErrorType.INVALID_AUCTION_CURRENCY
import com.procurement.access.exception.ErrorType.INVALID_AUCTION_ID
import com.procurement.access.exception.ErrorType.INVALID_AUCTION_MINIMUM
import com.procurement.access.exception.ErrorType.INVALID_AUCTION_RELATED_LOTS
import com.procurement.access.exception.ErrorType.INVALID_DOCS_ID
import com.procurement.access.exception.ErrorType.INVALID_DOCS_RELATED_LOTS
import com.procurement.access.exception.ErrorType.INVALID_ITEMS
import com.procurement.access.exception.ErrorType.INVALID_ITEMS_RELATED_LOTS
import com.procurement.access.exception.ErrorType.INVALID_LOT_AMOUNT
import com.procurement.access.exception.ErrorType.INVALID_LOT_CONTRACT_PERIOD
import com.procurement.access.exception.ErrorType.INVALID_LOT_CURRENCY
import com.procurement.access.exception.ErrorType.INVALID_LOT_ID
import com.procurement.access.exception.ErrorType.INVALID_PMD
import com.procurement.access.model.dto.bpe.CommandMessage
import com.procurement.access.model.dto.bpe.ResponseDto
import com.procurement.access.model.dto.cn.BudgetCnCreate
import com.procurement.access.model.dto.cn.CnCreate
import com.procurement.access.model.dto.cn.ItemCnCreate
import com.procurement.access.model.dto.cn.LotCnCreate
import com.procurement.access.model.dto.cn.TenderCnCreate
import com.procurement.access.model.dto.cn.validate
import com.procurement.access.model.dto.ocds.AcceleratedProcedure
import com.procurement.access.model.dto.ocds.AwardCriteria
import com.procurement.access.model.dto.ocds.Budget
import com.procurement.access.model.dto.ocds.ContractPeriod
import com.procurement.access.model.dto.ocds.DesignContest
import com.procurement.access.model.dto.ocds.Document
import com.procurement.access.model.dto.ocds.DynamicPurchasingSystem
import com.procurement.access.model.dto.ocds.ElectronicAuctionsDetails
import com.procurement.access.model.dto.ocds.ElectronicWorkflows
import com.procurement.access.model.dto.ocds.Framework
import com.procurement.access.model.dto.ocds.Item
import com.procurement.access.model.dto.ocds.JointProcurement
import com.procurement.access.model.dto.ocds.Lot
import com.procurement.access.model.dto.ocds.LotGroup
import com.procurement.access.model.dto.ocds.LotStatus
import com.procurement.access.model.dto.ocds.LotStatusDetails
import com.procurement.access.model.dto.ocds.Option
import com.procurement.access.model.dto.ocds.Planning
import com.procurement.access.model.dto.ocds.ProcedureOutsourcing
import com.procurement.access.model.dto.ocds.ProcurementMethod
import com.procurement.access.model.dto.ocds.RecurrentProcurement
import com.procurement.access.model.dto.ocds.Renewal
import com.procurement.access.model.dto.ocds.SubmissionMethod
import com.procurement.access.model.dto.ocds.Tender
import com.procurement.access.model.dto.ocds.TenderProcess
import com.procurement.access.model.dto.ocds.TenderStatus.ACTIVE
import com.procurement.access.model.dto.ocds.TenderStatusDetails
import com.procurement.access.model.dto.ocds.Value
import com.procurement.access.model.dto.ocds.Variant
import com.procurement.access.model.dto.ocds.validate
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
class CnCreateService(private val generationService: GenerationService,
                      private val tenderProcessDao: TenderProcessDao,
                      private val rulesService: RulesService) {

    fun createCn(cm: CommandMessage): ResponseDto {
        val country = cm.context.country ?: throw ErrorException(CONTEXT)
        val pmd = cm.context.pmd ?: throw ErrorException(CONTEXT)
        val owner = cm.context.owner ?: throw ErrorException(CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)
        val dateTime = cm.context.startDate?.toLocal() ?: throw ErrorException(CONTEXT)
        val phase = cm.context.phase ?: throw ErrorException(CONTEXT)
        val cnDto = toObject(CnCreate::class.java, cm.data).validate()
        validateAuctionsDto(country, pmd, cnDto)

        checkLotsCurrency(cnDto)
        checkLotsContractPeriod(cnDto)
        val cpId = generationService.getCpId(country)
        val planningDto = cnDto.planning
        val tenderDto = cnDto.tender
        validateDtoRelatedLots(tenderDto)
        setItemsId(tenderDto.items)
        setLotsId(tenderDto)
        cnDto.tender.procuringEntity.id = generationService.generateOrganizationId(cnDto.tender.procuringEntity)
        val tp = TenderProcess(
                ocid = cpId,
                token = null,
                planning = Planning(
                        budget = Budget(
                                description = planningDto.budget.description,
                                amount = planningDto.budget.amount,
                                isEuropeanUnionFunded = planningDto.budget.isEuropeanUnionFunded,
                                budgetBreakdown = planningDto.budget.budgetBreakdown
                        ),
                        rationale = planningDto.rationale
                ),
                tender = Tender(
                        id = cpId,
                        title = tenderDto.title,
                        description = tenderDto.description,
                        status = ACTIVE,
                        statusDetails = TenderStatusDetails.fromValue(phase),
                        classification = tenderDto.classification,
                        mainProcurementCategory = tenderDto.mainProcurementCategory,
                        additionalProcurementCategories = null,
                        procurementMethod = getPmd(pmd),
                        procurementMethodDetails = tenderDto.procurementMethodDetails,
                        procurementMethodRationale = tenderDto.procurementMethodRationale,
                        procurementMethodAdditionalInfo = tenderDto.procurementMethodAdditionalInfo,
                        submissionMethod = listOf(SubmissionMethod.ELECTRONIC_SUBMISSION),
                        submissionMethodDetails = tenderDto.submissionMethodDetails,
                        submissionMethodRationale = tenderDto.submissionMethodRationale,
                        submissionLanguages = null,
                        eligibilityCriteria = tenderDto.eligibilityCriteria,
                        acceleratedProcedure = AcceleratedProcedure(isAcceleratedProcedure = false),
                        designContest = DesignContest(serviceContractAward = false),
                        electronicWorkflows = ElectronicWorkflows(useOrdering = false, acceptInvoicing = false, usePayment = false),
                        jointProcurement = JointProcurement(isJointProcurement = false),
                        procedureOutsourcing = ProcedureOutsourcing(procedureOutsourced = false),
                        framework = Framework(isAFramework = false),
                        dynamicPurchasingSystem = DynamicPurchasingSystem(hasDynamicPurchasingSystem = false),
                        legalBasis = tenderDto.legalBasis,
                        procuringEntity = tenderDto.procuringEntity,
                        awardCriteria = tenderDto.awardCriteria ?: AwardCriteria.PRICE_ONLY,
                        requiresElectronicCatalogue = false,
                        contractPeriod = getContractPeriod(tenderDto.lots, planningDto.budget),
                        tenderPeriod = tenderDto.tenderPeriod,
                        enquiryPeriod = tenderDto.enquiryPeriod,
                        value = getValueFromLots(tenderDto.lots, planningDto.budget.amount),
                        lotGroups = listOf(LotGroup(optionToCombine = false)),
                        lots = getLots(tenderDto.lots),
                        items = getItems(tenderDto.items),
                        documents = getDocuments(tenderDto.documents),
                        procurementMethodModalities = tenderDto.procurementMethodModalities,
                        electronicAuctions = tenderDto.electronicAuctions
                )
        )
        val entity = getEntity(tp, cpId, stage, dateTime, owner)
        tenderProcessDao.save(entity)
        tp.token = entity.token.toString()
        return ResponseDto(data = tp)
    }

    private fun validateAuctionsDto(country: String, pmd: String, cnDto: CnCreate) {
        if (rulesService.isAuctionRequired(country, pmd, cnDto.tender.mainProcurementCategory.value)) {
            cnDto.tender.procurementMethodModalities ?: throw ErrorException(ErrorType.INVALID_PMM)
            if (cnDto.tender.procurementMethodModalities.isEmpty()) throw ErrorException(ErrorType.INVALID_PMM)
            cnDto.tender.electronicAuctions ?: throw ErrorException(ErrorType.INVALID_AUCTION_IS_EMPTY)
            cnDto.tender.electronicAuctions.validate()
        }
    }

    private fun checkLotsCurrency(cn: CnCreate) {
        val budgetCurrency = cn.planning.budget.amount.currency
        cn.tender.lots.asSequence().firstOrNull { it.value.currency != budgetCurrency }?.let {
            throw ErrorException(INVALID_LOT_CURRENCY)
        }
    }

    private fun checkLotsContractPeriod(cn: CnCreate) {
        cn.tender.lots.forEach { lot ->
            if (lot.contractPeriod.startDate >= lot.contractPeriod.endDate) {
                throw ErrorException(INVALID_LOT_CONTRACT_PERIOD)
            }
            if (lot.contractPeriod.startDate <= cn.tender.tenderPeriod.endDate) {
                throw ErrorException(INVALID_LOT_CONTRACT_PERIOD)
            }
        }
    }

    private fun getLots(lotsDto: List<LotCnCreate>): List<Lot> {
        return lotsDto.asSequence().map { convertDtoLotToLot(it) }.toList()
    }

    private fun setItemsId(items: List<ItemCnCreate>) {
        val itemsId = items.asSequence().map { it.id }.toHashSet()
        if (itemsId.size != items.size) throw ErrorException(INVALID_ITEMS)
        items.forEach { it.id = generationService.getTimeBasedUUID() }
    }

    private fun setLotsId(tender: TenderCnCreate) {
        tender.lots.forEach { lot ->
            val id = generationService.getTimeBasedUUID()
            tender.items.asSequence()
                    .filter { it.relatedLot == lot.id }
                    .forEach { it.relatedLot = id }
            tender.documents.asSequence()
                    .filter { it.relatedLots != null }
                    .forEach { document ->
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
                    validateAuctionsMinimum(lot.value.amount, lot.value.currency, auction)
                }
            }
            lot.id = id
        }
    }

    private fun validateDtoRelatedLots(tender: TenderCnCreate) {
        val lotsIdSet = tender.lots.asSequence().map { it.id }.toHashSet()
        if (lotsIdSet.size != tender.lots.size) throw ErrorException(INVALID_LOT_ID)
        val lotsFromItemsSet = tender.items.asSequence().map { it.relatedLot }.toHashSet()
        if (lotsIdSet.size != lotsFromItemsSet.size) throw ErrorException(INVALID_ITEMS_RELATED_LOTS)
        if (!lotsIdSet.containsAll(lotsFromItemsSet)) throw ErrorException(INVALID_ITEMS_RELATED_LOTS)
        val lotsFromDocuments = tender.documents.asSequence()
                .filter { it.relatedLots != null }.flatMap { it.relatedLots!!.asSequence() }.toHashSet()
        if (lotsFromDocuments.isNotEmpty()) {
            if (!lotsIdSet.containsAll(lotsFromDocuments)) throw ErrorException(INVALID_DOCS_RELATED_LOTS)
        }
        tender.electronicAuctions?.let { auctions ->
            val auctionIds = auctions.details.asSequence().map { it.id }.toHashSet()
            if (auctionIds.size != auctions.details.size) throw ErrorException(INVALID_AUCTION_ID)
            val lotsFromAuctions = auctions.details.asSequence().map { it.relatedLot }.toHashSet()
            if (lotsFromAuctions.size != auctions.details.size) throw ErrorException(INVALID_AUCTION_RELATED_LOTS)
            if (lotsFromAuctions.size != lotsIdSet.size) throw ErrorException(INVALID_AUCTION_RELATED_LOTS)
            if (!lotsIdSet.containsAll(lotsFromAuctions)) throw ErrorException(INVALID_AUCTION_RELATED_LOTS)
        }
    }

    private fun validateAuctionsMinimum(lotAmount: BigDecimal, lotCurrency: String, auction: ElectronicAuctionsDetails) {
        val lotAmountMinimum = lotAmount.div(BigDecimal(10))
        for (modality in auction.electronicAuctionModalities) {
            if (modality.eligibleMinimumDifference.amount > lotAmountMinimum)
                throw ErrorException(INVALID_AUCTION_MINIMUM)
            if (modality.eligibleMinimumDifference.currency != lotCurrency)
                throw ErrorException(INVALID_AUCTION_CURRENCY)
        }
    }

    private fun getPmd(pmd: String): ProcurementMethod {
        return when (pmd) {
            "MV" -> ProcurementMethod.MV
            "OT" -> ProcurementMethod.OT
            "RT" -> ProcurementMethod.RT
            "SV" -> ProcurementMethod.SV
            "DA" -> ProcurementMethod.DA
            "NP" -> ProcurementMethod.NP
            "FA" -> ProcurementMethod.FA
            "TEST_OT" -> ProcurementMethod.TEST_OT
            "TEST_SV" -> ProcurementMethod.TEST_SV
            "TEST_RT" -> ProcurementMethod.TEST_RT
            "TEST_MV" -> ProcurementMethod.TEST_MV
            else -> throw ErrorException(INVALID_PMD)
        }
    }

    private fun getValueFromLots(lotsDto: List<LotCnCreate>, budgetValue: Value): Value {
        val currency = budgetValue.currency
        val totalAmount = lotsDto.asSequence()
                .sumByDouble { it.value.amount.toDouble() }
                .toBigDecimal().setScale(2, RoundingMode.HALF_UP)
        if (totalAmount > budgetValue.amount) throw ErrorException(INVALID_LOT_AMOUNT)
        return Value(totalAmount, currency)
    }

    private fun getItems(itemsDto: List<ItemCnCreate>): List<Item> {
        return itemsDto.asSequence().map { convertDtoItemToItem(it) }.toList()
    }

    private fun getDocuments(documentsDto: List<Document>): List<Document>? {
        val docsId = documentsDto.asSequence().map { it.id }.toHashSet()
        if (docsId.size != documentsDto.size) throw ErrorException(INVALID_DOCS_ID)
        return documentsDto
    }

    private fun getContractPeriod(lotsDto: List<LotCnCreate>, budget: BudgetCnCreate): ContractPeriod {
        val contractPeriodSet = lotsDto.asSequence().map { it.contractPeriod }.toSet()
        val startDate = contractPeriodSet.minBy { it.startDate }!!.startDate
        val endDate = contractPeriodSet.maxBy { it.endDate }!!.endDate
        budget.budgetBreakdown.forEach { bb ->
            if (startDate > bb.period.endDate) throw ErrorException(INVALID_LOT_CONTRACT_PERIOD)
            if (endDate < bb.period.startDate) throw ErrorException(INVALID_LOT_CONTRACT_PERIOD)
        }
        return ContractPeriod(startDate, endDate)
    }

    private fun convertDtoLotToLot(lotDto: LotCnCreate): Lot {
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
                contractPeriod = ContractPeriod(lotDto.contractPeriod.startDate, lotDto.contractPeriod.endDate),
                placeOfPerformance = lotDto.placeOfPerformance
        )
    }

    private fun convertDtoItemToItem(itemDto: ItemCnCreate): Item {
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
                          cpId: String,
                          stage: String,
                          dateTime: LocalDateTime,
                          owner: String): TenderProcessEntity {
        return TenderProcessEntity(
                cpId = cpId,
                token = generationService.generateRandomUUID(),
                stage = stage,
                owner = owner,
                createdDate = dateTime.toDate(),
                jsonData = toJson(tp)
        )
    }
}
