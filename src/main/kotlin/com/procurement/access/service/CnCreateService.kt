package com.procurement.access.service

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType.*
import com.procurement.access.model.bpe.CommandMessage
import com.procurement.access.model.bpe.ResponseDto
import com.procurement.access.model.dto.cn.*
import com.procurement.access.model.dto.ocds.*
import com.procurement.access.model.dto.ocds.TenderStatus.ACTIVE
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toDate
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toLocal
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service
import java.math.RoundingMode
import java.time.LocalDateTime

interface CnCreateService {

    fun createCn(cm: CommandMessage): ResponseDto
}

@Service
class CnCreateServiceImpl(private val generationService: GenerationService,
                          private val tenderProcessDao: TenderProcessDao) : CnCreateService {

    override fun createCn(cm: CommandMessage): ResponseDto {
        val country = cm.context.country ?: throw ErrorException(CONTEXT)
        val pmd = cm.context.pmd ?: throw ErrorException(CONTEXT)
        val owner = cm.context.owner ?: throw ErrorException(CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)
        val dateTime = cm.context.startDate?.toLocal() ?: throw ErrorException(CONTEXT)
        val phase = cm.context.phase ?: throw ErrorException(CONTEXT)
        val cnDto = toObject(CnCreate::class.java, cm.data)

        checkLotsCurrency(cnDto)
        checkLotsContractPeriod(cnDto)
        val cpId = generationService.getCpId(country)
        val planningDto = cnDto.planning
        val tenderDto = cnDto.tender
        validateRelatedLots(tenderDto)
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
                        awardCriteria = AwardCriteria.PRICE_ONLY,
                        requiresElectronicCatalogue = false,
                        contractPeriod = setContractPeriod(tenderDto.lots, planningDto.budget),
                        tenderPeriod = tenderDto.tenderPeriod,
                        enquiryPeriod = tenderDto.enquiryPeriod,
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
        return ResponseDto(data = tp)
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

    private fun setLots(lotsDto: List<LotCnCreate>): List<Lot> {
        return lotsDto.asSequence().map { convertDtoLotToLot(it) }.toList()
    }

    private fun setItemsId(tender: TenderCnCreate) {
        tender.items.forEach { it.id = generationService.getTimeBasedUUID() }
    }

    private fun setLotsIdAndItemsAndDocumentsRelatedLots(tender: TenderCnCreate) {
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
            lot.id = id
        }
    }

    private fun validateRelatedLots(tender: TenderCnCreate) {
        val lotsFromCn = tender.lots.asSequence().map { it.id }.toHashSet()
        if (lotsFromCn.size < tender.lots.size) throw ErrorException(INVALID_LOT_ID)
        val lotsFromDocuments = tender.documents.asSequence()
                .filter { it.relatedLots != null }
                .flatMap { it.relatedLots!!.asSequence() }.toHashSet()
        if (lotsFromDocuments.isNotEmpty()) {
            if (!lotsFromCn.containsAll(lotsFromDocuments)) throw ErrorException(INVALID_DOCS_RELATED_LOTS)
        }
        val lotsFromItems = tender.items.asSequence()
                .map { it.relatedLot }.toHashSet()
        if (!lotsFromCn.containsAll(lotsFromItems)) throw ErrorException(INVALID_ITEMS_RELATED_LOTS)
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

    private fun setItems(itemsDto: List<ItemCnCreate>): List<Item> {
        return itemsDto.asSequence().map { convertDtoItemToItem(it) }.toList()
    }

    private fun setContractPeriod(lotsDto: List<LotCnCreate>, budget: BudgetCnCreate): ContractPeriod {
        val startDate: LocalDateTime = lotsDto.asSequence()
                .minBy { it.contractPeriod.startDate }?.contractPeriod?.startDate
                ?: throw ErrorException(INVALID_LOT_CONTRACT_PERIOD)
        val endDate: LocalDateTime = lotsDto.asSequence()
                .maxBy { it.contractPeriod.endDate }?.contractPeriod?.endDate
                ?: throw ErrorException(INVALID_LOT_CONTRACT_PERIOD)
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
