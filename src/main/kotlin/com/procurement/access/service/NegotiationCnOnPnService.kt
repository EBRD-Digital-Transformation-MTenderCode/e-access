package com.procurement.access.service

import com.procurement.access.application.service.CheckNegotiationCnOnPnContext
import com.procurement.access.application.service.CheckedNegotiationCnOnPn
import com.procurement.access.application.service.CreateNegotiationCnOnPnContext
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.enums.AwardCriteria
import com.procurement.access.domain.model.enums.DocumentType
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.domain.model.enums.TenderDocumentType
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.exception.ErrorType.DATA_NOT_FOUND
import com.procurement.access.exception.ErrorType.INVALID_DOCS_ID
import com.procurement.access.exception.ErrorType.INVALID_DOCS_RELATED_LOTS
import com.procurement.access.exception.ErrorType.INVALID_ITEMS_RELATED_LOTS
import com.procurement.access.exception.ErrorType.INVALID_LOT_CONTRACT_PERIOD
import com.procurement.access.exception.ErrorType.INVALID_LOT_CURRENCY
import com.procurement.access.exception.ErrorType.INVALID_OWNER
import com.procurement.access.exception.ErrorType.INVALID_TENDER_AMOUNT
import com.procurement.access.exception.ErrorType.INVALID_TOKEN
import com.procurement.access.exception.ErrorType.ITEM_ID_IS_DUPLICATED
import com.procurement.access.exception.ErrorType.LOT_ID_DUPLICATED
import com.procurement.access.infrastructure.dto.cn.NegotiationCnOnPnRequest
import com.procurement.access.infrastructure.dto.cn.NegotiationCnOnPnResponse
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.entity.PNEntity
import com.procurement.access.lib.toSetBy
import com.procurement.access.lib.uniqueBy
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toDate
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

