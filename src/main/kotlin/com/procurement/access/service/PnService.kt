package com.procurement.access.service

import com.procurement.access.application.model.MainMode
import com.procurement.access.application.model.TestMode
import com.procurement.access.application.service.pn.create.CreatePnContext
import com.procurement.access.application.service.pn.create.PnCreateData
import com.procurement.access.application.service.pn.create.PnCreateResult
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.DocumentType
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.domain.model.enums.ProcurementMethod
import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.domain.model.enums.SubmissionMethod
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import com.procurement.access.domain.model.money.Money
import com.procurement.access.domain.util.extension.nowDefaultUTC
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.exception.ErrorType.CONTEXT
import com.procurement.access.infrastructure.api.v1.CommandMessage
import com.procurement.access.infrastructure.api.v1.startDate
import com.procurement.access.infrastructure.api.v1.testMode
import com.procurement.access.infrastructure.entity.PNEntity
import com.procurement.access.infrastructure.repository.CassandraTenderProcessRepositoryV1
import com.procurement.access.lib.extension.getDuplicate
import com.procurement.access.lib.extension.isUnique
import com.procurement.access.lib.extension.toSet
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toJson
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.util.*

@Service
class PnService(
    private val generationService: GenerationService,
    private val tenderRepository: CassandraTenderProcessRepositoryV1
) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(PnService::class.java)
    }

    private val allowedTenderDocumentTypes = DocumentType.allowedElements
        .filter {
            when (it) {
                DocumentType.TENDER_NOTICE,
                DocumentType.BIDDING_DOCUMENTS,
                DocumentType.TECHNICAL_SPECIFICATIONS,
                DocumentType.EVALUATION_CRITERIA,
                DocumentType.CLARIFICATIONS,
                DocumentType.ELIGIBILITY_CRITERIA,
                DocumentType.RISK_PROVISIONS,
                DocumentType.BILL_OF_QUANTITY,
                DocumentType.CONFLICT_OF_INTEREST,
                DocumentType.PROCUREMENT_PLAN,
                DocumentType.CONTRACT_DRAFT,
                DocumentType.COMPLAINTS,
                DocumentType.ILLUSTRATION,
                DocumentType.CANCELLATION_DETAILS,
                DocumentType.EVALUATION_REPORTS,
                DocumentType.SHORTLISTED_FIRMS,
                DocumentType.CONTRACT_ARRANGEMENTS,
                DocumentType.CONTRACT_GUARANTEES -> true

                DocumentType.ASSET_AND_LIABILITY_ASSESSMENT,
                DocumentType.ENVIRONMENTAL_IMPACT,
                DocumentType.FEASIBILITY_STUDY,
                DocumentType.HEARING_NOTICE,
                DocumentType.MARKET_STUDIES,
                DocumentType.NEEDS_ASSESSMENT,
                DocumentType.PROJECT_PLAN -> false
            }
        }.toSet()

    fun createPn(contextRequest: CreatePnContext, request: PnCreateData): PnCreateResult {
        checkValidationRules(request, contextRequest)
        request.validateDuplicates()

        val cpid = generationService.generateCpid(contextRequest.mode.prefix, contextRequest.country, nowDefaultUTC())
        val ocid = generationService.generateOcid(cpid, Stage.PN.key)
        val pnEntity: PNEntity = businessRules(contextRequest, request, ocid)

        val token = generationService.generateToken()
        tenderRepository.save(
            TenderProcessEntity(
                cpId = cpid,
                token = token,
                ocid = ocid,
                owner = contextRequest.owner,
                createdDate = contextRequest.startDate,
                jsonData = toJson(pnEntity)
            )
        )
        return getResponse(pnEntity, token, cpid)
    }

    /**
     * Validation rules
     */
    private fun checkValidationRules(request: PnCreateData, context: CreatePnContext) {
        //VR-3.1.16
        if (request.tender.title.isBlank())
            throw ErrorException(
                error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                message = "The attribute 'tender.title' is empty or blank."
            )

        //VR-3.1.17
        if (request.tender.description.isBlank())
            throw ErrorException(
                error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                message = "The attribute 'tender.description' is empty or blank."
            )

        //VR-3.1.6 Tender Period: Start Date
        checkTenderPeriod(tenderPeriod = request.tender.tenderPeriod)

        val documents = request.tender.documents

        //VR-3.1.1
        val isUniqueDocuments = documents.isUnique { it.id }
        if (!isUniqueDocuments) throw ErrorException(ErrorType.DOCUMENT_ID_DUPLICATED)

        val documentWithRelatedLots = documents.associate { it.id to it.relatedLots }
        val receivedLotIds = request.tender.lots.toSet { it.id }
        checkDocumentsRelationWithLot(documentWithRelatedLots, receivedLotIds)

        //VR-3.6.1
        checkTenderDocumentsTypes(request)

        val lots: List<PnCreateData.Tender.Lot> = request.tender.lots
        val items = request.tender.items
        if (items.isEmpty()) {
            if (lots.isNotEmpty()) throw ErrorException(ErrorType.EMPTY_ITEMS)
        } else {
            if (lots.isEmpty()) throw ErrorException(ErrorType.EMPTY_LOTS)

            //VR-3.1.8
            checkQuantityInItems(items)

            //VR-3.1.14
            checkLotIdFromRequest(lots = lots)

            //VR-3.1.15
            checkItemIdFromRequest(items = items)

            //VR-3.1.12 Lots
            checkLotIdsAsRelatedLotInItems(lots = lots, items = items)

            val lotsIds = lots.asSequence().map { it.id }.toSet()

            //VR-3.1.13 Items
            checkRelatedLotInItems(lotsIds, items)

            //VR-3.1.4 "Value" (tender)
            val tenderValue = calculateTenderValueFromLots(lots = lots)
            checkTenderValue(tenderAmount = tenderValue.amount, budgetAmount = request.planning.budget.amount)

            //VR-3.1.7 "Currency" (lot)
            checkCurrencyInLotsFromRequest(lots = lots, budget = request.planning.budget)

            //VR-3.1.9 "Contract Period" (Tender)
            checkContractPeriodInTender(lots, request.planning.budget.budgetBreakdowns)

            //VR-3.1.10 "Related Lots" (documents)
            checkRelatedLotsInDocuments(lotsIds, documents)

            //VR-3.1.11 "Contract Period" (Lot)
            checkContractPeriodInLots(lots, request.tender.tenderPeriod.startDate)
        }

        checkProcuringEntity(context.pmd, request.tender.procuringEntity)
    }

    private fun checkProcuringEntity(pmd: ProcurementMethod, procuringEntity: PnCreateData.Tender.ProcuringEntity?) {
        // VR-3.1.19
        val isProcuringEntityNeed = isProcuringEntityNeed(pmd)
        isProcuringEntityNeed.ifPresent { isNeed ->
            when {
                isNeed && procuringEntity == null ->
                    throw ErrorException(ErrorType.MISSING_ATTRIBUTE, "Missing 'procuringEntity' in request.")

                !isNeed && procuringEntity != null ->
                    throw ErrorException(ErrorType.INCORRECT_VALUE_ATTRIBUTE, "Unexpected 'procuringEntity' attribute in request.")
            }
        }
    }

    private fun isProcuringEntityNeed(pmd: ProcurementMethod): Optional<Boolean> =
        when (pmd) {
            ProcurementMethod.OT, ProcurementMethod.TEST_OT,
            ProcurementMethod.SV, ProcurementMethod.TEST_SV,
            ProcurementMethod.MV, ProcurementMethod.TEST_MV,
            ProcurementMethod.DA, ProcurementMethod.TEST_DA,
            ProcurementMethod.NP, ProcurementMethod.TEST_NP,
            ProcurementMethod.CD, ProcurementMethod.TEST_CD,
            ProcurementMethod.DC, ProcurementMethod.TEST_DC,
            ProcurementMethod.IP, ProcurementMethod.TEST_IP,
            ProcurementMethod.GPA, ProcurementMethod.TEST_GPA,
            ProcurementMethod.RT, ProcurementMethod.TEST_RT -> Optional.of(true)

            ProcurementMethod.MC, ProcurementMethod.TEST_MC,
            ProcurementMethod.DCO, ProcurementMethod.TEST_DCO,
            ProcurementMethod.RFQ, ProcurementMethod.TEST_RFQ -> Optional.of(false)

            ProcurementMethod.OP, ProcurementMethod.TEST_OP,
            ProcurementMethod.CF, ProcurementMethod.TEST_CF,
            ProcurementMethod.OF, ProcurementMethod.TEST_OF,
            ProcurementMethod.FA, ProcurementMethod.TEST_FA -> Optional.empty()
        }

    private fun PnCreateData.validateDuplicates() {
        tender.items
            .forEachIndexed { index, item ->
                val duplicateAdditionalClassification =
                    item.additionalClassifications.getDuplicate { it.scheme.key + it.id.toUpperCase() }

                if (duplicateAdditionalClassification != null)
                    throw ErrorException(
                        error = ErrorType.DUPLICATE,
                        message = "Attribute 'tender.items[$index].additionalClassifications' has duplicate by scheme '${duplicateAdditionalClassification.scheme}' and id '${duplicateAdditionalClassification.id}'."
                    )
            }
    }

    fun checkDocumentsRelationWithLot(documents: Map<String, List<String>>, lotsIds: Set<String>) {
        documents.forEach { (documentId, relatedLots) ->
            relatedLots.forEach { relatedLot ->
                if (relatedLot !in lotsIds)
                    throw ErrorException(
                        error = ErrorType.INVALID_DOCS_RELATED_LOTS,
                        message = "Cannot find lot (id '${relatedLot}') assigned to documents['${documentId}'].relatedLot"
                    )
            }
        }
    }

    /**
     * VR-3.1.6 Tender Period: Start Date
     *
     * eAccess проверяет что, в поле Tender.tenderPeriod.startDate зафиксировано первое календарное число
     * каждого месяца:
     *
     * IF 1 месяц,  tenderPeriod.startDate == "YYYY-01-01Thh:mm:ssZ"
     * IF 2 месяц,  tenderPeriod.startDate == "YYYY-02-01Thh:mm:ssZ"
     * IF 3 месяц,  tenderPeriod.startDate == "YYYY-03-01Thh:mm:ssZ"
     * IF 4 месяц,  tenderPeriod.startDate == "YYYY-04-01Thh:mm:ssZ"
     * IF 5 месяц,  tenderPeriod.startDate == "YYYY-05-01Thh:mm:ssZ"
     * IF 6 месяц,  tenderPeriod.startDate == "YYYY-06-01Thh:mm:ssZ"
     * IF 7 месяц,  tenderPeriod.startDate == "YYYY-07-01Thh:mm:ssZ"
     * IF 8 месяц,  tenderPeriod.startDate == "YYYY-08-01Thh:mm:ssZ"
     * IF 9 месяц,  tenderPeriod.startDate == "YYYY-09-01Thh:mm:ssZ"
     * IF 10 месяц, tenderPeriod.startDate == "YYYY-10-01Thh:mm:ssZ"
     * IF 11 месяц, tenderPeriod.startDate == "YYYY-11-01Thh:mm:ssZ"
     * IF 12 месяц, tenderPeriod.startDate == "YYYY-12-01Thh:mm:ssZ"
     */
    private fun checkTenderPeriod(tenderPeriod: PnCreateData.Tender.TenderPeriod) {
        if (tenderPeriod.startDate.dayOfMonth != 1)
            throw ErrorException(ErrorType.INVALID_START_DATE)
    }

    private fun checkTenderDocumentsTypes(data: PnCreateData) {
        data.tender.documents
            .map { document ->
                if (document.documentType !in allowedTenderDocumentTypes)
                    throw ErrorException(
                        error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                        message = "Tender document '${document.id}' contains incorrect documentType '${document.documentType}'. Allowed values: '${allowedTenderDocumentTypes.joinToString()}'"
                    )
            }
    }

    /**
     * VR-3.1.4 (PN) "Value" (tender) -> VR-3.6.5
     *
     * eAccess проверяет, что "Value" (tender/value/amount), рассчитанное по правилу BR-3.6.30,  меньше / ровно значения
     * поля «Budget Value» (budget/amount/amount) запроса.
     */
    private fun checkTenderValue(tenderAmount: BigDecimal, budgetAmount: Money) {
        if (tenderAmount > budgetAmount.amount)
            throw ErrorException(
                error = ErrorType.INVALID_TENDER_AMOUNT,
                message = "The amount of the tender [$tenderAmount] more that the amount of the budget [${budgetAmount.amount}]."
            )
    }

    /**
     * VR-3.1.7 (PN)  "Currency" (lot) -> VR-3.6.6
     *
     * eAccess проверяет, что значение "Currency" (tender.lot.value.currency) from Request == "Currency"
     * (budget.amount.currency) from Request.
     */
    private fun checkCurrencyInLotsFromRequest(
        lots: List<PnCreateData.Tender.Lot>,
        budget: PnCreateData.Planning.Budget
    ) {
        lots.forEach { lot ->
            if (lot.value.currency != budget.amount.currency)
                throw ErrorException(
                    error = ErrorType.INVALID_LOT_CURRENCY,
                    message = "Lot with id: '${lot.id}' contains invalid currency (lot currency: '${lot.value.currency}', budget amount currency: '${budget.amount.currency}')"
                )
        }
    }

    /**
     * VR-3.1.9 "Contract Period" (Tender) -> VR-3.6.10
     *
     * eAccess проверяет, что значение ContractPeriod.StartDate (tender/contractPeriod/startDate),
     * определенное по правилу BR-3.6.31, -  меньше или равно каждому значению
     * budgetBreakdown.period.endDate AND ContractPeriod.EndDate (tender/contractPeriod/endDate)
     * больше или равен каждому значению budgetBreakdown.period.startDate добавляемых FS:
     *
     * tender.contractPeriod.startdate  <= planning.budget.budgetBreakdown.period.endDate
     * tender.contractPeriod.enddate  >= planning.budget.budgetBreakdown.period.startDate
     *
     * Пример:
     * tender.contractPeriod.startdate = 10.06.2017 AND tender.contractPeriod.enddate = 30.10.2017
     * Period of budgetBreakdown1 [01.06.2017 - 10.08.2017] - budgetBreakdown is OK
     * Period of budgetBreakdown2 [31.10.2017 - 30.11.2017] - budgetBreakdown isn't OK
     * Period of budgetBreakdown3 [01.01.2017 - 09.06.2017] - budgetBreakdown isn't OK
     * Period of budgetBreakdown4 [03.03.2017 - 10.06.2017] - budgetBreakdown is OK
     */
    private fun checkContractPeriodInTender(
        lots: List<PnCreateData.Tender.Lot>,
        budgetBreakdowns: List<PnCreateData.Planning.Budget.BudgetBreakdown>
    ) {
        val contractPeriod = contractPeriod(lots)
        budgetBreakdowns.forEach { budgetBreakdown ->
            if (contractPeriod.startDate > budgetBreakdown.period.endDate)
                throw ErrorException(
                    error = ErrorType.INVALID_LOT_CONTRACT_PERIOD,
                    message = "The start date of the tender contract period [${contractPeriod.startDate}] after than the end date of the budget breakdown period [${budgetBreakdown.period.endDate}]"
                )
            if (contractPeriod.endDate < budgetBreakdown.period.startDate)
                throw ErrorException(
                    error = ErrorType.INVALID_LOT_CONTRACT_PERIOD,
                    message = "The end date of the tender contract period [${contractPeriod.endDate}] before than the start date of the budget breakdown period [${budgetBreakdown.period.startDate}]"
                )
        }
    }

    /**
     * VR-3.1.10 "Related Lots" (documents) -> VR-3.6.12
     *
     * Access проверяет, что значения указанные в поле relatedLots (document.relatedLots) каждого объекта
     * секции Documents имеют соответствие в списке значений tender.lots.id.
     */
    private fun checkRelatedLotsInDocuments(lotsIds: Set<String>, documents: List<PnCreateData.Tender.Document>) {
        documents.forEach { document ->
            document.relatedLots.forEach { relatedLot ->
                if (relatedLot !in lotsIds)
                    throw ErrorException(ErrorType.INVALID_DOCS_RELATED_LOTS)
            }
        }
    }

    /**
     * VR-3.1.11 "Contract Period" (Lot)
     *
     * - eAccess проверяет, что значение "Contract Period: Start Date" (tender/lots/contractPeriod/startDate)
     *   является более ранним по сравнению с "Contract Period: End Date" (tender/lots/contractPeriod/endDate)
     *   в данном объекте секции Lots и более поздним по сравнению с датой, указанной в поле «Tender Period: Start Date»
     *   (tender/tenderPeriod/startDate).
     *
     * - eAccess проверяет, что значение "Contract Period: End Date" (tender/lots/contractPeriod/endDate) является более
     *   поздним по сравнению с "Contract Period: Start Date" (tender/lots/contractPeriod/startDate)
     *   в данном объекте секции Lots.
     *
     * - eAccess checks startDate && endDate values:
     *   IF startDate && endDate value are present in calendar of current year, validation is successful;
     *   ELSE (startDate && endDate value are not found in calendar) { eAccess throws Exception: "Date is not exist";
     */
    private fun checkContractPeriodInLots(lots: List<PnCreateData.Tender.Lot>, tenderPeriodStartDate: LocalDateTime) {
        lots.forEach { lot ->
            checkRangeContractPeriodInLot(lot)

            if (lot.contractPeriod.startDate <= tenderPeriodStartDate)
                throw ErrorException(
                    error = ErrorType.INVALID_LOT_CONTRACT_PERIOD,
                    message = "The start date [${lot.contractPeriod.startDate}] of the contract period of the lot [${lot.id}] before or eq that the end date of the tender period [$tenderPeriodStartDate]."
                )
        }
    }

    private fun checkAdditionalIdentifiersInProcuringEntity(procuringEntity: PnCreateData.Tender.ProcuringEntity) {
        val isAdditionalIdentifiersUnique = procuringEntity.additionalIdentifiers.isUnique { Pair(it.scheme, it.id) }
        if (!isAdditionalIdentifiersUnique)
            throw ErrorException(ErrorType.INVALID_PROCURING_ENTITY, "Additional identifiers of procuring entity are duplicated")
    }

    private fun checkRangeContractPeriodInLot(lot: PnCreateData.Tender.Lot) {
        if (lot.contractPeriod.startDate >= lot.contractPeriod.endDate)
            throw ErrorException(ErrorType.INVALID_LOT_CONTRACT_PERIOD)
    }

    /**
     * VR-3.1.12 Lots
     *
     * eAccess проверяет:
     * 1. В каждом объекте секции Lots заполнен следующий набор полей:
     *      "Id" (tender/lots/id) = MANDATORY
     *      "Title" (tender/lots/title) = MANDATORY
     *      "Description" (tender/lots/description) = MANDATORY
     *      "Amount" (tender/lots/value/amount) = MANDATORY
     *      "Currency" (tender/lots/value/currency) = MANDATORY
     *      "Contract Period" (tender/lots/contractPeriod) = MANDATORY
     * 2. Checks the quantity of Lot object in Request:
     *      IF quantity of Lot object in Request > 0, validation is successful;
     *      ELSE eAccess throws Exception;
     * 3. Analyzes Lot.ID from Request:
     *      IF all lot.ID from Request are presented in list of values item.relatedLot from Request (at least once),
     *      validation is successful;
     *      ELSE eAccess throws Exception;
     */
    private fun checkLotIdsAsRelatedLotInItems(
        lots: List<PnCreateData.Tender.Lot>,
        items: List<PnCreateData.Tender.Item>
    ) {
        if (lots.isEmpty())
            throw ErrorException(ErrorType.EMPTY_LOTS)

        val lotsIds = lots.toSet { it.id }
        val itemsRelatedLots: Set<String> = items.toSet { it.relatedLot }
        lotsIds.forEach { lotId ->
            if (lotId !in itemsRelatedLots)
                throw ErrorException(
                    error = ErrorType.LOT_ID_NOT_MATCH_TO_RELATED_LOT_IN_ITEMS,
                    message = ""
                )
        }
    }

    /**
     * VR-3.1.13 Items
     *
     * eAccess проверяет, что:
     * 1. В каждом объекте секции заполнен следующий набор полей:
     *      "Classification" (tender/items/classification) = MANDATORY
     *      "Id" (tender/items/id) = MANDATORY
     *      "Unit" (tender/items/unit) = MANDATORY
     *      "Quantity" (tender/items/quantity) = MANDATORY
     *      "Related Lot" (tender/items/relatedLot) = MANDATORY
     * 2. Используется Lots section (tender/lots).
     * 3. eAccess проверяет, что значению "Related Lot" (tender/items/relatedLot) каждого объекта секции Items запроса
     *    соответствует объект секции Lots из запроса по полю "Id" (tender/lots/id).
     */
    private fun checkRelatedLotInItems(lotsIds: Set<String>, items: List<PnCreateData.Tender.Item>) {
        items.forEach { item ->
            val relatedLot = item.relatedLot
            if (relatedLot !in lotsIds)
                throw ErrorException(ErrorType.INVALID_ITEMS_RELATED_LOTS)
        }
    }

    /**
     * VR-3.1.8
     */
    private fun checkQuantityInItems(items: List<PnCreateData.Tender.Item>) {
        items.forEach {item ->
            if(item.quantity <= BigDecimal.ZERO)
                throw ErrorException(ErrorType.INVALID_ITEMS_QUANTITY)
        }
    }

    /**
     * VR-3.1.14 Lot.ID
     *
     * eAccess analyzes Lot.ID from Request:
     * IF every lot.ID from Request is included once in list from Request, validation is successful;
     * ELSE eAccess throws Exception;
     */
    private fun checkLotIdFromRequest(lots: List<PnCreateData.Tender.Lot>) {
        val idsAreUniques = lots.isUnique { it.id }
        if (idsAreUniques.not())
            throw throw ErrorException(ErrorType.LOT_ID_DUPLICATED)
    }

    /**
     * VR-3.1.15 Item.ID
     *
     * eAccess analyzes item.ID from Request:
     * IF every item.ID from Request is included once in list from Request, validation is successful;
     * ELSE eAccess throws Exception;
     */
    private fun checkItemIdFromRequest(items: List<PnCreateData.Tender.Item>) {
        val idsAreUniques = items.isUnique { it.id }
        if (idsAreUniques.not())
            throw throw ErrorException(ErrorType.ITEM_ID_DUPLICATED)
    }

    /**
     * Business rules
     */
    private fun businessRules(contextRequest: CreatePnContext, request: PnCreateData, ocid: Ocid.SingleStage): PNEntity {
        val invalidIds = request.planning.budget.budgetBreakdowns
            .asSequence()
            .map { it.id }
            .filter { !contextRequest.mode.pattern.containsMatchIn(it) }
            .toList()

        if (invalidIds.isNotEmpty()) {
            when (contextRequest.mode) {
                is TestMode -> throw ErrorException(
                    error = ErrorType.INVALID_FS,
                    message = """Cannot create test PN based on non test FS. Invalid ids: ${invalidIds}}. """
                )
                is MainMode -> throw ErrorException(
                    error = ErrorType.INVALID_FS,
                    message = """Cannot create PN based on test FS. Invalid ids: ${invalidIds}}. """
                )
            }
        }

        val contractPeriod: PNEntity.Tender.ContractPeriod?
        val value: PNEntity.Tender.Value
        val lots: List<PNEntity.Tender.Lot>
        val items: List<PNEntity.Tender.Item>
        val documents: List<PNEntity.Tender.Document>?

        if (request.tender.items.isNullOrEmpty()) {
            contractPeriod = null

            //BR-3.1.28
            value = calculateTenderValueFromBudget(request.planning.budget)

            lots = emptyList()
            items = emptyList()

            documents = request.tender.documents.map { document ->
                convertRequestDocument(document)
            }
        } else {
            contractPeriod = contractPeriod(request.tender.lots)

            //BR-3.1.25
            value = calculateTenderValueFromLots(request.tender.lots)

            val relatedTemporalWithPermanentLotId: Map<String, String> = generatePermanentLotId(request.tender.lots)
            lots = convertRequestLots(request.tender.lots, relatedTemporalWithPermanentLotId)
            items = convertRequestItems(request.tender.items, relatedTemporalWithPermanentLotId)

            documents = request.tender.documents.map { document ->
                convertRequestDocument(document, relatedTemporalWithPermanentLotId)
            }
        }

        //VR-3.1.18
        request.tender.procuringEntity?.let { checkAdditionalIdentifiersInProcuringEntity(it) }

        val buyer = if (needToGenerateBuyer(contextRequest.pmd))
            generateBuyer(request)
        else null

        return PNEntity(
            ocid = ocid.value,
            planning = planning(request),
            tender = tender(
                pmd = contextRequest.pmd,
                value = value,
                lots = lots,
                items = items,
                contractPeriod = contractPeriod,
                documents = documents,
                tenderRequest = request.tender
            ),
            relatedProcesses = null,
            buyer = buyer
        )
    }

    private fun needToGenerateBuyer(pmd: ProcurementMethod) = when (pmd) {
        ProcurementMethod.RFQ, ProcurementMethod.TEST_RFQ,
        ProcurementMethod.DCO, ProcurementMethod.TEST_DCO,
        ProcurementMethod.MC, ProcurementMethod.TEST_MC -> true

        ProcurementMethod.CD, ProcurementMethod.TEST_CD,
        ProcurementMethod.CF, ProcurementMethod.TEST_CF,
        ProcurementMethod.DA, ProcurementMethod.TEST_DA,
        ProcurementMethod.DC, ProcurementMethod.TEST_DC,
        ProcurementMethod.FA, ProcurementMethod.TEST_FA,
        ProcurementMethod.GPA, ProcurementMethod.TEST_GPA,
        ProcurementMethod.IP, ProcurementMethod.TEST_IP,
        ProcurementMethod.MV, ProcurementMethod.TEST_MV,
        ProcurementMethod.NP, ProcurementMethod.TEST_NP,
        ProcurementMethod.OF, ProcurementMethod.TEST_OF,
        ProcurementMethod.OP, ProcurementMethod.TEST_OP,
        ProcurementMethod.OT, ProcurementMethod.TEST_OT,
        ProcurementMethod.RT, ProcurementMethod.TEST_RT,
        ProcurementMethod.SV, ProcurementMethod.TEST_SV -> false
    }

    private fun generateBuyer(request: PnCreateData) =
        request.buyer.let { buyer ->
            PNEntity.Buyer(
                id = buyer.id,
                name = buyer.name,
                details = buyer.details
                    ?.let { details ->
                        PNEntity.Buyer.Details(
                            typeOfBuyer = details.typeOfBuyer,
                            mainGeneralActivity = details.mainGeneralActivity,
                            mainSectoralActivity = details.mainSectoralActivity
                        )
                    },
                additionalIdentifiers = buyer.additionalIdentifiers
                    ?.map { additionalIdentifier ->
                        PNEntity.Buyer.AdditionalIdentifier(
                            id = additionalIdentifier.id,
                            legalName = additionalIdentifier.legalName,
                            scheme = additionalIdentifier.scheme,
                            uri = additionalIdentifier.uri
                        )
                    },
                address = buyer.address
                    .let { address ->
                        PNEntity.Buyer.Address(
                            streetAddress = address.streetAddress,
                            postalCode = address.postalCode,
                            addressDetails = address.addressDetails.let { addressDetails ->
                                PNEntity.Buyer.Address.AddressDetails(
                                    country = addressDetails.country.let { country ->
                                        PNEntity.Buyer.Address.AddressDetails.Country(
                                            scheme = country.scheme,
                                            id = country.id,
                                            description = country.description,
                                            uri = country.uri
                                        )
                                    },
                                    region = addressDetails.region.let { region ->
                                        PNEntity.Buyer.Address.AddressDetails.Region(
                                            scheme = region.scheme,
                                            id = region.id,
                                            description = region.description,
                                            uri = region.uri
                                        )
                                    },
                                    locality = addressDetails.locality.let { locality ->
                                        PNEntity.Buyer.Address.AddressDetails.Locality(
                                            scheme = locality.scheme,
                                            id = locality.id,
                                            description = locality.description,
                                            uri = locality.uri
                                        )
                                    }
                                )
                            }
                        )
                    },
                contactPoint = buyer.contactPoint
                    .let { contactPoint ->
                        PNEntity.Buyer.ContactPoint(
                            name = contactPoint.name,
                            email = contactPoint.email,
                            faxNumber = contactPoint.faxNumber,
                            telephone = contactPoint.telephone,
                            url = contactPoint.url
                        )
                    },
                identifier = buyer.identifier
                    .let { identifier ->
                        PNEntity.Buyer.Identifier(
                            id = identifier.id,
                            scheme = identifier.scheme,
                            uri = identifier.uri,
                            legalName = identifier.legalName
                        )
                    }
            )
        }

    private fun planning(request: PnCreateData): PNEntity.Planning {
        return request.planning.let { planning ->
            PNEntity.Planning(
                rationale = planning.rationale,
                budget = planning.budget.let { budget ->
                    PNEntity.Planning.Budget(
                        description = budget.description,
                        amount = budget.amount.let { value ->
                            PNEntity.Planning.Budget.Amount(
                                amount = value.amount,
                                currency = value.currency
                            )
                        },
                        isEuropeanUnionFunded = budget.isEuropeanUnionFunded,
                        budgetBreakdowns = budget.budgetBreakdowns.map { budgetBreakdown ->
                            PNEntity.Planning.Budget.BudgetBreakdown(
                                id = budgetBreakdown.id,
                                description = budgetBreakdown.description,
                                amount = budgetBreakdown.amount.let { value ->
                                    PNEntity.Planning.Budget.BudgetBreakdown.Amount(
                                        amount = value.amount,
                                        currency = value.currency
                                    )
                                },
                                period = budgetBreakdown.period.let { period ->
                                    PNEntity.Planning.Budget.BudgetBreakdown.Period(
                                        startDate = period.startDate,
                                        endDate = period.endDate
                                    )
                                },
                                sourceParty = budgetBreakdown.sourceParty.let { sourceParty ->
                                    PNEntity.Planning.Budget.BudgetBreakdown.SourceParty(
                                        id = sourceParty.id,
                                        name = sourceParty.name
                                    )
                                },
                                europeanUnionFunding = budgetBreakdown.europeanUnionFunding?.let { europeanUnionFunding ->
                                    PNEntity.Planning.Budget.BudgetBreakdown.EuropeanUnionFunding(
                                        projectIdentifier = europeanUnionFunding.projectIdentifier,
                                        projectName = europeanUnionFunding.projectName,
                                        uri = europeanUnionFunding.uri
                                    )
                                }
                            )
                        }
                    )
                }
            )
        }
    }

    private fun tender(
        pmd: ProcurementMethod,
        value: PNEntity.Tender.Value,
        lots: List<PNEntity.Tender.Lot>,
        items: List<PNEntity.Tender.Item>,
        contractPeriod: PNEntity.Tender.ContractPeriod?,
        documents: List<PNEntity.Tender.Document>?,
        tenderRequest: PnCreateData.Tender
    ): PNEntity.Tender {
        return PNEntity.Tender(
            //BR-3.1.4
            id = generationService.generatePermanentTenderId(),
            /** Begin BR-3.1.2*/
            status = TenderStatus.PLANNING,
            statusDetails = TenderStatusDetails.PLANNING,
            /** End BR-3.1.2*/

            classification = tenderRequest.classification.let { classification ->
                PNEntity.Tender.Classification(
                    scheme = classification.scheme,
                    id = classification.id,
                    description = classification.description
                )
            },
            title = tenderRequest.title,
            description = tenderRequest.description,
            //BR-3.1.17
            acceleratedProcedure = PNEntity.Tender.AcceleratedProcedure(isAcceleratedProcedure = false),
            //BR-3.1.7
            designContest = PNEntity.Tender.DesignContest(serviceContractAward = false),
            //BR-3.1.8, BR-3.1.9, BR-3.1.10
            electronicWorkflows = PNEntity.Tender.ElectronicWorkflows(
                useOrdering = false,
                usePayment = false,
                acceptInvoicing = false
            ),
            //BR-3.1.11
            jointProcurement = PNEntity.Tender.JointProcurement(isJointProcurement = false),
            //BR-3.1.12
            procedureOutsourcing = PNEntity.Tender.ProcedureOutsourcing(procedureOutsourced = false),
            //BR-3.1.13
            framework = PNEntity.Tender.Framework(isAFramework = false),
            //BR-3.1.14
            dynamicPurchasingSystem = PNEntity.Tender.DynamicPurchasingSystem(hasDynamicPurchasingSystem = false),
            legalBasis = tenderRequest.legalBasis,
            procurementMethod = pmd,
            procurementMethodDetails = tenderRequest.procurementMethodDetails,
            procurementMethodRationale = tenderRequest.procurementMethodRationale,
            procurementMethodAdditionalInfo = tenderRequest.procurementMethodAdditionalInfo,
            mainProcurementCategory = tenderRequest.mainProcurementCategory,
            eligibilityCriteria = tenderRequest.eligibilityCriteria,
            tenderPeriod = tenderRequest.tenderPeriod.let { period ->
                PNEntity.Tender.TenderPeriod(
                    startDate = period.startDate
                )
            },
            //BR-3.1.26
            contractPeriod = contractPeriod,
            procuringEntity = tenderRequest.procuringEntity?.let { procuringEntity ->
                PNEntity.Tender.ProcuringEntity(
                    id = generationService.generateOrganizationId(
                        identifierScheme = procuringEntity.identifier.scheme,
                        identifierId = procuringEntity.identifier.id
                    ),
                    name = procuringEntity.name,
                    identifier = procuringEntity.identifier.let { identifier ->
                        PNEntity.Tender.ProcuringEntity.Identifier(
                            scheme = identifier.scheme,
                            id = identifier.id,
                            legalName = identifier.legalName,
                            uri = identifier.uri
                        )
                    },
                    additionalIdentifiers = procuringEntity.additionalIdentifiers.map { additionalIdentifier ->
                        PNEntity.Tender.ProcuringEntity.AdditionalIdentifier(
                            scheme = additionalIdentifier.scheme,
                            id = additionalIdentifier.id,
                            legalName = additionalIdentifier.legalName,
                            uri = additionalIdentifier.uri
                        )
                    },
                    address = procuringEntity.address.let { address ->
                        PNEntity.Tender.ProcuringEntity.Address(
                            streetAddress = address.streetAddress,
                            postalCode = address.postalCode,
                            addressDetails = address.addressDetails.let { addressDetails ->
                                PNEntity.Tender.ProcuringEntity.Address.AddressDetails(
                                    country = addressDetails.country.let { country ->
                                        PNEntity.Tender.ProcuringEntity.Address.AddressDetails.Country(
                                            scheme = country.scheme,
                                            id = country.id,
                                            description = country.description,
                                            uri = country.uri
                                        )
                                    },
                                    region = addressDetails.region.let { region ->
                                        PNEntity.Tender.ProcuringEntity.Address.AddressDetails.Region(
                                            scheme = region.scheme,
                                            id = region.id,
                                            description = region.description,
                                            uri = region.uri
                                        )
                                    },
                                    locality = addressDetails.locality.let { locality ->
                                        PNEntity.Tender.ProcuringEntity.Address.AddressDetails.Locality(
                                            scheme = locality.scheme,
                                            id = locality.id,
                                            description = locality.description,
                                            uri = locality.uri
                                        )
                                    }
                                )
                            }
                        )
                    },
                    contactPoint = procuringEntity.contactPoint.let { contactPoint ->
                        PNEntity.Tender.ProcuringEntity.ContactPoint(
                            name = contactPoint.name,
                            email = contactPoint.email,
                            telephone = contactPoint.telephone,
                            faxNumber = contactPoint.faxNumber,
                            url = contactPoint.url
                        )
                    }
                )
            },
            //BR-3.1.28 | BR-3.1.25
            value = value,
            //BR-3.1.15
            lotGroups = listOf(PNEntity.Tender.LotGroup(optionToCombine = false)),
            lots = lots,
            items = items,
            //BR-3.1.16
            requiresElectronicCatalogue = false,
            //BR-3.1.18
            submissionMethod = listOf(SubmissionMethod.ELECTRONIC_SUBMISSION),
            submissionMethodRationale = tenderRequest.submissionMethodRationale,
            submissionMethodDetails = tenderRequest.submissionMethodDetails,
            documents = documents
        )
    }

    private fun convertRequestLots(
        lots: List<PnCreateData.Tender.Lot>,
        relatedTemporalWithPermanentLotId: Map<String, String>
    ): List<PNEntity.Tender.Lot> {
        return lots.map { lot ->
            PNEntity.Tender.Lot(
                //BR-3.1.5
                id = relatedTemporalWithPermanentLotId.getValue(lot.id),
                internalId = lot.internalId,
                title = lot.title,
                description = lot.description,
                /** Begin BR-3.1.1 */
                status = LotStatus.PLANNING,
                statusDetails = LotStatusDetails.EMPTY,
                /** End BR-3.1.1 */
                value = lot.value.let { value ->
                    PNEntity.Tender.Lot.Value(
                        amount = value.amount,
                        currency = value.currency
                    )
                },
                //BR-3.1.19
                options = listOf(PNEntity.Tender.Lot.Option(false)),
                //BR-3.1.20
                variants = listOf(PNEntity.Tender.Lot.Variant(false)),
                //BR-3.1.21
                renewals = listOf(PNEntity.Tender.Lot.Renewal(false)),
                //BR-3.1.22
                recurrentProcurement = listOf(PNEntity.Tender.Lot.RecurrentProcurement(false)),
                contractPeriod = lot.contractPeriod.let { contractPeriod ->
                    PNEntity.Tender.Lot.ContractPeriod(
                        startDate = contractPeriod.startDate,
                        endDate = contractPeriod.endDate
                    )
                },
                placeOfPerformance = lot.placeOfPerformance.let { placeOfPerformance ->
                    PNEntity.Tender.Lot.PlaceOfPerformance(
                        address = placeOfPerformance.address.let { address ->
                            PNEntity.Tender.Lot.PlaceOfPerformance.Address(
                                streetAddress = address.streetAddress,
                                postalCode = address.postalCode,
                                addressDetails = address.addressDetails.let { addressDetails ->
                                    PNEntity.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                        country = addressDetails.country.let { country ->
                                            PNEntity.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                                scheme = country.scheme,
                                                id = country.id,
                                                description = country.description,
                                                uri = country.uri
                                            )
                                        },
                                        region = addressDetails.region.let { region ->
                                            PNEntity.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                                scheme = region.scheme,
                                                id = region.id,
                                                description = region.description,
                                                uri = region.uri
                                            )
                                        },
                                        locality = addressDetails.locality.let { locality ->
                                            PNEntity.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
                                                scheme = locality.scheme,
                                                id = locality.id,
                                                description = locality.description,
                                                uri = locality.uri
                                            )
                                        }
                                    )
                                }
                            )
                        },
                        description = placeOfPerformance.description
                    )
                }
            )
        }
    }

    private fun convertRequestItems(
        itemsFromRequest: List<PnCreateData.Tender.Item>,
        relatedTemporalWithPermanentLotId: Map<String, String>
    ): List<PNEntity.Tender.Item> {
        return itemsFromRequest.map { item ->
            PNEntity.Tender.Item(
                //BR-3.1.6
                id = generationService.generatePermanentItemId(),
                internalId = item.internalId,
                description = item.description,
                classification = item.classification.let { classification ->
                    PNEntity.Tender.Item.Classification(
                        scheme = classification.scheme,
                        id = classification.id,
                        description = classification.description
                    )
                },
                additionalClassifications = item.additionalClassifications.map { additionalClassification ->
                    PNEntity.Tender.Item.AdditionalClassification(
                        scheme = additionalClassification.scheme,
                        id = additionalClassification.id,
                        description = additionalClassification.description
                    )
                },
                quantity = item.quantity,
                unit = item.unit.let { unit ->
                    PNEntity.Tender.Item.Unit(
                        id = unit.id,
                        name = unit.name
                    )
                },
                relatedLot = relatedTemporalWithPermanentLotId.getValue(item.relatedLot)
            )
        }
    }

    private fun convertRequestDocument(documentFromRequest: PnCreateData.Tender.Document): PNEntity.Tender.Document {
        return PNEntity.Tender.Document(
            id = documentFromRequest.id,
            documentType = DocumentType.creator(documentFromRequest.documentType.key),
            title = documentFromRequest.title,
            description = documentFromRequest.description,
            relatedLots = documentFromRequest.relatedLots.toSet()
        )
    }

    private fun convertRequestDocument(
        documentFromRequest: PnCreateData.Tender.Document,
        relatedTemporalWithPermanentLotId: Map<String, String>
    ): PNEntity.Tender.Document {
        val relatedLots = documentFromRequest.relatedLots.map { relatedLot ->
            relatedTemporalWithPermanentLotId.getValue(relatedLot)
        }

        return PNEntity.Tender.Document(
            id = documentFromRequest.id,
            documentType = DocumentType.creator(documentFromRequest.documentType.key),
            title = documentFromRequest.title,
            description = documentFromRequest.description,
            relatedLots = relatedLots.toSet()
        )
    }

    /**
     * BR-3.1.5
     *
     * eAccess меняет временные "ID" (tender/lot/id) лотов на постоянные.
     * Постоянные "ID" (tender/lot/id) лотов формируются как уникальные для данного контрактного процесса
     * 32-символьные идентификаторы.
     */
    private fun generatePermanentLotId(lots: List<PnCreateData.Tender.Lot>): Map<String, String> {
        return lots.asSequence()
            .map { lot ->
                val permanentId = generationService.generatePermanentLotId()
                lot.id to permanentId
            }
            .toMap()
    }

    /**
     * BR-3.1.25 (PN) "Value" (tender) -> BR-3.6.30
     *
     * eAccess add object "Value":
     *   - "Amount" (tender.value.amount) is obtained by summation of values from "Amount" (tender.lot.value.amount)
     *      of all lot objects from Request.
     *   - eAccess sets "Currency" (tender.value.currency) == "Currency" (tender.lot.value.currency) from Request.
     */
    private fun calculateTenderValueFromLots(lots: List<PnCreateData.Tender.Lot>): PNEntity.Tender.Value {
        val currency = lots.elementAt(0).value.currency
        val totalAmount = lots.fold(BigDecimal.ZERO) { acc, lot ->
            acc.plus(lot.value.amount)
        }.setScale(2, RoundingMode.HALF_UP)
        return PNEntity.Tender.Value(totalAmount, currency)
    }

    /**
     * BR-3.1.26 "Contract Period" (Tender)
     *
     * - eAccess определяет "Contract Period: Start Date" (tender.contractPeriod.startDate) == наиболее раннему
     *   значению из полей "Contract Period: Start Date" (tender.lots.contractPeriod.startDate)
     *   всех добавленных объектов секции Lots запроса.
     * - eAccess определяет "Contract Period: End Date" (tender/contractPeriod/endDate) == наиболее позднему
     *   значению из полей "Contract Period: End Date" (tender/lots/contractPeriod/endDate)
     *   всех добавленных объектов секции Lots запроса.
     */
    private fun contractPeriod(lots: List<PnCreateData.Tender.Lot>): PNEntity.Tender.ContractPeriod {
        val contractPeriodSet = lots.asSequence().map { it.contractPeriod }.toSet()
        val startDate = contractPeriodSet.minBy { it.startDate }!!.startDate
        val endDate = contractPeriodSet.maxBy { it.endDate }!!.endDate
        return PNEntity.Tender.ContractPeriod(startDate, endDate)
    }

    /**
     * BR-3.1.28 "Value" (tender)
     *
     * IF there are NO items object in Request, eAccess adds object "Value" in such way:
     * Sets "Amount" (tender.value.amount) == "Amount" (budget.amount.amount) from Request.
     * Sets "Currency" (tender.value.currency) == "Currency" (budget.amount.currency) from Request.
     */
    private fun calculateTenderValueFromBudget(budget: PnCreateData.Planning.Budget): PNEntity.Tender.Value {
        return budget.amount.let { value ->
            PNEntity.Tender.Value(
                amount = value.amount,
                currency = value.currency
            )
        }
    }

    private fun context(cm: CommandMessage): ContextRequest {
        val owner = cm.context.owner
            ?: throw ErrorException(error = CONTEXT, message = "Missing the 'owner' attribute in context.")
        val country = cm.context.country
            ?: throw ErrorException(error = CONTEXT, message = "Missing the 'country' attribute in context.")
        val pmd: ProcurementMethod = cm.context.pmd?.let { getPmd(it) }
            ?: throw ErrorException(error = CONTEXT, message = "Missing the 'pmd' attribute in context.")
        val startDate: LocalDateTime = cm.startDate
        val testMode: Boolean = cm.testMode

        return ContextRequest(
            owner = owner,
            country = country,
            pmd = pmd,
            startDate = startDate,
            testMode = testMode
        )
    }

    private fun getPmd(pmd: String): ProcurementMethod = ProcurementMethod.creator(pmd)

    private fun getResponse(pn: PNEntity, token: UUID, cpid: Cpid): PnCreateResult {
        return PnCreateResult(
            cpid = cpid,
            ocid = pn.ocid,
            token = token.toString(),
            planning = pn.planning.let { planning ->
                PnCreateResult.Planning(
                    rationale = planning.rationale,
                    budget = planning.budget.let { budget ->
                        PnCreateResult.Planning.Budget(
                            description = budget.description,
                            amount = budget.amount.let { amount ->
                                Money(
                                    amount = amount.amount,
                                    currency = amount.currency
                                )
                            },
                            isEuropeanUnionFunded = budget.isEuropeanUnionFunded,
                            budgetBreakdowns = budget.budgetBreakdowns.map { budgetBreakdown ->
                                PnCreateResult.Planning.Budget.BudgetBreakdown(
                                    id = budgetBreakdown.id,
                                    description = budgetBreakdown.description,
                                    amount = budgetBreakdown.amount.let { amount ->
                                        Money(
                                            amount = amount.amount,
                                            currency = amount.currency
                                        )
                                    },
                                    period = budgetBreakdown.period.let { period ->
                                        PnCreateResult.Planning.Budget.BudgetBreakdown.Period(
                                            startDate = period.startDate,
                                            endDate = period.endDate
                                        )
                                    },
                                    sourceParty = budgetBreakdown.sourceParty.let { sourceParty ->
                                        PnCreateResult.Planning.Budget.BudgetBreakdown.SourceParty(
                                            id = sourceParty.id,
                                            name = sourceParty.name
                                        )
                                    },
                                    europeanUnionFunding = budgetBreakdown.europeanUnionFunding?.let { europeanUnionFunding ->
                                        PnCreateResult.Planning.Budget.BudgetBreakdown.EuropeanUnionFunding(
                                            projectIdentifier = europeanUnionFunding.projectIdentifier,
                                            projectName = europeanUnionFunding.projectName,
                                            uri = europeanUnionFunding.uri
                                        )
                                    }
                                )
                            }
                        )
                    }
                )
            },
            tender = pn.tender.let { tender ->
                PnCreateResult.Tender(
                    id = tender.id,
                    status = tender.status,
                    statusDetails = tender.statusDetails,
                    title = tender.title,
                    description = tender.description,
                    classification = tender.classification.let { classification ->
                        PnCreateResult.Tender.Classification(
                            scheme = classification.scheme,
                            id = classification.id,
                            description = classification.description
                        )
                    },
                    tenderPeriod = tender.tenderPeriod.let { tenderPeriod ->
                        PnCreateResult.Tender.TenderPeriod(
                            startDate = tenderPeriod.startDate
                        )
                    },
                    acceleratedProcedure = tender.acceleratedProcedure.let { acceleratedProcedure ->
                        PnCreateResult.Tender.AcceleratedProcedure(
                            isAcceleratedProcedure = acceleratedProcedure.isAcceleratedProcedure
                        )
                    },
                    designContest = tender.designContest.let { designContest ->
                        PnCreateResult.Tender.DesignContest(
                            serviceContractAward = designContest.serviceContractAward
                        )
                    },
                    electronicWorkflows = tender.electronicWorkflows.let { electronicWorkflows ->
                        PnCreateResult.Tender.ElectronicWorkflows(
                            useOrdering = electronicWorkflows.useOrdering,
                            usePayment = electronicWorkflows.usePayment,
                            acceptInvoicing = electronicWorkflows.acceptInvoicing
                        )
                    },
                    jointProcurement = tender.jointProcurement.let { jointProcurement ->
                        PnCreateResult.Tender.JointProcurement(
                            isJointProcurement = jointProcurement.isJointProcurement
                        )
                    },
                    procedureOutsourcing = tender.procedureOutsourcing.let { procedureOutsourcing ->
                        PnCreateResult.Tender.ProcedureOutsourcing(
                            procedureOutsourced = procedureOutsourcing.procedureOutsourced
                        )
                    },
                    framework = tender.framework.let { framework ->
                        PnCreateResult.Tender.Framework(
                            isAFramework = framework.isAFramework
                        )
                    },
                    dynamicPurchasingSystem = tender.dynamicPurchasingSystem.let { dynamicPurchasingSystem ->
                        PnCreateResult.Tender.DynamicPurchasingSystem(
                            hasDynamicPurchasingSystem = dynamicPurchasingSystem.hasDynamicPurchasingSystem
                        )
                    },
                    legalBasis = tender.legalBasis,
                    procurementMethod = tender.procurementMethod,
                    procurementMethodDetails = tender.procurementMethodDetails,
                    procurementMethodRationale = tender.procurementMethodRationale,
                    procurementMethodAdditionalInfo = tender.procurementMethodAdditionalInfo,
                    mainProcurementCategory = tender.mainProcurementCategory,
                    eligibilityCriteria = tender.eligibilityCriteria,
                    contractPeriod = tender.contractPeriod?.let { contractPeriod ->
                        PnCreateResult.Tender.ContractPeriod(
                            startDate = contractPeriod.startDate,
                            endDate = contractPeriod.endDate
                        )
                    },
                    procuringEntity = tender.procuringEntity?.let { procuringEntity ->
                        PnCreateResult.Tender.ProcuringEntity(
                            id = procuringEntity.id,
                            name = procuringEntity.name,
                            identifier = procuringEntity.identifier.let { identifier ->
                                PnCreateResult.Tender.ProcuringEntity.Identifier(
                                    scheme = identifier.scheme,
                                    id = identifier.id,
                                    legalName = identifier.legalName,
                                    uri = identifier.uri
                                )
                            },
                            additionalIdentifiers = procuringEntity.additionalIdentifiers?.map { additionalIdentifier ->
                                PnCreateResult.Tender.ProcuringEntity.AdditionalIdentifier(
                                    scheme = additionalIdentifier.scheme,
                                    id = additionalIdentifier.id,
                                    legalName = additionalIdentifier.legalName,
                                    uri = additionalIdentifier.uri
                                )
                            }
                                .orEmpty(),
                            address = procuringEntity.address.let { address ->
                                PnCreateResult.Tender.ProcuringEntity.Address(
                                    streetAddress = address.streetAddress,
                                    postalCode = address.postalCode,
                                    addressDetails = address.addressDetails.let { addressDetails ->
                                        PnCreateResult.Tender.ProcuringEntity.Address.AddressDetails(
                                            country = addressDetails.country.let { country ->
                                                PnCreateResult.Tender.ProcuringEntity.Address.AddressDetails.Country(
                                                    scheme = country.scheme,
                                                    id = country.id,
                                                    description = country.description,
                                                    uri = country.uri
                                                )
                                            },
                                            region = addressDetails.region.let { region ->
                                                PnCreateResult.Tender.ProcuringEntity.Address.AddressDetails.Region(
                                                    scheme = region.scheme,
                                                    id = region.id,
                                                    description = region.description,
                                                    uri = region.uri
                                                )
                                            },
                                            locality = addressDetails.locality.let { locality ->
                                                PnCreateResult.Tender.ProcuringEntity.Address.AddressDetails.Locality(
                                                    scheme = locality.scheme,
                                                    id = locality.id,
                                                    description = locality.description,
                                                    uri = locality.uri
                                                )
                                            }

                                        )
                                    }
                                )
                            },
                            contactPoint = procuringEntity.contactPoint.let { contactPoint ->
                                PnCreateResult.Tender.ProcuringEntity.ContactPoint(
                                    name = contactPoint.name,
                                    email = contactPoint.email,
                                    telephone = contactPoint.telephone,
                                    faxNumber = contactPoint.faxNumber,
                                    url = contactPoint.url
                                )
                            }
                        )
                    },
                    value = tender.value.let { value ->
                        Money(
                            amount = value.amount,
                            currency = value.currency
                        )
                    },
                    lotGroups = tender.lotGroups.map { lotGroup ->
                        PnCreateResult.Tender.LotGroup(
                            optionToCombine = lotGroup.optionToCombine
                        )
                    },
                    lots = tender.lots.map { lot ->
                        PnCreateResult.Tender.Lot(
                            id = lot.id,
                            internalId = lot.internalId,
                            title = lot.title,
                            description = lot.description,
                            status = lot.status,
                            statusDetails = lot.statusDetails,
                            value = lot.value.let { value ->
                                Money(
                                    amount = value.amount,
                                    currency = value.currency
                                )
                            },
                            options = lot.options?.map { option ->
                                PnCreateResult.Tender.Lot.Option(
                                    hasOptions = option.hasOptions
                                )
                            }
                                .orEmpty(),
                            variants = lot.variants?.map { variant ->
                                PnCreateResult.Tender.Lot.Variant(
                                    hasVariants = variant.hasVariants
                                )
                            }
                                .orEmpty(),
                            renewals = lot.renewals?.map { renewal ->
                                PnCreateResult.Tender.Lot.Renewal(
                                    hasRenewals = renewal.hasRenewals
                                )
                            }
                                .orEmpty(),
                            recurrentProcurement = lot.recurrentProcurement?.map { recurrentProcurement ->
                                PnCreateResult.Tender.Lot.RecurrentProcurement(
                                    isRecurrent = recurrentProcurement.isRecurrent
                                )
                            }
                                .orEmpty(),
                            contractPeriod = lot.contractPeriod.let { contractPeriod ->
                                PnCreateResult.Tender.Lot.ContractPeriod(
                                    startDate = contractPeriod.startDate,
                                    endDate = contractPeriod.endDate
                                )
                            },
                            placeOfPerformance = lot.placeOfPerformance.let { placeOfPerformance ->
                                PnCreateResult.Tender.Lot.PlaceOfPerformance(
                                    description = placeOfPerformance.description,
                                    address = placeOfPerformance.address.let { address ->
                                        PnCreateResult.Tender.Lot.PlaceOfPerformance.Address(
                                            streetAddress = address.streetAddress,
                                            postalCode = address.postalCode,
                                            addressDetails = address.addressDetails.let { addressDetails ->
                                                PnCreateResult.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                                    country = addressDetails.country.let { country ->
                                                        PnCreateResult.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                                            scheme = country.scheme,
                                                            id = country.id,
                                                            description = country.description,
                                                            uri = country.uri
                                                        )
                                                    },
                                                    region = addressDetails.region.let { region ->
                                                        PnCreateResult.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                                            scheme = region.scheme,
                                                            id = region.id,
                                                            description = region.description,
                                                            uri = region.uri
                                                        )
                                                    },
                                                    locality = addressDetails.locality.let { locality ->
                                                        PnCreateResult.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
                                                            scheme = locality.scheme,
                                                            id = locality.id,
                                                            description = locality.description,
                                                            uri = locality.uri
                                                        )
                                                    }

                                                )
                                            }
                                        )
                                    }
                                )
                            }
                        )
                    },
                    items = tender.items.map { item ->
                        PnCreateResult.Tender.Item(
                            id = item.id,
                            internalId = item.internalId,
                            classification = item.classification.let { classification ->
                                PnCreateResult.Tender.Item.Classification(
                                    scheme = classification.scheme,
                                    id = classification.id,
                                    description = classification.description
                                )
                            },
                            additionalClassifications = item.additionalClassifications?.map { additionalClassification ->
                                PnCreateResult.Tender.Item.AdditionalClassification(
                                    scheme = additionalClassification.scheme,
                                    id = additionalClassification.id,
                                    description = additionalClassification.description
                                )
                            }
                                .orEmpty(),
                            quantity = item.quantity,
                            unit = item.unit.let { unit ->
                                PnCreateResult.Tender.Item.Unit(
                                    id = unit.id,
                                    name = unit.name
                                )
                            },
                            description = item.description,
                            relatedLot = item.relatedLot
                        )
                    },
                    requiresElectronicCatalogue = tender.requiresElectronicCatalogue,
                    submissionMethod = tender.submissionMethod,
                    submissionMethodRationale = tender.submissionMethodRationale,
                    submissionMethodDetails = tender.submissionMethodDetails,
                    documents = tender.documents?.map { document ->
                        PnCreateResult.Tender.Document(
                            documentType = document.documentType,
                            id = document.id,
                            title = document.title!!,
                            description = document.description,
                            relatedLots = document.relatedLots?.toList() ?: emptyList()
                        )
                    }
                        .orEmpty()
                )
            },
            buyer = pn.buyer
                ?.let { buyer ->
                    PnCreateResult.Buyer(
                        id = buyer.id,
                        name = buyer.name,
                        details = buyer.details
                            ?.let { details ->
                                PnCreateResult.Buyer.Details(
                                    typeOfBuyer = details.typeOfBuyer,
                                    mainGeneralActivity = details.mainGeneralActivity,
                                    mainSectoralActivity = details.mainSectoralActivity
                                )
                            },
                        additionalIdentifiers = buyer.additionalIdentifiers
                            ?.map { additionalIdentifier ->
                                PnCreateResult.Buyer.AdditionalIdentifier(
                                    id = additionalIdentifier.id,
                                    legalName = additionalIdentifier.legalName,
                                    scheme = additionalIdentifier.scheme,
                                    uri = additionalIdentifier.uri
                                )
                            },
                        address = buyer.address
                            .let { address ->
                                PnCreateResult.Buyer.Address(
                                    streetAddress = address.streetAddress,
                                    postalCode = address.postalCode,
                                    addressDetails = address.addressDetails.let { addressDetails ->
                                        PnCreateResult.Buyer.Address.AddressDetails(
                                            country = addressDetails.country.let { country ->
                                                PnCreateResult.Buyer.Address.AddressDetails.Country(
                                                    scheme = country.scheme,
                                                    id = country.id,
                                                    description = country.description,
                                                    uri = country.uri
                                                )
                                            },
                                            region = addressDetails.region.let { region ->
                                                PnCreateResult.Buyer.Address.AddressDetails.Region(
                                                    scheme = region.scheme,
                                                    id = region.id,
                                                    description = region.description,
                                                    uri = region.uri
                                                )
                                            },
                                            locality = addressDetails.locality.let { locality ->
                                                PnCreateResult.Buyer.Address.AddressDetails.Locality(
                                                    scheme = locality.scheme,
                                                    id = locality.id,
                                                    description = locality.description,
                                                    uri = locality.uri
                                                )
                                            }
                                        )
                                    }
                                )
                            },
                        contactPoint = buyer.contactPoint
                            .let { contactPoint ->
                                PnCreateResult.Buyer.ContactPoint(
                                    name = contactPoint.name,
                                    email = contactPoint.email,
                                    faxNumber = contactPoint.faxNumber,
                                    telephone = contactPoint.telephone,
                                    url = contactPoint.url
                                )
                            },
                        identifier = buyer.identifier
                            .let { identifier ->
                                PnCreateResult.Buyer.Identifier(
                                    id = identifier.id,
                                    scheme = identifier.scheme,
                                    uri = identifier.uri,
                                    legalName = identifier.legalName
                                )
                            }
                    )
                }
        )
    }

    data class ContextRequest(
        val owner: String,
        val country: String,
        val pmd: ProcurementMethod,
        val startDate: LocalDateTime,
        val testMode: Boolean
    )
}
