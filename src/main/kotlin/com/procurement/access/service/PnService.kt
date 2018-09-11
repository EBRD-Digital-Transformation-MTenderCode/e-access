package com.procurement.access.service

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.ocds.*
import com.procurement.access.model.dto.ocds.TenderStatusDetails.EMPTY
import com.procurement.access.model.dto.pn.*
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toDate
import com.procurement.access.utils.toJson
import org.springframework.stereotype.Service
import java.math.RoundingMode
import java.time.LocalDateTime

interface PnService {

    fun createPn(stage: String,
                 country: String,
                 pmd: String,
                 owner: String,
                 dateTime: LocalDateTime,
                 pnDto: PnCreate): ResponseDto
}

@Service
class PnServiceImpl(private val generationService: GenerationService,
                    private val tenderProcessDao: TenderProcessDao) : PnService {

    override fun createPn(stage: String,
                          country: String,
                          pmd: String,
                          owner: String,
                          dateTime: LocalDateTime,
                          pnDto: PnCreate): ResponseDto {
        checkLotsCurrency(pnDto)
        checkLotsContractPeriod(pnDto)
        val cpId = generationService.getCpId(country)
        val planningDto = pnDto.planning
        val tenderDto = pnDto.tender
        setItemsId(pnDto.tender)
        setLotsIdAndItemsAndDocumentsRelatedLots(pnDto.tender)
        validateStartDate(pnDto.tender.tenderPeriod.startDate)
        pnDto.tender.procuringEntity.id = generationService.generateOrganizationId(pnDto.tender.procuringEntity)
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
                        status = TenderStatus.PLANNING,
                        statusDetails = EMPTY,
                        classification = tenderDto.classification,
                        mainProcurementCategory = tenderDto.mainProcurementCategory,
                        additionalProcurementCategories = null,
                        procurementMethod = getPmd(pmd),
                        procurementMethodDetails = tenderDto.procurementMethodDetails,
                        procurementMethodRationale = tenderDto.procurementMethodRationale,
                        procurementMethodAdditionalInfo = null,
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
                        awardCriteria = AwardCriteria.PRICE_ONLY,
                        requiresElectronicCatalogue = false,
                        contractPeriod = setContractPeriod(tenderDto.lots, planningDto.budget),
                        tenderPeriod = Period(tenderDto.tenderPeriod.startDate, null),
                        value = getValueFromLots(tenderDto.lots, planningDto.budget.amount),
                        lotGroups = listOf(LotGroup(optionToCombine = false)),
                        lots = setLots(tenderDto.lots),
                        items = setItems(tenderDto.items),
                        documents = setDocuments(tenderDto)
                )
        )

        val entity = getEntity(tp, cpId, stage, dateTime, owner)
        tenderProcessDao.save(entity)
        tp.token = entity.token.toString()
        return ResponseDto(data = tp)
    }

    private fun validateStartDate(startDate: LocalDateTime) {
        val month = startDate.month
        if (month != month.firstMonthOfQuarter()) throw ErrorException(ErrorType.INVALID_START_DATE)
        val day = startDate.dayOfMonth
        if (day != 1) throw ErrorException(ErrorType.INVALID_START_DATE)
    }

    private fun checkLotsCurrency(pn: PnCreate) {
        val budgetCurrency = pn.planning.budget.amount.currency
        pn.tender.lots?.asSequence()?.firstOrNull { it.value.currency != budgetCurrency }?.let {
            throw ErrorException(ErrorType.INVALID_LOT_CURRENCY)
        }
    }

    private fun checkLotsContractPeriod(pn: PnCreate) {
        pn.tender.lots?.forEach { lot ->
            if (lot.contractPeriod.startDate >= lot.contractPeriod.endDate) {
                throw ErrorException(ErrorType.INVALID_LOT_CONTRACT_PERIOD)
            }
            if (lot.contractPeriod.startDate < pn.tender.tenderPeriod.startDate) {
                throw ErrorException(ErrorType.INVALID_LOT_CONTRACT_PERIOD)
            }
        }
    }

    private fun setItemsId(tender: TenderPnCreate) {
        tender.items?.forEach { it.id = generationService.getTimeBasedUUID() }
    }

    private fun setLotsIdAndItemsAndDocumentsRelatedLots(tender: TenderPnCreate) {

        if (tender.lots != null) {
            val lotsId = tender.lots.asSequence().map { it.id }.toSet()
            if (lotsId.size < tender.lots.size) throw ErrorException(ErrorType.INVALID_LOT_ID)
            tender.lots.forEach { lot ->
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
            }
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
            "TEST_RT" -> ProcurementMethod.TEST_RT
            else -> throw ErrorException(ErrorType.INVALID_PMD)
        }
    }

    private fun setContractPeriod(lotsDto: List<LotPnCreate>?, budget: BudgetPnCreate): ContractPeriod? {
        return if (lotsDto != null) {
            val startDate: LocalDateTime = lotsDto.asSequence()
                    .minBy { it.contractPeriod.startDate }
                    ?.contractPeriod!!.startDate
            val endDate: LocalDateTime = lotsDto.asSequence()
                    .maxBy { it.contractPeriod.endDate }
                    ?.contractPeriod!!.endDate
            budget.budgetBreakdown.forEach { bb ->
                if (startDate > bb.period.endDate) throw ErrorException(ErrorType.INVALID_LOT_CONTRACT_PERIOD)
                if (endDate < bb.period.startDate) throw ErrorException(ErrorType.INVALID_LOT_CONTRACT_PERIOD)
            }
            ContractPeriod(startDate, endDate)
        } else {
            null
        }
    }

    private fun getValueFromLots(lotsDto: List<LotPnCreate>?, budgetValue: Value): Value {
        return if (lotsDto != null && lotsDto.isNotEmpty()) {
            val currency = budgetValue.currency
            val totalAmount = lotsDto.asSequence()
                    .sumByDouble { it.value.amount.toDouble() }
                    .toBigDecimal().setScale(2, RoundingMode.HALF_UP)
            if (totalAmount > budgetValue.amount) throw ErrorException(ErrorType.INVALID_LOT_AMOUNT)
            Value(totalAmount, currency)
        } else {
            budgetValue
        }
    }

    private fun setLots(lotsDto: List<LotPnCreate>?): List<Lot> {
        return lotsDto?.asSequence()?.map { convertDtoLotToLot(it) }?.toList() ?: listOf()
    }

    private fun setItems(itemsDto: List<ItemPnCreate>?): List<Item> {
        return itemsDto?.asSequence()?.map { convertDtoItemToItem(it) }?.toList() ?: listOf()
    }

    private fun convertDtoLotToLot(lotDto: LotPnCreate): Lot {
        return Lot(
                id = lotDto.id,
                title = lotDto.title,
                description = lotDto.description,
                status = TenderStatus.PLANNING,
                statusDetails = EMPTY,
                value = lotDto.value,
                options = listOf(Option(false)),
                recurrentProcurement = listOf(RecurrentProcurement(false)),
                renewals = listOf(Renewal(false)),
                variants = listOf(Variant(false)),
                contractPeriod = ContractPeriod(lotDto.contractPeriod.startDate, lotDto.contractPeriod.endDate),
                placeOfPerformance = lotDto.placeOfPerformance
        )
    }

    private fun convertDtoItemToItem(itemDto: ItemPnCreate): Item {
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

    private fun setDocuments(tenderDto: TenderPnCreate): List<Document>? {
        if ((tenderDto.lots == null || tenderDto.lots.isEmpty()) && (tenderDto.documents != null)) {
            if (tenderDto.documents.any { it.relatedLots != null }) {
                throw throw ErrorException(ErrorType.INVALID_DOCS_RELATED_LOTS)
            }
        }
        return tenderDto.documents
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
