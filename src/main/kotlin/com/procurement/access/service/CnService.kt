package com.procurement.access.service

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.cn.Cn
import com.procurement.access.model.dto.cn.LotCn
import com.procurement.access.model.dto.cn.TenderCn
import com.procurement.access.model.dto.cn.request.*
import com.procurement.access.model.dto.ocds.*
import com.procurement.access.model.dto.ocds.TenderStatus.ACTIVE
import com.procurement.access.model.dto.ocds.TenderStatusDetails.EMPTY
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toDate
import com.procurement.access.utils.toJson
import org.springframework.stereotype.Service
import java.math.RoundingMode
import java.time.LocalDateTime
import java.util.*

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
        setItemsId(cnDto.tender)
        setLotsIdAndItemsAndDocumentsRelatedLots(cnDto.tender)
        cnDto.tender.procuringEntity.id = generationService.generateOrganizationId(cnDto.tender.procuringEntity)
        val cn = Cn(
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
                tender = TenderCn(
                        id = cpId,
                        status = ACTIVE,
                        statusDetails = EMPTY,
                        designContest = DesignContest(serviceContractAward = false),
                        electronicWorkflows = ElectronicWorkflows(useOrdering = false, acceptInvoicing = false, usePayment = false),
                        jointProcurement = JointProcurement(isJointProcurement = false),
                        procedureOutsourcing = ProcedureOutsourcing(procedureOutsourced = false),
                        framework = Framework(isAFramework = false),
                        dynamicPurchasingSystem = DynamicPurchasingSystem(hasDynamicPurchasingSystem = false),
                        lotGroups = listOf(LotGroup(optionToCombine = false)),
                        requiresElectronicCatalogue = false,
                        acceleratedProcedure = AcceleratedProcedure(isAcceleratedProcedure = false),
                        awardCriteria = AwardCriteria.PRICE_ONLY,
                        submissionMethod = listOf(SubmissionMethod.ELECTRONIC_SUBMISSION),
                        lots = setLots(tenderDto.lots),
                        procuringEntity = tenderDto.procuringEntity,
                        description = tenderDto.description,
                        procurementMethod = getPmd(pmd),
                        value = getValueFromLots(tenderDto.lots),
                        items = setItems(tenderDto.items),
                        title = tenderDto.title,
                        documents = tenderDto.documents,
                        classification = tenderDto.classification,
                        contractPeriod = setContractPeriod(tenderDto.lots),
                        tenderPeriod = Period(dateTime, tenderDto.tenderPeriod.endDate),
                        submissionMethodRationale = tenderDto.submissionMethodRationale,
                        submissionLanguages = null,
                        procurementMethodAdditionalInfo = null,
                        legalBasis = tenderDto.legalBasis,
                        eligibilityCriteria = tenderDto.eligibilityCriteria,
                        submissionMethodDetails = tenderDto.submissionMethodDetails,
                        additionalProcurementCategories = null,
                        mainProcurementCategory = tenderDto.mainProcurementCategory,
                        procurementMethodRationale = tenderDto.procurementMethodRationale,
                        procurementMethodDetails = tenderDto.procurementMethodDetails
                )
        )
        val entity = getEntity(cn, cpId, stage, dateTime, owner)
        tenderProcessDao.save(entity)
        cn.token = entity.token.toString()
        return ResponseDto(true, null, cn)
    }

    private fun checkLotsCurrency(cn: CnCreate) {
        val budgetCurrency = cn.planning.budget.amount.currency
        cn.tender.lots.asSequence().firstOrNull { it.value.currency != budgetCurrency }?.let {
            throw ErrorException(ErrorType.INVALID_CURRENCY)
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

    private fun setLots(lotsDto: HashSet<LotCnCreate>): HashSet<LotCn> {
        return lotsDto.asSequence().map { convertDtoLotToCnLot(it) }.toHashSet()
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
            tender.documents?.asSequence()
                    ?.filter { it.relatedLots != null }
                    ?.filter { it.relatedLots!!.contains(lot.id) }
                    ?.forEach { it.relatedLots!!.minus(lot.id).plus(id) }
            lot.id = id
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

    private fun getValueFromLots(lotsDto: HashSet<LotCnCreate>): Value {
        val currency = lotsDto.elementAt(0).value.currency
        val totalAmount = lotsDto.asSequence()
                .sumByDouble { it.value.amount.toDouble() }
                .toBigDecimal().setScale(2, RoundingMode.HALF_UP)
        return Value(totalAmount, currency)
    }

    private fun setItems(itemsDto: HashSet<ItemCnCreate>): HashSet<Item> {
        return itemsDto.asSequence().map { convertDtoItemToCnItem(it) }.toHashSet()
    }

    private fun setContractPeriod(lotsDto: HashSet<LotCnCreate>): Period {
        val startDate = lotsDto.asSequence().minBy { it.contractPeriod.startDate }?.contractPeriod?.startDate
        val endDate = lotsDto.asSequence().maxBy { it.contractPeriod.endDate }?.contractPeriod?.endDate
        return Period(startDate!!, endDate!!)
    }

    private fun convertDtoLotToCnLot(lotDto: LotCnCreate): LotCn {
        return LotCn(
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

    private fun getEntity(dto: Cn,
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
                jsonData = toJson(dto)
        )
    }
}
