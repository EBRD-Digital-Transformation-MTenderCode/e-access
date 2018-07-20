package com.procurement.access.service

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.cn.CnCreate
import com.procurement.access.model.dto.cn.ItemCnCreate
import com.procurement.access.model.dto.cn.LotCnCreate
import com.procurement.access.model.dto.cn.TenderCnCreate
import com.procurement.access.model.dto.ocds.*
import com.procurement.access.model.dto.ocds.TenderStatus.ACTIVE
import com.procurement.access.model.dto.ocds.TenderStatusDetails.EMPTY
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toDate
import com.procurement.access.utils.toJson
import org.springframework.stereotype.Service
import java.math.RoundingMode
import java.time.LocalDateTime

interface CnService {

    fun createCn(stage: String,
                 country: String,
                 pmd: String,
                 owner: String,
                 dateTime: LocalDateTime,
                 cnDto: CnCreate): ResponseDto
}

@Service
class CnServiceImpl(private val generationService: GenerationService,
                    private val tenderProcessDao: TenderProcessDao) : CnService {

    override fun createCn(stage: String,
                          country: String,
                          pmd: String,
                          owner: String,
                          dateTime: LocalDateTime,
                          cnDto: CnCreate): ResponseDto {
        checkLotsCurrency(cnDto)
        checkLotsContractPeriod(cnDto)
        val cpId = generationService.getCpId(country)
        val planningDto = cnDto.planning
        val tenderDto = cnDto.tender
        setItemsId(tenderDto)
        setLotsIdAndItemsAndDocumentsRelatedLots(tenderDto)
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
                        contractPeriod = setContractPeriod(tenderDto.lots),
                        tenderPeriod = Period(dateTime, tenderDto.tenderPeriod.endDate),
                        value = getValueFromLots(tenderDto.lots, planningDto.budget.amount),
                        lotGroups = listOf(LotGroup(optionToCombine = false)),
                        lots = setLots(tenderDto.lots),
                        items = setItems(tenderDto.items),
                        documents = tenderDto.documents
                )
        )
        val entity = getEntity(tp, cpId, stage, dateTime, owner)
        tenderProcessDao.save(entity)
        tp.token = entity.token.toString()
        return ResponseDto(true, null, tp)
    }

    private fun checkLotsCurrency(cn: CnCreate) {
        val budgetCurrency = cn.planning.budget.amount.currency
        cn.tender.lots.asSequence().firstOrNull { it.value.currency != budgetCurrency }?.let {
            throw ErrorException(ErrorType.INVALID_LOT_CURRENCY)
        }
    }

    private fun checkLotsContractPeriod(cn: CnCreate) {
        val tenderPeriodEndDate = cn.tender.tenderPeriod.endDate
        cn.tender.lots
                .asSequence()
                .firstOrNull {
                    it.contractPeriod.startDate.isAfter(it.contractPeriod.endDate) || !it.contractPeriod.startDate.isAfter(tenderPeriodEndDate)
                }?.let { throw ErrorException(ErrorType.INVALID_LOT_CONTRACT_PERIOD) }
    }

    private fun setLots(lotsDto: List<LotCnCreate>): List<Lot> {
        return lotsDto.asSequence().map { convertDtoLotToCnLot(it) }.toList()
    }

    private fun setItemsId(tender: TenderCnCreate) {
        tender.items.forEach { it.id = generationService.generateTimeBasedUUID().toString() }
    }

    private fun setLotsIdAndItemsAndDocumentsRelatedLots(tender: TenderCnCreate) {
        tender.lots.forEach { lot ->

            val id = generationService.generateTimeBasedUUID().toString()

            tender.items.asSequence()
                    .filter { it.relatedLot == lot.id }
                    .forEach { it.relatedLot = id }

            tender.documents?.forEach { document ->
                document.relatedLots?.let { relatedLots ->
                    if (relatedLots.contains(lot.id)) {
                        relatedLots.remove(lot.id)
                        relatedLots.add(id)
                    }
                }
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
            else -> throw ErrorException(ErrorType.INVALID_PMD)
        }
    }

    private fun getValueFromLots(lotsDto: List<LotCnCreate>, budgetAmount: Value): Value {
        val currency = lotsDto.elementAt(0).value.currency
        val totalAmount = lotsDto.asSequence()
                .sumByDouble { it.value.amount.toDouble() }
                .toBigDecimal().setScale(2, RoundingMode.HALF_UP)
        if (totalAmount > budgetAmount.amount) throw ErrorException(ErrorType.INVALID_LOT_AMOUNT)
        return Value(totalAmount, currency)
    }

    private fun setItems(itemsDto: List<ItemCnCreate>): List<Item> {
        return itemsDto.asSequence().map { convertDtoItemToCnItem(it) }.toList()
    }

    private fun setContractPeriod(lotsDto: List<LotCnCreate>): Period {
        val startDate = lotsDto.asSequence().minBy { it.contractPeriod.startDate }?.contractPeriod?.startDate
        val endDate = lotsDto.asSequence().maxBy { it.contractPeriod.endDate }?.contractPeriod?.endDate
        return Period(startDate!!, endDate!!)
    }

    private fun convertDtoLotToCnLot(lotDto: LotCnCreate): Lot {
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

    private fun convertDtoItemToCnItem(itemDto: ItemCnCreate): Item {
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
