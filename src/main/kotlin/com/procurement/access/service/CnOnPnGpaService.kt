package com.procurement.access.service

import com.procurement.access.application.model.context.CheckCnOnPnGpaContext
import com.procurement.access.application.service.CheckedCnOnPnGpa
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.enums.BusinessFunctionDocumentType
import com.procurement.access.domain.model.enums.BusinessFunctionType
import com.procurement.access.domain.model.enums.MainProcurementCategory
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.exception.ErrorType.DATA_NOT_FOUND
import com.procurement.access.exception.ErrorType.INVALID_DOCS_ID
import com.procurement.access.exception.ErrorType.INVALID_DOCS_RELATED_LOTS
import com.procurement.access.exception.ErrorType.INVALID_ITEMS_RELATED_LOTS
import com.procurement.access.exception.ErrorType.INVALID_LOT_CONTRACT_PERIOD
import com.procurement.access.exception.ErrorType.INVALID_LOT_CURRENCY
import com.procurement.access.exception.ErrorType.INVALID_PMM
import com.procurement.access.exception.ErrorType.INVALID_PROCURING_ENTITY
import com.procurement.access.exception.ErrorType.INVALID_TENDER_AMOUNT
import com.procurement.access.exception.ErrorType.ITEM_ID_IS_DUPLICATED
import com.procurement.access.exception.ErrorType.LOT_ID_DUPLICATED
import com.procurement.access.infrastructure.dto.cn.CnOnPnGpaRequest
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.entity.PNEntity
import com.procurement.access.lib.toSetBy
import com.procurement.access.lib.uniqueBy
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