@Service
class NegotiationCnOnPnService(
    private val generationService: GenerationService,
    private val tenderProcessDao: TenderProcessDao
) {

    fun checkNegotiationCnOnPn(
        context: CheckNegotiationCnOnPnContext,
        data: NegotiationCnOnPnRequest
    ): CheckedNegotiationCnOnPn {
        val entity: TenderProcessEntity =
            tenderProcessDao.getByCpIdAndStage(context.cpid, context.previousStage)
                ?: throw ErrorException(DATA_NOT_FOUND)

        val pnEntity: PNEntity = toObject(PNEntity::class.java, entity.jsonData)

        //VR-3.8.18 Tender status
        checkTenderStatus(pnEntity)

        //VR-3.8.3 Documents (duplicate)
        checkDocuments(documentsFromRequest = data.tender.documents, documentsFromPN = pnEntity.tender.documents)

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
            checkContractPeriodInLotsWhenPNWithoutItemsFromRequest(
                context = context,
                tenderFromRequest = data.tender
            )

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
                context = context,
                lotsFromPN = pnEntity.tender.lots
            )
            /** End check Lots */

            /** Begin check Documents*/
            //VR-3.8.17(CN on PN)  "Related Lots"(documents) -> VR-3.7.13(Update CNEntity)
            val lotsIdsFromPN = pnEntity.tender.lots.toSetBy { it.id }
            checkRelatedLotsInDocumentsFromRequestWhenPNWithItems(
                lotsIdsFromPN = lotsIdsFromPN,
                documentsFromRequest = data.tender.documents
            )
            /** End check Documents */
        }
        return CheckedNegotiationCnOnPn(requireAuction = false)
    }

    fun createNegotiationCnOnPn(
        context: CreateNegotiationCnOnPnContext,
        data: NegotiationCnOnPnRequest
    ): NegotiationCnOnPnResponse {
        val tenderProcessEntity = tenderProcessDao.getByCpIdAndStage(context.cpid, context.previousStage)
            ?: throw ErrorException(DATA_NOT_FOUND)

        val pnEntity: PNEntity = toObject(PNEntity::class.java, tenderProcessEntity.jsonData)

        val tender: CNEntity.Tender = if (pnEntity.tender.items.isEmpty())
            createTenderBasedPNWithoutItems(request = data, pnEntity = pnEntity)
        else
            createTenderBasedPNWithItems(request = data, pnEntity = pnEntity)

        val cnEntity = CNEntity(
            ocid = pnEntity.ocid,
            planning = planning(pnEntity), //BR-3.8.1
            tender = tender
        )

        tenderProcessDao.save(
            TenderProcessEntity(
                cpId = context.cpid,
                token = tenderProcessEntity.token,
                stage = context.stage,
                owner = tenderProcessEntity.owner,
                createdDate = context.startDate.toDate(),
                jsonData = toJson(cnEntity)
            )
        )

        return getResponse(cnEntity, tenderProcessEntity.token)
    }

    /**
     * VR-3.8.1 identifier token
     *
     * eAccess проверяет что найденный по token из запароса Planning Notice содержит tender.ID,
     * значение которого равно занчению параметра identifier из запроса.
     */
    private fun checkToken(tokenFromRequest: String, entity: TenderProcessEntity) {
        if (entity.token.toString() != tokenFromRequest)
            throw ErrorException(error = INVALID_TOKEN)
    }

    /**
     * VR-3.8.2 owner
     *
     * eAccess проверяет соответствие owner связанного PNEntity (выбранного из БД) и owner,
     * полученного в параметре запроса (Id platform).
     */
    private fun checkOwner(ownerFromRequest: String, entity: TenderProcessEntity) {
        if (entity.owner != ownerFromRequest)
            throw ErrorException(error = INVALID_OWNER)
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
        documentsFromRequest: List<NegotiationCnOnPnRequest.Tender.Document>,
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
        lotsFromRequest: List<NegotiationCnOnPnRequest.Tender.Lot>,
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
        lotsFromRequest: List<NegotiationCnOnPnRequest.Tender.Lot>,
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
        documentsFromRequest: List<NegotiationCnOnPnRequest.Tender.Document>
    ) {
        documentsFromRequest.forEach { document ->
            document.relatedLots?.forEach { relatedLot ->
                if (relatedLot !in lotsIdsFromRequest)
                    throw ErrorException(INVALID_DOCS_RELATED_LOTS)
            }
        }
    }

    /**
     * VR-3.8.8(CN on PN)  "Contract Period" (Lot) -> VR-3.6.7(CN)
     *
     * VR-3.6.7 "Contract Period" (Lot)
     * eAccess checks startDate && endDate values:
     *
     * IF startDate && endDate value are present in calendar of current year, validation is successful;
     * ELSE (startDate && endDate value are not found in calendar) eAccess throws Exception: "Date is not exist";
     *
     * eAccess проверяет, что значение "Contract Period: Start Date" (tender/lots/contractPeriod/startDate)
     * является более ранним по сравнению с "Contract Period: End Date" (tender/lots/contractPeriod/endDate)
     * в данном объекте секции Lots.
     *
     * eAccess проверяет, что значение "Contract Period: End Date" (tender/lots/contractPeriod/endDate)
     * является более поздним по сравнению с "Contract Period: Start Date" (tender/lots/contractPeriod/startDate)
     * в данном объекте секции Lots.
     *
     * eAccess analyzes pmd value from Request:
     * IF pmd == "OT" || "SV" || "MV", eAccess checks lot.contractPeriod.startDate in every Lot object from Request:
     *      IF value of lot.contractPeriod.startDate from Request > (later than) value of tenderPeriod.endDate
     *      from Request, validation is successful;
     *      ELSE eAccess throws Exception;
     *
     * IF (pmd == "DA" || "NP" || "OP") { eAccess checks lot.contractPeriod.startDate in every Lot object from Request:
     *      IF value of lot.contractPeriod.startDate from Request > (later than) value of startDate from
     *      the context of Request, validation is successful;
     *      ELSE eAccess throws Exception;
     */
    private fun checkContractPeriodInLotsWhenPNWithoutItemsFromRequest(
        context: CheckNegotiationCnOnPnContext,
        tenderFromRequest: NegotiationCnOnPnRequest.Tender
    ) {

        tenderFromRequest.lots.forEach { lot ->
            checkRangeContractPeriodInLotFromRequest(lot)
            if (lot.contractPeriod.startDate <= context.startDate)
                throw ErrorException(INVALID_LOT_CONTRACT_PERIOD)
        }
    }

    private fun checkRangeContractPeriodInLotFromRequest(lot: NegotiationCnOnPnRequest.Tender.Lot) {
        if (lot.contractPeriod.startDate >= lot.contractPeriod.endDate)
            throw ErrorException(INVALID_LOT_CONTRACT_PERIOD)
    }

    /**
     * VR-3.8.9(CN on PN) "Quantity" (item) -> VR-3.6.11(CN)
     *
     * VR-3.6.11 "Quantity" (item)
     * eAccess проверяет, что значению "Quantity" (tender/items/quantity) каждого объекта секции Items больше нуля.
     */
    private fun checkQuantityInItems(itemsFromRequest: List<NegotiationCnOnPnRequest.Tender.Item>) {
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
        itemsFromRequest: List<NegotiationCnOnPnRequest.Tender.Item>
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
        itemsFromRequest: List<NegotiationCnOnPnRequest.Tender.Item>
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
    private fun checkLotIdFromRequest(lotsFromRequest: List<NegotiationCnOnPnRequest.Tender.Lot>) {
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
    private fun checkItemIdFromRequest(itemsFromRequest: List<NegotiationCnOnPnRequest.Tender.Item>) {
        val idsAreUniques = itemsFromRequest.uniqueBy { it.id }
        if (idsAreUniques.not())
            throw throw ErrorException(ITEM_ID_IS_DUPLICATED)
    }

    /**
     * VR-3.8.16(CN on PN) "Contract Period" (Lot)
     *
     * eAccess analyzes pmd value from Request:
     *
     * IF pmd == "OT" || "SV" || "MV", eAccess checks lot.contractPeriod.startDate in every Lot object from DB:
     *   IF value of lot.contractPeriod.startDate from DB > (later than) value of tenderPeriod.endDate
     *   from Request, validation is successful;
     *   ELSE eAccess throws Exception;
     *
     * IF (pmd == "DA" || "NP" || "OP") { eAccess checks lot.contractPeriod.startDate in every Lot object from DB:
     *   IF value of lot.contractPeriod.startDate from DB > (later than) value of startDate
     *   from the context of Request, validation is successful;
     *   ELSE eAccess throws Exception;
     */
    private fun checkContractPeriodInLotsFromRequestWhenPNWithItems(
        context: CheckNegotiationCnOnPnContext,
        lotsFromPN: List<PNEntity.Tender.Lot>
    ) {
        lotsFromPN.forEach { lot ->
            if (lot.contractPeriod.startDate <= context.startDate)
                throw ErrorException(INVALID_LOT_CONTRACT_PERIOD)
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
        documentsFromRequest: List<NegotiationCnOnPnRequest.Tender.Document>
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

    /** Begin Business Rules */
    private fun createTenderBasedPNWithoutItems(
        request: NegotiationCnOnPnRequest,
        pnEntity: PNEntity
    ): CNEntity.Tender {
        //BR-3.6.5
        val relatedTemporalWithPermanentLotId: Map<String, String> = generatePermanentLotId(request.tender.lots)

        /** Begin BR-3.8.3 */
        val classification: CNEntity.Tender.Classification =
            classificationFromRequest(classificationFromRequest = request.tender.classification!!)
        val lots: List<CNEntity.Tender.Lot> = convertRequestLots(request.tender, relatedTemporalWithPermanentLotId)
        val items: List<CNEntity.Tender.Item> =
            convertRequestItems(request.tender.items, relatedTemporalWithPermanentLotId)
        /** End BR-3.8.3 */

        /** Begin BR-3.8.4 */
        //BR-3.8.14 -> BR-3.6.30
        val value: CNEntity.Tender.Value = calculateTenderValueFromLots(request.tender.lots)
        //BR-3.8.15 -> BR-3.6.31
        val contractPeriod: CNEntity.Tender.ContractPeriod = calculationTenderContractPeriod(lots = request.tender.lots)
        /** End BR-3.8.4 */

        //BR-3.8.18 -> BR-3.7.13
        val updatedDocuments: List<CNEntity.Tender.Document> = updateDocuments(
            documentsFromRequest = request.tender.documents,
            documentsFromDB = pnEntity.tender.documents ?: emptyList(),
            relatedTemporalWithPermanentLotId = relatedTemporalWithPermanentLotId
        )

        return tender(
            request = request,
            pnEntity = pnEntity,
            classification = classification,
            lots = lots,
            items = items,
            value = value,
            contractPeriod = contractPeriod,
            updatedDocuments = updatedDocuments
        )
    }

    private fun createTenderBasedPNWithItems(request: NegotiationCnOnPnRequest, pnEntity: PNEntity): CNEntity.Tender {
        /** Begin BR-3.8.3 */
        val classification: CNEntity.Tender.Classification =
            classificationFromPNToCN(classificationFromPN = pnEntity.tender.classification)
        val lots: List<CNEntity.Tender.Lot> = lotsFromPNToCN(lotsFromPN = pnEntity.tender.lots)
        val items: List<CNEntity.Tender.Item> = itemsFromPNToCN(itemsFromPN = pnEntity.tender.items)
        /** End BR-3.8.3 */

        /** Begin BR-3.8.4 */
        val value: CNEntity.Tender.Value = pnEntity.tender.value.let {
            CNEntity.Tender.Value(
                amount = it.amount,
                currency = it.currency
            )
        }
        val contractPeriod: CNEntity.Tender.ContractPeriod = pnEntity.tender.contractPeriod!!.let {
            CNEntity.Tender.ContractPeriod(
                startDate = it.startDate,
                endDate = it.endDate
            )
        }
        /** End BR-3.8.4 */

        //BR-3.8.18 -> BR-3.7.13
        val updatedDocuments: List<CNEntity.Tender.Document> = updateDocuments(
            documentsFromRequest = request.tender.documents,
            documentsFromDB = pnEntity.tender.documents ?: emptyList(),
            relatedTemporalWithPermanentLotId = emptyMap()
        )

        return tender(
            request = request,
            pnEntity = pnEntity,
            classification = classification,
            lots = lots,
            items = items,
            value = value,
            contractPeriod = contractPeriod,
            updatedDocuments = updatedDocuments
        )
    }

    private fun planning(pnEntity: PNEntity): CNEntity.Planning {
        return CNEntity.Planning(
            rationale = pnEntity.planning.rationale,
            budget = CNEntity.Planning.Budget(
                description = pnEntity.planning.budget.description,
                amount = pnEntity.planning.budget.amount.let {
                    CNEntity.Planning.Budget.Amount(
                        amount = it.amount,
                        currency = it.currency
                    )
                },
                isEuropeanUnionFunded = pnEntity.planning.budget.isEuropeanUnionFunded,
                budgetBreakdowns = pnEntity.planning.budget.budgetBreakdowns.map { budgetBreakdown ->
                    CNEntity.Planning.Budget.BudgetBreakdown(
                        id = budgetBreakdown.id,
                        description = budgetBreakdown.description,
                        amount = budgetBreakdown.amount.let {
                            CNEntity.Planning.Budget.BudgetBreakdown.Amount(
                                amount = it.amount,
                                currency = it.currency
                            )
                        },
                        period = budgetBreakdown.period.let {
                            CNEntity.Planning.Budget.BudgetBreakdown.Period(
                                startDate = it.startDate,
                                endDate = it.endDate
                            )
                        },
                        sourceParty = budgetBreakdown.sourceParty.let {
                            CNEntity.Planning.Budget.BudgetBreakdown.SourceParty(
                                name = it.name,
                                id = it.id
                            )
                        },
                        europeanUnionFunding = budgetBreakdown.europeanUnionFunding?.let {
                            CNEntity.Planning.Budget.BudgetBreakdown.EuropeanUnionFunding(
                                projectIdentifier = it.projectIdentifier,
                                projectName = it.projectName,
                                uri = it.uri
                            )
                        }
                    )
                }
            )
        )
    }

    private fun tender(
        request: NegotiationCnOnPnRequest,
        pnEntity: PNEntity,
        classification: CNEntity.Tender.Classification,
        lots: List<CNEntity.Tender.Lot>,
        items: List<CNEntity.Tender.Item>,
        value: CNEntity.Tender.Value,
        contractPeriod: CNEntity.Tender.ContractPeriod,
        updatedDocuments: List<CNEntity.Tender.Document>
    ): CNEntity.Tender {
        /** Begin BR-3.8.8(CN on PN) Status StatusDetails (tender) -> BR-3.6.2(CN)*/
        val status = TenderStatus.ACTIVE
        val statusDetails: TenderStatusDetails = TenderStatusDetails.NEGOTIATION
        /** End BR-3.8.8(CN on PN) Status StatusDetails (tender) -> BR-3.6.2(CN)*/

        return CNEntity.Tender(
            id = pnEntity.tender.id, //BR-3.8.1
            /** Begin BR-3.8.8 -> BR-3.6.2*/
            status = status,
            statusDetails = statusDetails,
            /** End BR-3.8.8 -> BR-3.6.2*/

            classification = classification,
            title = pnEntity.tender.title, //BR-3.8.1
            description = pnEntity.tender.description, //BR-3.8.1
            //BR-3.8.1
            acceleratedProcedure = pnEntity.tender.acceleratedProcedure.let {
                CNEntity.Tender.AcceleratedProcedure(
                    isAcceleratedProcedure = it.isAcceleratedProcedure
                )
            },
            //BR-3.8.1
            designContest = pnEntity.tender.designContest.let {
                CNEntity.Tender.DesignContest(
                    serviceContractAward = it.serviceContractAward
                )
            },
            //BR-3.8.1
            electronicWorkflows = pnEntity.tender.electronicWorkflows.let {
                CNEntity.Tender.ElectronicWorkflows(
                    useOrdering = it.useOrdering,
                    usePayment = it.usePayment,
                    acceptInvoicing = it.acceptInvoicing
                )
            },
            //BR-3.8.1
            jointProcurement = pnEntity.tender.jointProcurement.let {
                CNEntity.Tender.JointProcurement(
                    isJointProcurement = it.isJointProcurement
                )
            },
            //BR-3.8.1
            procedureOutsourcing = pnEntity.tender.procedureOutsourcing.let {
                CNEntity.Tender.ProcedureOutsourcing(
                    procedureOutsourced = it.procedureOutsourced
                )
            },
            //BR-3.8.1
            framework = pnEntity.tender.framework.let {
                CNEntity.Tender.Framework(
                    isAFramework = it.isAFramework
                )
            },
            //BR-3.8.1
            dynamicPurchasingSystem = pnEntity.tender.dynamicPurchasingSystem.let {
                CNEntity.Tender.DynamicPurchasingSystem(
                    hasDynamicPurchasingSystem = it.hasDynamicPurchasingSystem
                )
            },
            legalBasis = pnEntity.tender.legalBasis, //BR-3.8.1
            procurementMethod = pnEntity.tender.procurementMethod, //BR-3.8.1
            procurementMethodDetails = pnEntity.tender.procurementMethodDetails,//BR-3.8.1
            procurementMethodRationale = request.tender.procurementMethodRationale,
            procurementMethodAdditionalInfo = request.tender.procurementMethodAdditionalInfo,
            mainProcurementCategory = pnEntity.tender.mainProcurementCategory, //BR-3.8.1
            eligibilityCriteria = pnEntity.tender.eligibilityCriteria, //BR-3.8.1

            //BR-3.8.17 -> BR-3.6.22 | VR-3.6.16
            awardCriteria = request.tender.awardCriteria ?: AwardCriteria.PRICE_ONLY,
            awardCriteriaDetails = null,
            tenderPeriod = null,
            contractPeriod = contractPeriod,
            enquiryPeriod = null,
            procurementMethodModalities = null,
            electronicAuctions = null, //BR-3.8.5 -> BR-3.6.5
            //BR-3.8.1
            procuringEntity = pnEntity.tender.procuringEntity.let { procuringEntity ->
                CNEntity.Tender.ProcuringEntity(
                    id = procuringEntity.id,
                    name = procuringEntity.name,
                    identifier = procuringEntity.identifier.let { identifier ->
                        CNEntity.Tender.ProcuringEntity.Identifier(
                            scheme = identifier.scheme,
                            id = identifier.id,
                            legalName = identifier.legalName,
                            uri = identifier.uri
                        )
                    },
                    additionalIdentifiers = procuringEntity.additionalIdentifiers?.map { additionalIdentifier ->
                        CNEntity.Tender.ProcuringEntity.AdditionalIdentifier(
                            scheme = additionalIdentifier.scheme,
                            id = additionalIdentifier.id,
                            legalName = additionalIdentifier.legalName,
                            uri = additionalIdentifier.uri
                        )
                    },
                    address = procuringEntity.address.let { address ->
                        CNEntity.Tender.ProcuringEntity.Address(
                            streetAddress = address.streetAddress,
                            postalCode = address.postalCode,
                            addressDetails = address.addressDetails.let { addressDetails ->
                                CNEntity.Tender.ProcuringEntity.Address.AddressDetails(
                                    country = addressDetails.country.let { country ->
                                        CNEntity.Tender.ProcuringEntity.Address.AddressDetails.Country(
                                            scheme = country.scheme,
                                            id = country.id,
                                            description = country.description,
                                            uri = country.uri
                                        )
                                    },
                                    region = addressDetails.region.let { region ->
                                        CNEntity.Tender.ProcuringEntity.Address.AddressDetails.Region(
                                            scheme = region.scheme,
                                            id = region.id,
                                            description = region.description,
                                            uri = region.uri
                                        )
                                    },
                                    locality = addressDetails.locality.let { locality ->
                                        CNEntity.Tender.ProcuringEntity.Address.AddressDetails.Locality(
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
                        CNEntity.Tender.ProcuringEntity.ContactPoint(
                            name = contactPoint.name,
                            email = contactPoint.email,
                            telephone = contactPoint.telephone,
                            faxNumber = contactPoint.faxNumber,
                            url = contactPoint.url
                        )
                    },
                    persones = null
                )
            },
            value = value,
            //BR-3.8.1
            lotGroups = pnEntity.tender.lotGroups.map {
                CNEntity.Tender.LotGroup(
                    optionToCombine = it.optionToCombine
                )
            },
            lots = lots, //BR-3.8.3
            items = items, //BR-3.8.3
            requiresElectronicCatalogue = pnEntity.tender.requiresElectronicCatalogue, //BR-3.8.1
            submissionMethod = pnEntity.tender.submissionMethod, //BR-3.8.1
            submissionMethodRationale = pnEntity.tender.submissionMethodRationale, //BR-3.8.1
            submissionMethodDetails = pnEntity.tender.submissionMethodDetails, //BR-3.8.1
            documents = updatedDocuments //BR-3.7.13
        )
    }

    /**
     * BR-3.8.5(CN on PN) lot id (tender.lots.id) -> BR-3.6.5
     *
     * eAccess меняет временные "ID" (tender/lot/id) лотов на постоянные.
     * Постоянные "ID" (tender/lot/id) лотов формируются как уникальные для данного контрактного процесса
     * 32-символьные идентификаторы.
     */
    private fun generatePermanentLotId(lots: List<NegotiationCnOnPnRequest.Tender.Lot>): Map<String, String> {
        return lots.asSequence()
            .map { lot ->
                val permanentId = generationService.generatePermanentLotId() //BR-3.8.6
                lot.id to permanentId
            }
            .toMap()
    }

    private fun updateDocuments(
        documentsFromRequest: List<NegotiationCnOnPnRequest.Tender.Document>,
        documentsFromDB: List<PNEntity.Tender.Document>,
        relatedTemporalWithPermanentLotId: Map<String, String>
    ): List<CNEntity.Tender.Document> {
        return if (documentsFromDB.isNotEmpty()) {
            val documentsFromRequestById: Map<String, NegotiationCnOnPnRequest.Tender.Document> =
                documentsFromRequest.associateBy { document -> document.id }
            val existsDocumentsById: Map<String, PNEntity.Tender.Document> =
                documentsFromDB.associateBy { document -> document.id }

            val updatedDocuments: Set<CNEntity.Tender.Document> = updateExistsDocuments(
                documentsFromRequestById = documentsFromRequestById,
                existsDocumentsById = existsDocumentsById,
                relatedTemporalWithPermanentLotId = relatedTemporalWithPermanentLotId
            )

            val newDocumentsFromRequest: Set<NegotiationCnOnPnRequest.Tender.Document> = extractNewDocuments(
                documentsFromRequest = documentsFromRequest,
                existsDocumentsById = existsDocumentsById
            )

            val newDocuments: List<CNEntity.Tender.Document> = convertNewDocuments(
                newDocumentsFromRequest = newDocumentsFromRequest,
                relatedTemporalWithPermanentLotId = relatedTemporalWithPermanentLotId
            )

            updatedDocuments.union(newDocuments).toList()
        } else {
            convertNewDocuments(
                newDocumentsFromRequest = documentsFromRequest,
                relatedTemporalWithPermanentLotId = relatedTemporalWithPermanentLotId
            )
        }
    }

    private fun updateExistsDocuments(
        documentsFromRequestById: Map<String, NegotiationCnOnPnRequest.Tender.Document>,
        existsDocumentsById: Map<String, PNEntity.Tender.Document>,
        relatedTemporalWithPermanentLotId: Map<String, String>
    ): Set<CNEntity.Tender.Document> {
        return existsDocumentsById.asSequence()
            .map { (id, document) ->
                val documentSource =
                    documentsFromRequestById[id]
                        ?: throw ErrorException(
                            error = INVALID_DOCS_ID,
                            message = "Document with id: '$id' from db not contains in request"
                        )

                val relatedLots = getPermanentLotsIds(
                    temporalIds = documentSource.relatedLots,
                    relatedTemporalWithPermanentLotId = relatedTemporalWithPermanentLotId
                )

                CNEntity.Tender.Document(
                    documentType = document.documentType,
                    id = document.id,
                    title = documentSource.title,
                    description = documentSource.description,
                    //BR-3.6.5(CN)
                    relatedLots = relatedLots
                )
            }.toSet()
    }

    private fun extractNewDocuments(
        documentsFromRequest: Collection<NegotiationCnOnPnRequest.Tender.Document>,
        existsDocumentsById: Map<String, PNEntity.Tender.Document>
    ): Set<NegotiationCnOnPnRequest.Tender.Document> {
        return documentsFromRequest.asSequence()
            .filter { document -> !existsDocumentsById.containsKey(document.id) }
            .toSet()
    }

    private fun convertNewDocuments(
        newDocumentsFromRequest: Collection<NegotiationCnOnPnRequest.Tender.Document>,
        relatedTemporalWithPermanentLotId: Map<String, String>
    ): List<CNEntity.Tender.Document> {
        return newDocumentsFromRequest.map { document ->
            convertNewDocument(document, relatedTemporalWithPermanentLotId)
        }
    }

    private fun convertNewDocument(
        newDocumentFromRequest: NegotiationCnOnPnRequest.Tender.Document,
        relatedTemporalWithPermanentLotId: Map<String, String>
    ): CNEntity.Tender.Document {
        val relatedLots = getPermanentLotsIds(
            temporalIds = newDocumentFromRequest.relatedLots,
            relatedTemporalWithPermanentLotId = relatedTemporalWithPermanentLotId
        )

        return CNEntity.Tender.Document(
            id = newDocumentFromRequest.id,
            documentType = DocumentType.creator(newDocumentFromRequest.documentType.key),
            title = newDocumentFromRequest.title,
            description = newDocumentFromRequest.description,
            //BR-3.6.5(CN)
            relatedLots = relatedLots
        )
    }

    private fun getPermanentLotsIds(
        temporalIds: List<String>?,
        relatedTemporalWithPermanentLotId: Map<String, String>
    ): List<String>? {
        return if (temporalIds != null && relatedTemporalWithPermanentLotId.isNotEmpty())
            temporalIds.map { relatedTemporalWithPermanentLotId.getValue(it) }
        else
            temporalIds
    }

    private fun convertRequestItems(
        itemsFromRequest: List<NegotiationCnOnPnRequest.Tender.Item>,
        relatedTemporalWithPermanentLotId: Map<String, String>
    ): List<CNEntity.Tender.Item> {
        return itemsFromRequest.map { item ->
            CNEntity.Tender.Item(
                //BR-3.8.6(CN on PN) item id (tender.items.id) -> BR-3.6.6
                id = generationService.generatePermanentItemId(),
                internalId = item.internalId,
                description = item.description,
                classification = item.classification.let { classification ->
                    CNEntity.Tender.Item.Classification(
                        scheme = classification.scheme,
                        id = classification.id,
                        description = classification.description
                    )
                },
                additionalClassifications = item.additionalClassifications
                    ?.map { additionalClassification ->
                        CNEntity.Tender.Item.AdditionalClassification(
                            scheme = additionalClassification.scheme,
                            id = additionalClassification.id,
                            description = additionalClassification.description
                        )
                    },
                quantity = item.quantity,
                unit = item.unit.let { unit ->
                    CNEntity.Tender.Item.Unit(
                        id = unit.id,
                        name = unit.name
                    )
                },
                relatedLot = relatedTemporalWithPermanentLotId.getValue(item.relatedLot) //BR-3.8.6(CN on PN) -> BR-3.6.5(CN)
            )
        }
    }

    /**
     * BR-3.8.3
     */
    private fun convertRequestLots(
        tender: NegotiationCnOnPnRequest.Tender,
        relatedTemporalWithPermanentLotId: Map<String, String>
    ): List<CNEntity.Tender.Lot> {
        return tender.lots.map { lot ->
            CNEntity.Tender.Lot(
                id = relatedTemporalWithPermanentLotId.getValue(lot.id), //BR-3.8.5
                internalId = lot.internalId,
                title = lot.title,
                description = lot.description,
                /** Begin BR-3.8.7 -> BR-3.6.1 */
                status = LotStatus.ACTIVE,
                statusDetails = LotStatusDetails.EMPTY,
                /** End BR-3.8.7 -> BR-3.6.1 */

                //BR-3.8.4; BR-3.8.14 -> BR-3.6.30
                value = lot.value.let { value ->
                    CNEntity.Tender.Lot.Value(
                        amount = value.amount,
                        currency = value.currency
                    )
                },
                /** Begin BR-3.8.4 */
                //BR-3.8.9 -> BR-3.6.17
                options = listOf(CNEntity.Tender.Lot.Option(false)), //BR-3.8.4; BR-3.8.9 -> BR-3.6.17
                //BR-3.8.10 -> BR-3.6.18
                variants = listOf(CNEntity.Tender.Lot.Variant(false)), //BR-3.8.4; BR-3.8.10 -> BR-3.6.18
                //BR-3.8.11 -> BR-3.6.19
                renewals = listOf(CNEntity.Tender.Lot.Renewal(false)), //BR-3.8.4; BR-3.8.11 -> BR-3.6.19
                //BR-3.8.12 -> BR-3.6.20
                recurrentProcurement = listOf(CNEntity.Tender.Lot.RecurrentProcurement(false)), //BR-3.8.4; BR-3.8.12 -> BR-3.6.20
                /** End BR-3.8.4 */

                //BR-3.8.4; BR-3.8.15 -> BR-3.6.31
                contractPeriod = lot.contractPeriod.let { contractPeriod ->
                    CNEntity.Tender.Lot.ContractPeriod(
                        startDate = contractPeriod.startDate,
                        endDate = contractPeriod.endDate
                    )
                },
                placeOfPerformance = lot.placeOfPerformance.let { placeOfPerformance ->
                    CNEntity.Tender.Lot.PlaceOfPerformance(
                        address = placeOfPerformance.address.let { address ->
                            CNEntity.Tender.Lot.PlaceOfPerformance.Address(
                                streetAddress = address.streetAddress,
                                postalCode = address.postalCode,
                                addressDetails = address.addressDetails.let { addressDetails ->
                                    CNEntity.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                        country = addressDetails.country.let { country ->
                                            CNEntity.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                                scheme = country.scheme!!, //VR-3.14.1(CheckItem)
                                                id = country.id,
                                                description = country.description!!,
                                                uri = country.uri!!
                                            )
                                        },
                                        region = addressDetails.region.let { region ->
                                            CNEntity.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                                scheme = region.scheme!!,
                                                id = region.id,
                                                description = region.description!!,
                                                uri = region.uri!!
                                            )
                                        },
                                        locality = addressDetails.locality.let { locality ->
                                            CNEntity.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
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

    /**
     * BR-3.8.14(CN on PN) -> BR-3.6.30(CN)
     *
     * eAccess add object "Value":
     *      "Amount" (tender.value.amount) is obtained by summation of values from "Amount" (tender.lot.value.amount)
     *      of all lot objects from Request.
     *      eAccess sets "Currency" (tender.value.currency) == "Currency" (tender.lot.value.currency) from Request.
     */
    private fun calculateTenderValueFromLots(lotsFromRequest: List<NegotiationCnOnPnRequest.Tender.Lot>): CNEntity.Tender.Value {
        val currency = lotsFromRequest.elementAt(0).value.currency
        val totalAmount = lotsFromRequest.fold(BigDecimal.ZERO) { acc, lot ->
            acc.plus(lot.value.amount)
        }.setScale(2, RoundingMode.HALF_UP)
        return CNEntity.Tender.Value(totalAmount, currency)
    }

    private fun calculationTenderContractPeriod(lots: List<NegotiationCnOnPnRequest.Tender.Lot>): CNEntity.Tender.ContractPeriod {
        val contractPeriodSet = lots.asSequence().map { it.contractPeriod }.toSet()
        val startDate = contractPeriodSet.minBy { it.startDate }!!.startDate
        val endDate = contractPeriodSet.maxBy { it.endDate }!!.endDate
        return CNEntity.Tender.ContractPeriod(startDate, endDate)
    }

    /**
     * BR-3.8.3
     */
    private fun classificationFromRequest(
        classificationFromRequest: NegotiationCnOnPnRequest.Tender.Classification
    ): CNEntity.Tender.Classification {
        return classificationFromRequest.let {
            CNEntity.Tender.Classification(
                scheme = it.scheme,
                id = it.id,
                description = it.description
            )
        }
    }

    /**
     * BR-3.8.3
     */
    private fun classificationFromPNToCN(
        classificationFromPN: PNEntity.Tender.Classification
    ): CNEntity.Tender.Classification {
        return classificationFromPN.let {
            CNEntity.Tender.Classification(
                scheme = it.scheme,
                id = it.id,
                description = it.description
            )
        }
    }

    /**
     * BR-3.8.3
     */
    private fun lotsFromPNToCN(lotsFromPN: List<PNEntity.Tender.Lot>): List<CNEntity.Tender.Lot> {
        return lotsFromPN.map { lot ->
            /** Begin BR-3.8.7 */
            val status = if (lot.status == LotStatus.PLANNING)
                LotStatus.ACTIVE
            else
                lot.status
            /** End BR-3.8.7 */

            CNEntity.Tender.Lot(
                //BR-3.8.5
                id = lot.id,

                internalId = null,
                title = lot.title,
                description = lot.description,
                /** Begin BR-3.8.7 */
                status = status,
                statusDetails = LotStatusDetails.EMPTY,
                /** End BR-3.8.7 */
                value = lot.value.let { value ->
                    CNEntity.Tender.Lot.Value(
                        amount = value.amount,
                        currency = value.currency
                    )
                },
                options = listOf(CNEntity.Tender.Lot.Option(false)), //BR-3.8.9 -> BR-3.6.17
                recurrentProcurement = listOf(CNEntity.Tender.Lot.RecurrentProcurement(false)), //BR-3.8.12 -> BR-3.6.20
                renewals = listOf(CNEntity.Tender.Lot.Renewal(false)), //BR-3.8.11 -> BR-3.6.19
                variants = listOf(CNEntity.Tender.Lot.Variant(false)), //BR-3.8.10 -> BR-3.6.18
                contractPeriod = lot.contractPeriod.let { contractPeriod ->
                    CNEntity.Tender.Lot.ContractPeriod(
                        startDate = contractPeriod.startDate,
                        endDate = contractPeriod.endDate
                    )
                },
                placeOfPerformance = lot.placeOfPerformance.let { placeOfPerformance ->
                    CNEntity.Tender.Lot.PlaceOfPerformance(
                        address = placeOfPerformance.address.let { address ->
                            CNEntity.Tender.Lot.PlaceOfPerformance.Address(
                                streetAddress = address.streetAddress,
                                postalCode = address.postalCode,
                                addressDetails = address.addressDetails.let { addressDetails ->
                                    CNEntity.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                        country = addressDetails.country.let { country ->
                                            CNEntity.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                                scheme = country.scheme, //VR-3.14.1(CheckItem)
                                                id = country.id,
                                                description = country.description,
                                                uri = country.uri
                                            )
                                        },
                                        region = addressDetails.region.let { region ->
                                            CNEntity.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                                scheme = region.scheme,
                                                id = region.id,
                                                description = region.description,
                                                uri = region.uri
                                            )
                                        },
                                        locality = addressDetails.locality.let { locality ->
                                            CNEntity.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
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

    /**
     * BR-3.8.3
     */
    private fun itemsFromPNToCN(itemsFromPN: List<PNEntity.Tender.Item>): List<CNEntity.Tender.Item> {
        return itemsFromPN.map { item ->
            CNEntity.Tender.Item(
                //BR-3.8.6
                id = item.id,
                internalId = null,
                description = item.description,
                classification = item.classification.let { classification ->
                    CNEntity.Tender.Item.Classification(
                        scheme = classification.scheme,
                        id = classification.id,
                        description = classification.description
                    )
                },
                additionalClassifications = item.additionalClassifications
                    ?.map { additionalClassification ->
                        CNEntity.Tender.Item.AdditionalClassification(
                            scheme = additionalClassification.scheme,
                            id = additionalClassification.id,
                            description = additionalClassification.description
                        )
                    },
                quantity = item.quantity,
                unit = item.unit.let { unit ->
                    CNEntity.Tender.Item.Unit(
                        id = unit.id,
                        name = unit.name
                    )
                },
                relatedLot = item.relatedLot
            )
        }
    }

    private fun getResponse(cn: CNEntity, token: UUID): NegotiationCnOnPnResponse {
        return NegotiationCnOnPnResponse(
            ocid = cn.ocid,
            token = token.toString(),
            planning = cn.planning.let { planning ->
                NegotiationCnOnPnResponse.Planning(
                    rationale = planning.rationale,
                    budget = planning.budget.let { budget ->
                        NegotiationCnOnPnResponse.Planning.Budget(
                            description = budget.description,
                            amount = budget.amount.let { amount ->
                                NegotiationCnOnPnResponse.Planning.Budget.Amount(
                                    amount = amount.amount,
                                    currency = amount.currency
                                )
                            },
                            isEuropeanUnionFunded = budget.isEuropeanUnionFunded,
                            budgetBreakdowns = budget.budgetBreakdowns.map { budgetBreakdown ->
                                NegotiationCnOnPnResponse.Planning.Budget.BudgetBreakdown(
                                    id = budgetBreakdown.id,
                                    description = budgetBreakdown.description,
                                    amount = budgetBreakdown.amount.let { amount ->
                                        NegotiationCnOnPnResponse.Planning.Budget.BudgetBreakdown.Amount(
                                            amount = amount.amount,
                                            currency = amount.currency
                                        )
                                    },
                                    period = budgetBreakdown.period.let { period ->
                                        NegotiationCnOnPnResponse.Planning.Budget.BudgetBreakdown.Period(
                                            startDate = period.startDate,
                                            endDate = period.endDate
                                        )
                                    },
                                    sourceParty = budgetBreakdown.sourceParty.let { sourceParty ->
                                        NegotiationCnOnPnResponse.Planning.Budget.BudgetBreakdown.SourceParty(
                                            id = sourceParty.id,
                                            name = sourceParty.name
                                        )
                                    },
                                    europeanUnionFunding = budgetBreakdown.europeanUnionFunding?.let { europeanUnionFunding ->
                                        NegotiationCnOnPnResponse.Planning.Budget.BudgetBreakdown.EuropeanUnionFunding(
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
            tender = cn.tender.let { tender ->
                NegotiationCnOnPnResponse.Tender(
                    id = tender.id,
                    status = tender.status,
                    statusDetails = tender.statusDetails,
                    title = tender.title,
                    description = tender.description,
                    classification = tender.classification.let { classification ->
                        NegotiationCnOnPnResponse.Tender.Classification(
                            scheme = classification.scheme,
                            id = classification.id,
                            description = classification.description
                        )
                    },
                    acceleratedProcedure = tender.acceleratedProcedure.let { acceleratedProcedure ->
                        NegotiationCnOnPnResponse.Tender.AcceleratedProcedure(
                            isAcceleratedProcedure = acceleratedProcedure.isAcceleratedProcedure
                        )
                    },
                    designContest = tender.designContest.let { designContest ->
                        NegotiationCnOnPnResponse.Tender.DesignContest(
                            serviceContractAward = designContest.serviceContractAward
                        )
                    },
                    electronicWorkflows = tender.electronicWorkflows.let { electronicWorkflows ->
                        NegotiationCnOnPnResponse.Tender.ElectronicWorkflows(
                            useOrdering = electronicWorkflows.useOrdering,
                            usePayment = electronicWorkflows.usePayment,
                            acceptInvoicing = electronicWorkflows.acceptInvoicing
                        )
                    },
                    jointProcurement = tender.jointProcurement.let { jointProcurement ->
                        NegotiationCnOnPnResponse.Tender.JointProcurement(
                            isJointProcurement = jointProcurement.isJointProcurement
                        )
                    },
                    procedureOutsourcing = tender.procedureOutsourcing.let { procedureOutsourcing ->
                        NegotiationCnOnPnResponse.Tender.ProcedureOutsourcing(
                            procedureOutsourced = procedureOutsourcing.procedureOutsourced
                        )
                    },
                    framework = tender.framework.let { framework ->
                        NegotiationCnOnPnResponse.Tender.Framework(
                            isAFramework = framework.isAFramework
                        )
                    },
                    dynamicPurchasingSystem = tender.dynamicPurchasingSystem.let { dynamicPurchasingSystem ->
                        NegotiationCnOnPnResponse.Tender.DynamicPurchasingSystem(
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
                        NegotiationCnOnPnResponse.Tender.ContractPeriod(
                            startDate = contractPeriod.startDate,
                            endDate = contractPeriod.endDate
                        )
                    },
                    procuringEntity = tender.procuringEntity.let { procuringEntity ->
                        NegotiationCnOnPnResponse.Tender.ProcuringEntity(
                            id = procuringEntity.id,
                            name = procuringEntity.name,
                            identifier = procuringEntity.identifier.let { identifier ->
                                NegotiationCnOnPnResponse.Tender.ProcuringEntity.Identifier(
                                    scheme = identifier.scheme,
                                    id = identifier.id,
                                    legalName = identifier.legalName,
                                    uri = identifier.uri
                                )
                            },
                            additionalIdentifiers = procuringEntity.additionalIdentifiers?.map { additionalIdentifier ->
                                NegotiationCnOnPnResponse.Tender.ProcuringEntity.AdditionalIdentifier(
                                    scheme = additionalIdentifier.scheme,
                                    id = additionalIdentifier.id,
                                    legalName = additionalIdentifier.legalName,
                                    uri = additionalIdentifier.uri
                                )
                            },
                            address = procuringEntity.address.let { address ->
                                NegotiationCnOnPnResponse.Tender.ProcuringEntity.Address(
                                    streetAddress = address.streetAddress,
                                    postalCode = address.postalCode,
                                    addressDetails = address.addressDetails.let { addressDetails ->
                                        NegotiationCnOnPnResponse.Tender.ProcuringEntity.Address.AddressDetails(
                                            country = addressDetails.country.let { country ->
                                                NegotiationCnOnPnResponse.Tender.ProcuringEntity.Address.AddressDetails.Country(
                                                    scheme = country.scheme,
                                                    id = country.id,
                                                    description = country.description,
                                                    uri = country.uri
                                                )
                                            },
                                            region = addressDetails.region.let { region ->
                                                NegotiationCnOnPnResponse.Tender.ProcuringEntity.Address.AddressDetails.Region(
                                                    scheme = region.scheme,
                                                    id = region.id,
                                                    description = region.description,
                                                    uri = region.uri
                                                )
                                            },
                                            locality = addressDetails.locality.let { locality ->
                                                NegotiationCnOnPnResponse.Tender.ProcuringEntity.Address.AddressDetails.Locality(
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
                                NegotiationCnOnPnResponse.Tender.ProcuringEntity.ContactPoint(
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
                        NegotiationCnOnPnResponse.Tender.Value(
                            amount = value.amount,
                            currency = value.currency
                        )
                    },
                    lotGroups = tender.lotGroups.map { lotGroup ->
                        NegotiationCnOnPnResponse.Tender.LotGroup(
                            optionToCombine = lotGroup.optionToCombine
                        )
                    },
                    lots = tender.lots.map { lot ->
                        NegotiationCnOnPnResponse.Tender.Lot(
                            id = lot.id,
                            internalId = lot.internalId,
                            title = lot.title,
                            description = lot.description,
                            status = lot.status,
                            statusDetails = lot.statusDetails,
                            value = lot.value.let { value ->
                                NegotiationCnOnPnResponse.Tender.Lot.Value(
                                    amount = value.amount,
                                    currency = value.currency
                                )
                            },
                            options = lot.options.map { option ->
                                NegotiationCnOnPnResponse.Tender.Lot.Option(
                                    hasOptions = option.hasOptions
                                )
                            },
                            variants = lot.variants.map { variant ->
                                NegotiationCnOnPnResponse.Tender.Lot.Variant(
                                    hasVariants = variant.hasVariants
                                )
                            },
                            renewals = lot.renewals.map { renewal ->
                                NegotiationCnOnPnResponse.Tender.Lot.Renewal(
                                    hasRenewals = renewal.hasRenewals
                                )
                            },
                            recurrentProcurement = lot.recurrentProcurement.map { recurrentProcurement ->
                                NegotiationCnOnPnResponse.Tender.Lot.RecurrentProcurement(
                                    isRecurrent = recurrentProcurement.isRecurrent
                                )
                            },
                            contractPeriod = lot.contractPeriod.let { contractPeriod ->
                                NegotiationCnOnPnResponse.Tender.Lot.ContractPeriod(
                                    startDate = contractPeriod.startDate,
                                    endDate = contractPeriod.endDate
                                )
                            },
                            placeOfPerformance = lot.placeOfPerformance.let { placeOfPerformance ->
                                NegotiationCnOnPnResponse.Tender.Lot.PlaceOfPerformance(
                                    description = placeOfPerformance.description,
                                    address = placeOfPerformance.address.let { address ->
                                        NegotiationCnOnPnResponse.Tender.Lot.PlaceOfPerformance.Address(
                                            streetAddress = address.streetAddress,
                                            postalCode = address.postalCode,
                                            addressDetails = address.addressDetails.let { addressDetails ->
                                                NegotiationCnOnPnResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                                    country = addressDetails.country.let { country ->
                                                        NegotiationCnOnPnResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                                            scheme = country.scheme,
                                                            id = country.id,
                                                            description = country.description,
                                                            uri = country.uri
                                                        )
                                                    },
                                                    region = addressDetails.region.let { region ->
                                                        NegotiationCnOnPnResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                                            scheme = region.scheme,
                                                            id = region.id,
                                                            description = region.description,
                                                            uri = region.uri
                                                        )
                                                    },
                                                    locality = addressDetails.locality.let { locality ->
                                                        NegotiationCnOnPnResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
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
                        NegotiationCnOnPnResponse.Tender.Item(
                            id = item.id,
                            internalId = item.internalId,
                            classification = item.classification.let { classification ->
                                NegotiationCnOnPnResponse.Tender.Item.Classification(
                                    scheme = classification.scheme,
                                    id = classification.id,
                                    description = classification.description
                                )
                            },
                            additionalClassifications = item.additionalClassifications?.map { additionalClassification ->
                                NegotiationCnOnPnResponse.Tender.Item.AdditionalClassification(
                                    scheme = additionalClassification.scheme,
                                    id = additionalClassification.id,
                                    description = additionalClassification.description
                                )
                            },
                            quantity = item.quantity,
                            unit = item.unit.let { unit ->
                                NegotiationCnOnPnResponse.Tender.Item.Unit(
                                    id = unit.id,
                                    name = unit.name
                                )
                            },
                            description = item.description,
                            relatedLot = item.relatedLot
                        )
                    },
                    awardCriteria = tender.awardCriteria,
                    requiresElectronicCatalogue = tender.requiresElectronicCatalogue,
                    submissionMethod = tender.submissionMethod,
                    submissionMethodRationale = tender.submissionMethodRationale,
                    submissionMethodDetails = tender.submissionMethodDetails,
                    documents = tender.documents.map { document ->
                        NegotiationCnOnPnResponse.Tender.Document(
                            documentType = TenderDocumentType.creator(document.documentType.key),
                            id = document.id,
                            title = document.title,
                            description = document.description,
                            relatedLots = document.relatedLots
                        )
                    }
                )
            }
        )
    }
}