@Service
class CnOnPnGpaService(
    private val tenderProcessDao: TenderProcessDao,
    private val rulesService: RulesService
) {

    fun checkCnOnPnGpa(context: CheckCnOnPnGpaContext, data: CnOnPnGpaRequest): CheckedCnOnPnGpa {
        val entity: TenderProcessEntity =
            tenderProcessDao.getByCpIdAndStage(context.cpid, context.previousStage)
                ?: throw ErrorException(DATA_NOT_FOUND)

        val pnEntity: PNEntity = toObject(PNEntity::class.java, entity.jsonData)

        //VR-3.8.18 Tender status
        checkTenderStatus(pnEntity)

        //VR-3.8.3 Documents (duplicate)
        checkDocuments(documentsFromRequest = data.tender.documents, documentsFromPN = pnEntity.tender.documents)

        data.tender.procuringEntity?.also { requestProcuringEntity ->
            //VR-1.0.1.10.1
            checkProcuringEntityIdentifier(requestProcuringEntity, pnEntity.tender.procuringEntity)

            // VR-1.0.1.10.2, VR-1.0.1.10.3, VR-1.0.1.10.6
            checkProcuringEntityPersones(requestProcuringEntity)

            // VR-1.0.1.10.4, VR-1.0.1.10.5
            checkPersonesBusinessFunctions(requestProcuringEntity)

            // VR-1.0.1.10.7
            checkBusinessFunctionPeriod(requestProcuringEntity, context)

            // VR-1.0.1.2.1, VR-1.0.1.2.7, VR-1.0.1.2.8
            checkBusinessFunctionDocuments(requestProcuringEntity)
            checkTenderDocumentsNotEmpty(data.tender)
        }


        data.tender.secondStage?.run {
            validateCompleteness()                      // VR-1.0.1.11.1

            minimumCandidates
                ?.let { validateMinimumCandidates(it) } // VR-1.0.1.11.2

            maximumCandidates
                ?.let { validateMaminumCandidates(it) } // VR-1.0.1.11.3

            // VR-1.0.1.11.4
            if (this.minimumCandidates != null && this.maximumCandidates != null)
                validateCandidatesRange(min = minimumCandidates, max = maximumCandidates)
        }



        if (pnEntity.tender.items.isEmpty()) {
            val lotsIdsFromRequest = data.tender.lots.asSequence()
                .map { it.id }
                .toSet()

            /** Begin check Lots */
            //VR-3.8.5(CN on PN)  "Currency" (lot)
            checkCurrencyInLotsFromRequest(
                lotsFromRequest = data.tender.lots,
                budgetFromPN = pnEntity.planning.budget
            )

            //VR-3.8.8(CN on PN)  "Contract Period" (Lot) -> VR-3.6.7(CN)
            checkContractPeriodInLotsWhenPNWithoutItemsFromRequest(tenderFromRequest = data)

            //VR-3.8.10(CN on PN) Lots (tender.lots) -> VR-3.6.9(CN)
            checkLotIdsAsRelatedLotInItems(
                lotsIdsFromRequest = lotsIdsFromRequest,
                itemsFromRequest = data.tender.items
            )
            //VR-3.8.12(CN on PN) Lot.ID -> VR-3.1.14(CN)
            checkLotIdFromRequest(lotsFromRequest = data.tender.lots)
            /** End check Lots */

            /** Begin check Items */
            //VR-3.8.9(CN on PN) "Quantity" (item) -> VR-3.6.11(CN)
            checkQuantityInItems(itemsFromRequest = data.tender.items)

            //VR-3.8.11(CN on PN) Items (tender.Items) -> VR-3.6.8(CN)
            checkRelatedLotInItemsFromRequest(
                lotsIdsFromRequest = lotsIdsFromRequest,
                itemsFromRequest = data.tender.items
            )

            //VR-3.8.13(CN on PN) Item.ID -> VR-3.1.15(CN)
            checkItemIdFromRequest(itemsFromRequest = data.tender.items)
            /** End check Items */

            /** Begin check Tender */
            //VR-3.8.4(CN on PN) "Value" (tender)
            val tenderValue = calculateTenderValueFromLots(lotsFromRequest = data.tender.lots)
            checkTenderValue(tenderValue.amount, pnEntity.planning.budget)

            //VR-3.8.6(CN on PN)  "Contract Period"(Tender) -> VR-3.6.10(CN)
            checkContractPeriodInTender(
                lotsFromRequest = data.tender.lots,
                budgetBreakdownsFromPN = pnEntity.planning.budget.budgetBreakdowns
            )
            /** End check Tender */

            /** Begin check Auctions */
            //VR-1.0.1.7.7
            checkAuctionsAreRequired(
                context = context,
                data = data,
                mainProcurementCategory = pnEntity.tender.mainProcurementCategory
            )
            /** End check Auctions */

            /** Begin check Documents*/
            //VR-3.8.7(CN on PN)  "Related Lots"(documents) -> VR-3.6.12(CN)
            checkRelatedLotsInDocumentsFromRequestWhenPNWithoutItems(
                lotsIdsFromRequest = lotsIdsFromRequest,
                documentsFromRequest = data.tender.documents
            )
            /** End check Documents */
        } else {
            /** Begin check Lots*/
            //VR-3.8.16 "Contract Period" (Lot)
            checkContractPeriodInLotsFromRequestWhenPNWithItems(
                preQualificationPeriodEndDate = data.preQualification.period.endDate,
                lotsFromPN = pnEntity.tender.lots
            )
            /** End check Lots */

            /** Begin check Auctions*/
            //VR-3.8.15 electronicAuctions.details
            checkAuctionsAreRequired(
                context = context,
                data = data,
                mainProcurementCategory = pnEntity.tender.mainProcurementCategory
            )
            /** End check Auctions */

            /** Begin check Documents*/
            //VR-3.8.17(CN on PN)  "Related Lots"(documents) -> VR-3.7.13(Update CNEntity)
            val lotsIdsFromPN = pnEntity.tender.lots.toSetBy { it.id }
            checkRelatedLotsInDocumentsFromRequestWhenPNWithItems(
                lotsIdsFromPN = lotsIdsFromPN,
                documentsFromRequest = data.tender.documents
            )
            /** End check Documents */
        }

        return CheckedCnOnPnGpa(requireAuction = data.tender.electronicAuctions != null)
    }


    /**
     * VR-3.8.3(CN on PN) Documents ->  VR-3.6.1(CN)
     *                              ->  VR-3.7.3(CN)
     *
     * Checks the uniqueness of all documents.ID from Request;
     * IF there is NO repeated value in list of documents.ID values from Request, validation is successful;
     * ELSE eAccess throws Exception: "Invalid documents IDs";
     *
     * VR-3.6.1(CN)
     * eAccess валидирует наличие секции Documents (tender/Documents) в секции Tender запроса.
     * Производится проверка, что "documentType" (tender.Documents.documentType) каждого объекта секции Documents
     * запроса равен одному из значений из списка
     *
     * VR-3.7.3(CN)
     * eAccess проверяет, что все Document.ID документов сохраненной версии тендера имеют соответствие
     * в массиве Documents из запроса.
     */
    private fun checkDocuments(
        documentsFromRequest: List<CnOnPnGpaRequest.Tender.Document>,
        documentsFromPN: List<PNEntity.Tender.Document>?
    ) {
        val uniqueIdsDocumentsFromRequest: Set<String> = documentsFromRequest.toSetBy { it.id }
        if (uniqueIdsDocumentsFromRequest.size != documentsFromRequest.size)
            throw ErrorException(INVALID_DOCS_ID)

        documentsFromPN?.toSetBy { it.id }
            ?.forEach { id ->
                if (id !in uniqueIdsDocumentsFromRequest) {
                    throw ErrorException(
                        error = INVALID_DOCS_ID,
                        message = "The request is missing a document with id '$id'"
                    )
                }
            }
    }

    /**
     * VR-1.0.1.11.1
     *
     * eAccess checks availability at least one value: minimumCandidates or maximumCandidates from the context of Request:
     * a. IF [there is at least one value minimumCandidates or maximumCandidates in Request] then: validation is successful;
     * b. } else {  eAccess throws Exception: "At least one value should be: minimumCandidates or maximumCandidates";
     */
    private fun CnOnPnGpaRequest.Tender.SecondStage.validateCompleteness() {
        if (this.minimumCandidates == null && this.maximumCandidates == null)
            throw ErrorException(
                error = ErrorType.INVALID_SECOND_STAGE,
                message = "Path: tender.secondStage. At least one value should be: minimumCandidates or maximumCandidates"
            )
    }

    /**
     * VR-1.0.1.11.2
     *
     * eAccess checks minimumCandidates values from the context of Request:
     * a. IF [secondStage.minimumCandidates value from Request > (more) 0] then: validation is successful;
     * b. else eAccess throws Exception: "minimumCandidates value should not be zero";
     */
    private fun validateMinimumCandidates(amount: Int) {
        if (amount <= 0)
            throw ErrorException(
                error = ErrorType.INVALID_SECOND_STAGE,
                message = "Path: tender.secondStage.minimumCandidates. MinimumCandidates value should not be less or equals than zero"
            )
    }

    /**
     * VR-1.0.1.11.3
     *
     * eAccess checks maximumCandidates values from the context of Request:
     * a. IF [secondStage.maximumCandidates value from Request > (more) 0] then: validation is successful;
     * b. else eAccess throws Exception: "MaximumCandidates value should not be zero";
     */
    private fun validateMaminumCandidates(amount: Int) {
        if (amount <= 0)
            throw ErrorException(
                error = ErrorType.INVALID_SECOND_STAGE,
                message = "Path: tender.secondStage.maximumCandidates. MaximumCandidates value should not be less or equals than zero"
            )
    }

    /**
     * VR-1.0.1.11.4
     *
     * eAccess compares secondStage.minimumCandidates and maximumCandidates from the context of Request:
     * a. IF [secondStage.minimumCandidates < (less) maximumCandidates from Request]  then: validation is successful;
     * b. else eAccess throws Exception: "MaximumCandidates value should be more or equal MinimumCandidates ";
     */
    private fun validateCandidatesRange(min: Int, max: Int) {
        if (min >= max)
            throw ErrorException(
                error = ErrorType.INVALID_SECOND_STAGE,
                message = "Path: tender.secondStage.maximumCandidates. MaximumCandidates value should be more than MinimumCandidates "
            )
    }


    /**
     * VR-1.0.1.10.1
     *
     * eAccess compares procuringEntity.ID related to saved PN || CN from DB and procuringEntity.ID from Request:
     * IF [procuringEntity.ID value in DB ==  (equal to) procuringEntity.ID from Request] then: validation is successful; }
     * else {  eAccess throws Exception: "Invalid identifier of procuring entity";}
     *
     */
    private fun checkProcuringEntityIdentifier(
        procuringEntityRequest: CnOnPnGpaRequest.Tender.ProcuringEntity,
        procuringEntityDB: PNEntity.Tender.ProcuringEntity
    ) {
        if (procuringEntityDB.id != procuringEntityRequest.id) throw ErrorException(
            error = INVALID_PROCURING_ENTITY,
            message = "Invalid identifier of procuring entity. " +
                "Request.procuringEntity.id (=${procuringEntityRequest.id})  != " +
                "DB.procuringEntity.id (=${procuringEntityDB.id}). "
        )
    }

    /**
     * VR-1.0.1.10.2
     *
     * eAccess checks the avaliability of at least one procuringEntity.Persones object in array  from Request:
     * IF [there is at least one Persones object in Request] then validation is successful; }
     * else { eAccess throws Exception: "At least one Person should be added"; }
     *
     *
     * VR-1.0.1.10.3
     * eAccess checks the uniqueness of all Persones.identifier.ID values from every object of Persones array of Request:
     * IF [there is NO repeated values of identifier.ID in Request] then validation is successful; }
     * else { eAccess throws Exception: "Persones objects should be unique in Request"; }
     *
     *
     * VR-1.0.1.10.6
     *
     * eAccess checks persones.businessFunctions.type values in all businessFuctions object from Request;
     * IF businessFunctions.type == oneOf procuringEntityBusinessFuncTypeEnum value (link), validation is successful;}
     * else {  eAccess throws Exception: "Invalid business functions type";
     */
    private fun checkProcuringEntityPersones(
        procuringEntityRequest: CnOnPnGpaRequest.Tender.ProcuringEntity
    ) {

        procuringEntityRequest.persones
            ?.apply {
                if (this.isEmpty()) throw ErrorException(
                    error = INVALID_PROCURING_ENTITY,
                    message = "At least one Person should be added. "
                )

                val personesIdentifier = this.map { it.identifier }
                val personesIdentifierUnique = personesIdentifier.toSet()
                if (personesIdentifier.size != personesIdentifierUnique.size) throw ErrorException(
                    error = INVALID_PROCURING_ENTITY,
                    message = "Persones objects should be unique in Request. "
                )
            }
    }

    /**
     * VR-1.0.1.10.4
     *
     * eAccess checks the avaliability of at least one persones.businessFunctions object in businessFunctions array  from Request:
     * IF [there is at least one businessFunctions object in appropriate Persones object] then validation is successful; }
     * else { eAccess throws Exception: "At least one businessFunctions detaluzation should be added"; }
     *
     *
     * VR-1.0.1.10.5
     *
     * eAccess checks the uniqueness of all Persones.businessFunctions.ID values from every object of businessFunctions array of Request:
     * IF [there is NO repeated values of businessFunctions.ID] then validation is successful; }
     * else { eAccess throws Exception: "businessFunctions objects should be unique in every Person from Request"; }
     *
     */
    private fun checkPersonesBusinessFunctions(
        procuringEntityRequest: CnOnPnGpaRequest.Tender.ProcuringEntity
    ) {

        procuringEntityRequest.persones
            ?.map { it.businessFunctions }
            ?.forEach { businessfunctions ->
                if (businessfunctions.isEmpty()) throw ErrorException(
                    error = INVALID_PROCURING_ENTITY,
                    message = "At least one businessFunctions detalization should be added. "
                )
                if (businessfunctions.toSetBy { it.id }.size != businessfunctions.size) throw ErrorException(
                    error = INVALID_PROCURING_ENTITY,
                    message = "businessFunctions objects should be unique in every Person from Request. "
                )
            }

        procuringEntityRequest.persones
            ?.flatMap { it.businessFunctions }
            ?.forEach {
                when (it.type) {
                    BusinessFunctionType.CHAIRMAN,
                    BusinessFunctionType.PROCURMENT_OFFICER,
                    BusinessFunctionType.CONTACT_POINT,
                    BusinessFunctionType.TECHNICAL_EVALUATOR,
                    BusinessFunctionType.TECHNICAL_OPENER,
                    BusinessFunctionType.PRICE_OPENER,
                    BusinessFunctionType.PRICE_EVALUATOR -> Unit

                    BusinessFunctionType.AUTHORITY       -> throw ErrorException(
                        error = ErrorType.INVALID_BUSINESS_FUNCTION,
                        message = "Type '${BusinessFunctionType.AUTHORITY.key}' was deprecated. Use '${BusinessFunctionType.CHAIRMAN}' instead of it"
                    )
                }
            }
    }

    /**
     * VR-1.0.1.10.7
     *
     * eAccess compares businessFunctions.period.startDate and startDate from the context of Request:
     * IF [businessFunctions.period.startDate <= (less || equal to) startDate from Request] then: validation is successful; }
     * else {  eAccess throws Exception: "Invalid period in bussiness function specification"; }
     *
     */
    private fun checkBusinessFunctionPeriod(
        procuringEntityRequest: CnOnPnGpaRequest.Tender.ProcuringEntity,
        context: CheckCnOnPnGpaContext
    ) {
        fun dateError(): Nothing = throw ErrorException(
            error = INVALID_PROCURING_ENTITY,
            message = "Invalid period in bussiness function specification. "
        )

        procuringEntityRequest.persones
            ?.flatMap { it.businessFunctions }
            ?.forEach { if (it.period.startDate > context.startDate) dateError() }
    }

    /**
     * VR-1.0.1.2.1
     *
     * eAccess checks the uniqueness of all documents.ID from Request;
     * if there is NO repeated value in list of documents.ID values from Request, validation is successful;
     * else {  eAccess throws Exception: "Invalid documents IDs";
     *
     *
     * VR-1.0.1.2.7
     *
     * eAccess checks the avaliability of at least one tender.documents || bussinessFunctions.documents object in array from Request:
     * IF [there is at least one Document object in Request] then validation is successful; }
     * else { eAccess throws Exception: "At least one document should be added"; }
     *
     *
     * VR-1.0.1.2.8
     *
     * eAccess checks documents.documentType values in all Documents object from Request;
     * IF document.documentType == oneOf bussinesFunctionsDocumentTupeEnum value (link), validation is successful; }
     * else {  eAccess throws Exception: "Invalid document type"; }
     */
    private fun checkBusinessFunctionDocuments(
        procuringEntityRequest: CnOnPnGpaRequest.Tender.ProcuringEntity
    ) {

        procuringEntityRequest.persones
            ?.flatMap { it.businessFunctions }
            ?.forEach { businessFunction ->
                businessFunction.documents?.let { documents ->
                    if (documents.isEmpty()) throw ErrorException(
                        error = ErrorType.EMPTY_DOCS,
                        message = "At least one document should be added to businessFunction documents. "
                    )

                    val actualIds = documents.map { it.id }
                    val uniqueIds = actualIds.toSet()

                    if (actualIds.size != uniqueIds.size) throw ErrorException(
                        error = INVALID_DOCS_ID,
                        message = "Invalid documents IDs. Ids not unique [${actualIds}]. "
                    )

                    documents.forEach {
                        when (it.documentType) {
                            BusinessFunctionDocumentType.REGULATORY_DOCUMENT -> Unit
                        }
                    }
                }
            }
    }

    private fun checkTenderDocumentsNotEmpty(
        tender: CnOnPnGpaRequest.Tender
    ) {
        if (tender.documents.isEmpty()) throw ErrorException(
            error = ErrorType.EMPTY_DOCS,
            message = "At least one document should be added to tenders documents. "
        )
    }

    /**
     * VR-3.8.4(CN on PN) "Value" (tender)
     *
     * eAccess проверяет, что "Value" (tender.value.amount), полученное по BR-3.8.14(CN),
     * меньше / ровно значения поля «Budget Value» (budget.amount.amount) from DB.
     */
    private fun checkTenderValue(tenderValueAmount: BigDecimal, budget: PNEntity.Planning.Budget) {
        if (tenderValueAmount > budget.amount.amount)
            throw ErrorException(
                error = INVALID_TENDER_AMOUNT,
                message = "The amount of the tender [$tenderValueAmount] more that the amount of the budget [${budget.amount.amount}]."
            )
    }

    /**
     * VR-3.8.5(CN on PN)  "Currency" (lot)
     *
     * eAccess проверяет, что значение "Currency" (tender.lot.value.currency)
     * from Request == "Currency" (budget.amount.currency) from saved PNEntity.
     */
    private fun checkCurrencyInLotsFromRequest(
        lotsFromRequest: List<CnOnPnGpaRequest.Tender.Lot>,
        budgetFromPN: PNEntity.Planning.Budget
    ) {
        lotsFromRequest.forEach { lot ->
            if (lot.value.currency != budgetFromPN.amount.currency)
                throw ErrorException(
                    error = INVALID_LOT_CURRENCY,
                    message = "Lot with id: '${lot.id}' contains invalid currency (lot currency: '${lot.value.currency}', budget amount currency: '${budgetFromPN.amount.currency}')"
                )
        }
    }

    /**
     * VR-3.8.6(CN on PN)  "Contract Period"(Tender) -> VR-3.6.10(CN)
     *
     * VR-3.6.10(CN)	"Contract Period" (Tender)
     * eAccess проверяет, что значение ContractPeriod.StartDate (tender/contractPeriod/startDate),
     * определенное по правилу BR-3.6.31, -  меньше или равно каждому значению budgetBreakdown.period.endDate
     * AND ContractPeriod.EndDate (tender/contractPeriod/endDate) - больше или равен каждому значению budgetBreakdown.period.startDate
     * добавляемых FS:
     *
     * tender.contractPeriod.startDate  <= planning.budget.budgetBreakdown.period.endDate
     * tender.contractPeriod.endDate  >= planning.budget.budgetBreakdown.period.startDate
     *
     *Пример:
     * tender.contractPeriod.startDate = 10.06.2017 AND tender.contractPeriod.endDate = 30.10.2017
     * Period of budgetBreakdown1 [01.06.2017 - 10.08.2017] - budgetBreakdown is OK
     * Period of budgetBreakdown2 [31.10.2017 - 30.11.2017] - budgetBreakdown isn't OK
     * Period of budgetBreakdown3 [01.01.2017 - 09.06.2017] - budgetBreakdown isn't OK
     * Period of budgetBreakdown4 [03.03.2017 - 10.06.2017] - budgetBreakdown is OK
     *
     * BR-3.6.31(CN)	"Contract Period" (Tender)
     * eAccess определяет "Contract Period: Start Date" (tender.contractPeriod.startDate) == наиболее раннему значению
     * из полей "Contract Period: Start Date" (tender.lots.contractPeriod.startDate)
     * всех добавленных объектов секции Lots запроса.
     *
     * eAccess определяет "Contract Period: End Date" (tender/contractPeriod/endDate) == наиболее позднему значению
     * из полей "Contract Period: End Date" (tender/lots/contractPeriod/endDate)
     * всех добавленных объектов секции Lots запроса.
     */
    private fun checkContractPeriodInTender(
        lotsFromRequest: List<CnOnPnGpaRequest.Tender.Lot>,
        budgetBreakdownsFromPN: List<PNEntity.Planning.Budget.BudgetBreakdown>
    ) {
        val tenderContractPeriod = calculationTenderContractPeriod(lotsFromRequest)
        checkContractPeriodInTender(tenderContractPeriod, budgetBreakdownsFromPN)
    }

    private fun checkContractPeriodInTender(
        tenderContractPeriod: CNEntity.Tender.ContractPeriod,
        budgetBreakdownsFromPN: List<PNEntity.Planning.Budget.BudgetBreakdown>
    ) {
        budgetBreakdownsFromPN.forEach { budgetBreakdown ->
            if (tenderContractPeriod.startDate > budgetBreakdown.period.endDate)
                throw ErrorException(
                    error = INVALID_LOT_CONTRACT_PERIOD,
                    message = "The start date of the tender contract period [${tenderContractPeriod.startDate}] after than the end date of the budget breakdown period [${budgetBreakdown.period.endDate}]"
                )
            if (tenderContractPeriod.endDate < budgetBreakdown.period.startDate)
                throw ErrorException(
                    error = INVALID_LOT_CONTRACT_PERIOD,
                    message = "The end date of the tender contract period [${tenderContractPeriod.endDate}] before than the start date of the budget breakdown period [${budgetBreakdown.period.startDate}]"
                )
        }
    }

    /**
     * VR-3.8.7(CN on PN)  "Related Lots"(documents) -> VR-3.6.12(CN)
     *
     * VR-3.6.12 "Related Lots" (documents)
     * Access проверяет, что значения указанные в поле relatedLots (document.relatedLots) каждого объекта
     * секции Documents имеют соответствие в списке значений tender.lots.id.
     */
    private fun checkRelatedLotsInDocumentsFromRequestWhenPNWithoutItems(
        lotsIdsFromRequest: Set<String>,
        documentsFromRequest: List<CnOnPnGpaRequest.Tender.Document>
    ) {
        documentsFromRequest.forEach { document ->
            document.relatedLots?.forEach { relatedLot ->
                if (relatedLot !in lotsIdsFromRequest)
                    throw ErrorException(INVALID_DOCS_RELATED_LOTS)
            }
        }
    }

    /**
     *  VR-1.0.1.4.3
     *
     * IF (pmd == "GPA") { eAccess checks lot.contractPeriod.startDate in every Lot object from Request:
     *      IF value of lot.contractPeriod.startDate from Request > (later than) value of preQualification.period.endDate
     *      from Request, validation is successful;
     *      else { eAccess throws Exception: "Invalid date-time values in lot contract period";
     */
    private fun checkContractPeriodInLotsWhenPNWithoutItemsFromRequest(tenderFromRequest: CnOnPnGpaRequest) {
        val preQualificationPeriodEndDate = tenderFromRequest.preQualification.period.endDate
        tenderFromRequest.tender.lots.forEach { lot ->
            checkRangeContractPeriodInLotFromRequest(lot)
            if (lot.contractPeriod.startDate <= preQualificationPeriodEndDate)
                throw ErrorException(
                    error = INVALID_LOT_CONTRACT_PERIOD,
                    message = "The start date [${lot.contractPeriod.startDate}] of the contract period of the lot [${lot.id}] less or equals that the end date of the tender period [$preQualificationPeriodEndDate]."
                )
        }
    }

    private fun checkRangeContractPeriodInLotFromRequest(lot: CnOnPnGpaRequest.Tender.Lot) {
        if (lot.contractPeriod.startDate >= lot.contractPeriod.endDate)
            throw ErrorException(INVALID_LOT_CONTRACT_PERIOD)
    }

    /**
     * VR-3.8.9(CN on PN) "Quantity" (item) -> VR-3.6.11(CN)
     *
     * VR-3.6.11 "Quantity" (item)
     * eAccess проверяет, что значению "Quantity" (tender/items/quantity) каждого объекта секции Items больше нуля.
     */
    private fun checkQuantityInItems(itemsFromRequest: List<CnOnPnGpaRequest.Tender.Item>) {
        itemsFromRequest.forEach { item ->
            if (item.quantity <= BigDecimal.ZERO)
                throw ErrorException(ErrorType.INVALID_ITEMS_QUANTITY)
        }
    }

    /**
     * VR-3.8.10(CN on PN) Lots (tender.lots) -> VR-3.6.9(CN)
     *
     * VR-3.6.9	Lots
     * eAccess performs next steps:
     * Checks the quantity of Lot object in Request:
     *      IF quantity of Lot object in Request > 0, validation is successful;
     *      ELSE eAccess throws Exception;
     *
     * Analyzes Lot.ID from Request:
     *      IF all lot.ID from Request are presented in list of values item.relatedLot from Request (at least once),
     *      validation is successful;
     *      ELSE eAccess throws Exception;
     */
    private fun checkLotIdsAsRelatedLotInItems(
        lotsIdsFromRequest: Set<String>,
        itemsFromRequest: List<CnOnPnGpaRequest.Tender.Item>
    ) {
        if (lotsIdsFromRequest.isEmpty())
            throw ErrorException(ErrorType.EMPTY_LOTS)

        val itemsRelatedLots: Set<String> = itemsFromRequest.toSetBy { it.relatedLot }
        lotsIdsFromRequest.forEach { lotId ->
            if (lotId !in itemsRelatedLots)
                throw ErrorException(
                    error = ErrorType.LOT_ID_NOT_MATCH_TO_RELATED_LOT_IN_ITEMS,
                    message = ""
                )
        }
    }

    /**
     * VR-3.8.11(CN on PN) Items (tender.Items) -> VR-3.6.8(CN)
     *
     * VR-3.6.8 (CN)	Items
     * eAccess проверяет, что используется также Lots section (tender/lots).
     *
     * eAccess проверяет, что значению "Related Lot" (tender/items/relatedLot)
     * каждого объекта секции Items соответствует объект секции Lots по полю "Id" (tender/lots/id).
     */
    private fun checkRelatedLotInItemsFromRequest(
        lotsIdsFromRequest: Set<String>,
        itemsFromRequest: List<CnOnPnGpaRequest.Tender.Item>
    ) {
        itemsFromRequest.forEach { item ->
            val relatedLot = item.relatedLot
            if (relatedLot !in lotsIdsFromRequest)
                throw ErrorException(INVALID_ITEMS_RELATED_LOTS)
        }
    }

    /**
     * VR-3.8.12(CN on PN) Lot.ID -> VR-3.1.14(CN)
     *
     * VR-3.1.14	Lot.ID
     * eAccess analyzes Lot.ID from Request:
     * IF every lot.ID from Request is included once in list from Request, validation is successful;
     * ELSE eAccess throws Exception;
     */
    private fun checkLotIdFromRequest(lotsFromRequest: List<CnOnPnGpaRequest.Tender.Lot>) {
        val idsAreUniques = lotsFromRequest.uniqueBy { it.id }
        if (idsAreUniques.not())
            throw throw ErrorException(LOT_ID_DUPLICATED)
    }

    /**
     * VR-3.8.13(CN on PN) Item.ID -> VR-3.1.15(CN)
     *
     * VR-3.1.15	Item.ID
     * eAccess analyzes item.ID from Request:
     * IF every item.ID from Request is included once in list from Request, validation is successful;
     * ELSE eAccess throws Exception;
     */
    private fun checkItemIdFromRequest(itemsFromRequest: List<CnOnPnGpaRequest.Tender.Item>) {
        val idsAreUniques = itemsFromRequest.uniqueBy { it.id }
        if (idsAreUniques.not())
            throw throw ErrorException(ITEM_ID_IS_DUPLICATED)
    }

    /**
     * VR-3.8.16(CN on PN) "Contract Period" (Lot)
     *
     * IF (pmd == "GPA")  eAccess checks lot.contractPeriod.startDate in every Lot object from DB:
     *   IF value of lot.contractPeriod.startDate from DB > (later than) value of preQualification.period.endDate
     *   from Request, validation is successful;
     *   else eAccess throws Exception: "Invalid date-time values in lot contract period";
     */
    private fun checkContractPeriodInLotsFromRequestWhenPNWithItems(
        preQualificationPeriodEndDate: LocalDateTime,
        lotsFromPN: List<PNEntity.Tender.Lot>
    ) {
        lotsFromPN.forEach { lot ->
            if (lot.contractPeriod.startDate <= preQualificationPeriodEndDate)
                throw ErrorException(
                    error = INVALID_LOT_CONTRACT_PERIOD,
                    message = "The start date [${lot.contractPeriod.startDate}] of the contract period of the lot [${lot.id}] less or eq that the tender period end date [$preQualificationPeriodEndDate]. "
                )
        }
    }

    /**
     * VR-3.8.17(CN on PN)  "Related Lots"(documents) -> VR-3.7.13(Update CNEntity)
     *
     * VR-3.6.12 "Related Lots" (documents)
     * Access проверяет, что значения указанные в поле relatedLots (document.relatedLots) каждого объекта
     * секции Documents имеют соответствие в списке значений tender.lots.id.
     */
    private fun checkRelatedLotsInDocumentsFromRequestWhenPNWithItems(
        lotsIdsFromPN: Set<String>,
        documentsFromRequest: List<CnOnPnGpaRequest.Tender.Document>
    ) {
        documentsFromRequest.forEach { document ->
            document.relatedLots?.forEach { relatedLot ->
                if (relatedLot !in lotsIdsFromPN)
                    throw ErrorException(
                        error = INVALID_DOCS_RELATED_LOTS,
                        message = "The document from request with id '${document.id}' contains invalid related lot '$relatedLot'. Valid lot ids: $lotsIdsFromPN."
                    )
            }
        }
    }

    /**
     * VR-1.0.1.7.7
     */
    private fun checkAuctionsAreRequired(
        context: CheckCnOnPnGpaContext,
        data: CnOnPnGpaRequest,
        mainProcurementCategory: MainProcurementCategory
    ) {
        val isAuctionRequired = rulesService.isAuctionRequired(
            context.country,
            context.pmd,
            mainProcurementCategory
        )

        if (isAuctionRequired) {
            val procurementMethodModalities = data.tender.procurementMethodModalities
            if (procurementMethodModalities == null || procurementMethodModalities.isEmpty())
                throw ErrorException(INVALID_PMM)

            val electronicAuctions = data.tender.electronicAuctions
            if (electronicAuctions == null || electronicAuctions.details.isEmpty())
                throw ErrorException(ErrorType.INVALID_AUCTION_IS_EMPTY)
        }
    }

    /**
     * VR-3.8.18 Status (tender)
     *
     * eAccess analyzes tender.status value from DB:
     * IF tender.status in DB == "planning", validation is successful;
     * ELSE (tender.status in DB != "planning") eAccess throws Exception: "Planning Notice can not be used";
     */
    private fun checkTenderStatus(pnEntity: PNEntity) {
        if (pnEntity.tender.status == TenderStatus.UNSUCCESSFUL)
            throw ErrorException(
                error = ErrorType.TENDER_IN_UNSUCCESSFUL_STATUS,
                message = "The tender is unsuccessful."
            )
    }

    /**
     * BR-3.8.14(CN on PN) -> BR-3.6.30(CN)
     *
     * eAccess add object "Value":
     *      "Amount" (tender.value.amount) is obtained by summation of values from "Amount" (tender.lot.value.amount)
     *      of all lot objects from Request.
     *      eAccess sets "Currency" (tender.value.currency) == "Currency" (tender.lot.value.currency) from Request.
     */
    private fun calculateTenderValueFromLots(lotsFromRequest: List<CnOnPnGpaRequest.Tender.Lot>): CNEntity.Tender.Value {
        val currency = lotsFromRequest.elementAt(0).value.currency
        val totalAmount = lotsFromRequest.fold(BigDecimal.ZERO) { acc, lot ->
            acc.plus(lot.value.amount)
        }.setScale(2, RoundingMode.HALF_UP)
        return CNEntity.Tender.Value(totalAmount, currency)
    }

    private fun calculationTenderContractPeriod(lots: List<CnOnPnGpaRequest.Tender.Lot>): CNEntity.Tender.ContractPeriod {
        val contractPeriodSet = lots.asSequence().map { it.contractPeriod }.toSet()
        val startDate = contractPeriodSet.minBy { it.startDate }!!.startDate
        val endDate = contractPeriodSet.maxBy { it.endDate }!!.endDate
        return CNEntity.Tender.ContractPeriod(startDate, endDate)
    }

}

