package com.procurement.access.service

import com.procurement.access.application.service.cn.update.UpdateSelectiveCnContext
import com.procurement.access.application.service.cn.update.UpdateSelectiveCnData
import com.procurement.access.application.service.cn.update.UpdatedSelectiveCn
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.enums.*
import com.procurement.access.domain.model.isNotUniqueIds
import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.domain.model.money.Money
import com.procurement.access.domain.model.money.sum
import com.procurement.access.domain.model.persone.PersonId
import com.procurement.access.domain.model.uniqueIds
import com.procurement.access.domain.model.update
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.lib.extension.isUnique
import com.procurement.access.lib.extension.mapOrEmpty
import com.procurement.access.lib.extension.orThrow
import com.procurement.access.lib.extension.toSet
import com.procurement.access.lib.takeIfNotNullOrDefault
import com.procurement.access.model.entity.TenderProcessEntity

import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

interface SelectiveCNService {
    fun update(context: UpdateSelectiveCnContext, data: UpdateSelectiveCnData): UpdatedSelectiveCn
}

@Service
class SelectiveCNServiceImpl(
    private val tenderProcessDao: TenderProcessDao
) : SelectiveCNService {
    override fun update(context: UpdateSelectiveCnContext, data: UpdateSelectiveCnData): UpdatedSelectiveCn {
        data.checkLotsIds() //VR-1.0.1.4.1
            .checkUniqueIdsItems() // VR-1.0.1.5.1
            .checkIdsPersons() //VR-1.0.1.10.3
            .checkBusinessFunctions(context.startDate) //VR-1.0.1.10.5, VR-1.0.1.10.6, VR-1.0.1.10.7, VR-1.0.1.2.1, VR-1.0.1.2.8

        val entity = tenderProcessDao.getByCpIdAndStage(cpId = context.cpid, stage = context.stage)
            ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)

        if (entity.owner != context.owner) throw ErrorException(error = ErrorType.INVALID_OWNER)
        if (entity.token != context.token) throw ErrorException(error = ErrorType.INVALID_TOKEN)

        val cn = toObject(CNEntity::class.java, entity.jsonData)

        //VR-1.0.1.1.4
        cn.checkStatus()

        val receivedLotsByIds: Map<LotId, UpdateSelectiveCnData.Tender.Lot> = data.tender.lots.associateBy { it.id }
        val savedLotsByIds: Map<LotId, CNEntity.Tender.Lot> = cn.tender.lots.associateBy { LotId.fromString(it.id) }
        val allLotsIds: Set<LotId> = receivedLotsByIds.keys + savedLotsByIds.keys
        val documentsIds: Set<String> = cn.tender.documents.toSet { it.id }

        data.checkDocuments(documentsIds) //VR-1.0.1.2.1, VR-1.0.1.2.2, VR-1.0.1.2.9
            .checkLotsValue(budgetCurrency = cn.planning.budget.amount.currency) //VR-1.0.1.4.2
            .checkRelatedLotItems(allLotsIds) //VR-1.0.1.5.4
            .checkRelatedLotsOfDocuments(allLotsIds) //VR-1.0.1.2.6
            .checkIdsProcuringEntity(cn) //VR-1.0.1.10.1

        cn.checkTenderAmount(savedLotsByIds, receivedLotsByIds) //VR-1.0.1.3.2

        //BR-1.0.1.2.1
        val calculatedTenderContractPeriod = data.calculateTenderContractPeriod()
        cn.checkContractPeriod(calculatedTenderContractPeriod) //VR-1.0.1.6.1

        val receivedLotsIds = receivedLotsByIds.keys
        val savedLotsIds = savedLotsByIds.keys
        val idsUpdateLots = getElementsForUpdate(receivedLotsIds, savedLotsIds)
        val idsUnmodifiedLots = savedLotsIds - idsUpdateLots

        val receivedLotsByPermanentIds = data.tender.lots.associateBy { it.id }

        val updatedLots = idsUpdateLots.map { id ->
            updateLot(
                src = receivedLotsByPermanentIds.getValue(id),
                dst = savedLotsByIds.getValue(id)
            )
        }

        val unmodifiedLots = idsUnmodifiedLots.map { id ->
            savedLotsByIds.getValue(id)
        }

        val allModifiedLots = updatedLots.also {
            //VR-1.0.1.4.7
            if (!it.any { lot -> lot.status == LotStatus.ACTIVE })
                throw ErrorException(ErrorType.NO_ACTIVE_LOTS)

            //VR-1.0.1.4.8
            data.checkRelatedLotItemsPermanent(it)
        }

        val activeLotsFromDb = savedLotsByIds
            .filter { it.value.status == LotStatus.ACTIVE }

        // VR-1.0.1.4.10
        validateAllActiveLotsWereTransferred(activeLotsFromDb, receivedLotsByIds)

        val updatedItems = cn.updateItems(data)

        //BR-1.0.1.5.2
        val updatedTenderDocuments = cn.updateTenderDocuments(data.tender.documents)

        //BR-1.0.1.1.2
        val updatedValue = calculateTenderAmount(allModifiedLots)
            .let {
                CNEntity.Tender.Value(amount = it.amount, currency = it.currency)
            }

        //BR-1.0.1.15.3
        val updatedProcuringEntity = if (data.tender.procuringEntity != null)
            cn.tender.procuringEntity.update(data.tender.procuringEntity.persons)
        else
            cn.tender.procuringEntity

        val updatedCN = cn.copy(
            planning = cn.planning.copy(
                rationale = data.planning?.rationale ?: cn.planning.rationale,
                budget = cn.planning.budget.copy(
                    description = data.planning?.budget?.description
                        ?: cn.planning.budget.description
                )
            ),
            tender = cn.tender.copy(
                title = data.tender.title,
                description = data.tender.description,
                contractPeriod = calculatedTenderContractPeriod,
                procurementMethodRationale = data.tender.procurementMethodRationale
                    .takeIfNotNullOrDefault(
                        cn.tender.procurementMethodRationale
                    ),
                procurementMethodAdditionalInfo = data.tender.procurementMethodAdditionalInfo
                    .takeIfNotNullOrDefault(
                        cn.tender.procurementMethodAdditionalInfo
                    ),
                procurementMethodModalities = data.tender.procurementMethodModalities
                    ?.toSet()
                    .takeIfNotNullOrDefault(
                        cn.tender.procurementMethodModalities
                    ),
                procuringEntity = updatedProcuringEntity, //BR-1.0.1.15.3
                value = updatedValue, //BR-1.0.1.1.2
                lots = allModifiedLots + unmodifiedLots,
                items = updatedItems,
                documents = updatedTenderDocuments //BR-1.0.1.5.2
            )
        )

        tenderProcessDao.save(
            TenderProcessEntity(
                cpId = context.cpid,
                token = entity.token,
                stage = context.stage,
                owner = entity.owner,
                createdDate = context.startDate,
                jsonData = toJson(updatedCN)
            )
        )

        return getResponse(
            cn = updatedCN,
            data = data,
            lotsChanged = false
        )
    }

    /**
     * VR-1.0.1.4.10
     *
     * eAccess compares Lot objects  transferred in Request with Lot objects from DB && have lot.status = "active":
     * a. IF  [Lots.id in Request == (equal) Lots.id from DB], validation is successful;
     * b. else eAccess throws Exception: "All lots in status “active “ must be transferred”
     */
    private fun validateAllActiveLotsWereTransferred(
        activeLotsFromDb: Map<LotId, CNEntity.Tender.Lot>,
        receivedLotsByIds: Map<LotId, UpdateSelectiveCnData.Tender.Lot>
    ) {
        activeLotsFromDb.forEach { id, _ ->
            receivedLotsByIds[id] ?: throw ErrorException(
                error = ErrorType.INVALID_LOT,
                message = "All lots in status 'active' must be transferred. Lot with id='${id}' not transferred."
            )
        }
    }

    /**
     *  VR-1.0.1.1.4
     * eAccess analyzes tender.status value from DB:
     * a. IF tender.status in DB == "active", validation is successful;
     * b. ELSE (tender.status in DB != "active") eAccess throws Exception: "Contract Notice can not be updated";
     */
    private fun CNEntity.checkStatus(): CNEntity {
        if (this.tender.status != TenderStatus.ACTIVE)
            throw ErrorException(error = ErrorType.INVALID_TENDER_STATUS)

        return this
    }

    /**
     * VR-1.0.1.2.1 id (documents)
     * eAccess checks the uniqueness of all documents.ID from Request;
     * a. IF there is NO repeated value in list of documents.ID values from Request, validation is successful;
     * b. ELSE  eAccess throws Exception: "Invalid documents IDs";
     *
     * VR-1.0.1.2.2 documentType (documents)
     * eAccess checks documents.documentType values in all Documents object from Request;
     * IF document.documentType == oneOf tenderDocumentType value, validation is successful;
     * ELSE eAccess throws Exception: "Invalid document type";
     *
     * VR-1.0.1.2.9 documents
     * System checks the availability of Documents object in DB (in saved PN):
     * a. IF [there are NO Documents in DB] then: validation is successful;
     * b. ELSE[Documents are presented in saved tender] { then: system checks that all documents.ID from DB were transferred in Request:
     *   i.  IF [list of documents.ID from Request containsAll documents.ID from DB] then: validation is successful;
     *   ii. ELSE then: eAccess throws Exception: "All saved documents should be transferred";
     */
    private fun UpdateSelectiveCnData.checkDocuments(documentIds: Set<String>): UpdateSelectiveCnData {
        //VR-1.0.1.2.1
        val uniqueDocumentIds: Set<String> = this.tender.documents.uniqueIds()
            ?: throw ErrorException(
                error = ErrorType.INVALID_DOCS_ID,
                message = "The list documents of tender contain duplicates."
            )

        //VR-1.0.1.2.2
        this.tender.documents.forEach { document ->
            when (document.documentType) {
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
                DocumentType.CONTRACT_GUARANTEES -> Unit

                DocumentType.MARKET_STUDIES,
                DocumentType.HEARING_NOTICE,
                DocumentType.ENVIRONMENTAL_IMPACT,
                DocumentType.ASSET_AND_LIABILITY_ASSESSMENT,
                DocumentType.NEEDS_ASSESSMENT,
                DocumentType.FEASIBILITY_STUDY,
                DocumentType.PROJECT_PLAN -> throw ErrorException(
                    error = ErrorType.INVALID_DOCUMENT_TYPE,
                    message = "tender.documents[] contains not allowed document type"
                )
            }
        }

        //VR-1.0.1.2.9
        if (documentIds.isNotEmpty() && !uniqueDocumentIds.containsAll(documentIds)) {
            throw ErrorException(
                error = ErrorType.INVALID_DOCS_ID,
                message = "The list documents of tender from request not contain saved documents."
            )
        }
        return this
    }

    /**
     * VR-1.0.1.2.6 relatedLots (documents) (there are lots in DB)
     * 1. Gets all Lots object from processed CN in DB;
     * 2. Gets.Lot.ID values from Lots (got before) and saves them as a set of IDs in memory;
     * 3. Adds to set of lot IDs (got before) all lot.ID from Request;
     * 4. Analyzes the values of documents.relatedLots values in all Documents object from Request:
     *   a. IF set of documents.relatedLots values from Request can be included entirely in set of lot.ID values (got on step 3), validation is successful;
     *   b. ELSE eAccess throws Exception: "Undefined relatedLot value in Document";
     */
    private fun UpdateSelectiveCnData.checkRelatedLotsOfDocuments(allLotsIds: Set<LotId>): UpdateSelectiveCnData {
        this.tender.documents.forEach { document ->
            document.validation(allLotsIds) {
                throw ErrorException(
                    error = ErrorType.INVALID_DOCS_RELATED_LOTS,
                    message = "The document with id: ${document.id} contain invalid related lot: '$it'."
                )

            }
        }
        return this
    }

    /**
     * VR-1.0.1.3.2 amount (tender)
     */
    private fun CNEntity.checkTenderAmount(
        savedLotsByIds: Map<LotId, CNEntity.Tender.Lot>,
        receivedLotsByIds: Map<LotId, UpdateSelectiveCnData.Tender.Lot>
    ): CNEntity {
        val amount = calculateLotsAmount(savedLotsByIds, receivedLotsByIds)
        val tenderValue = this.tender.value
        if (tenderValue.currency != amount.currency)
            throw ErrorException(
                error = ErrorType.INVALID_CURRENCY,
                message = "The currency of tender not compatible with the currency of lots."
            )
        if (amount.amount > tenderValue.amount)
            throw ErrorException(
                error = ErrorType.INVALID_TENDER,
                message = "The amount of tender greater than the amount of lots."
            )
        return this
    }

    private fun calculateLotsAmount(
        savedLotsByIds: Map<LotId, CNEntity.Tender.Lot>,
        receivedLotsByIds: Map<LotId, UpdateSelectiveCnData.Tender.Lot>
    ): Money {
        val allLotsIds: Set<LotId> = receivedLotsByIds.keys + savedLotsByIds.keys

        return allLotsIds.asSequence()
            .filter { id ->
                savedLotsByIds[id]
                    ?.let {
                        it.status == LotStatus.ACTIVE
                    }
                    ?: true
            }
            .map { id ->
                savedLotsByIds[id]
                    ?.let { lot ->
                        val value = lot.value
                        Money(amount = value.amount, currency = value.currency)
                    }
                    ?: receivedLotsByIds.getValue(id).value
            }
            .sum {
                ErrorException(error = ErrorType.INVALID_LOT_CURRENCY)
            }
            .orThrow {
                throw ErrorException(error = ErrorType.INVALID_LOT_AMOUNT)
            }
    }

    private fun calculateTenderAmount(lots: Collection<CNEntity.Tender.Lot>): Money = lots.asSequence()
        .filter { lot ->
            lot.status == LotStatus.ACTIVE
        }
        .map { lot ->
            val value = lot.value
            Money(amount = value.amount, currency = value.currency)
        }
        .sum {
            ErrorException(error = ErrorType.INVALID_LOT_CURRENCY)
        }
        .orThrow {
            throw ErrorException(error = ErrorType.INVALID_LOT_AMOUNT)
        }

    /**
     * VR-1.0.1.4.8 Lot Item (lot)
     *
     * eAccess compares item.relatedLot values in all Items object from Request and lots.ID with lots.status == "active":
     * IF [Item.relatedLot from Request containsAll lot.ID from Lot with lots.status == "active"] validation is successful;
     * ELSE eAccess throws Exception: "Active lot should be connected to at least one item";
     */
    private fun UpdateSelectiveCnData.checkRelatedLotItemsPermanent(allModifiedLots: List<CNEntity.Tender.Lot>) {
        val allActiveLotsIds: Set<LotId> = allModifiedLots.asSequence()
            .filter { lot -> lot.status == LotStatus.ACTIVE }
            .map { lot -> LotId.fromString(lot.id) }
            .toSet()

        val itemsRelatedLots: Set<LotId> = this.tender.items
            .toSet { item ->
                item.relatedLot
            }

        if (allActiveLotsIds.any { it !in itemsRelatedLots })
            throw ErrorException(
                error = ErrorType.INVALID_LOT,
                message = "Active lot should be connected to at least one item."
            )
    }

    /**
     * VR-1.0.1.4.1 id (lot)
     * eAccess analyzes Lot.ID from Request:
     * a. IF every lot.ID from Request is included once in list from Request, validation is successful;
     * b. ELSE eAccess throws Exception: "Lot ID are repeated in list";
     */
    private fun UpdateSelectiveCnData.checkLotsIds(): UpdateSelectiveCnData {
        this.tender.lots.isNotUniqueIds {
            ErrorException(
                error = ErrorType.LOT_ID_DUPLICATED,
                message = "The list lots of tender contain duplicates."
            )
        }
        return this
    }

    /**
     * VR-1.0.1.4.2 currency (lot) (there are lots in DB)
     * FOR every Lot object from Request system compares tender.lot.value.currency value with budget.amount.currency from proceeded PN || CN in DB:
     * a. IF lot.value.currency value == (equal to) budget.amount.currency value, validation is successful;
     * b. ELSE eAccess throws Exception: "Lot contains currency that is different from announced currency in budget";
     */
    private fun UpdateSelectiveCnData.checkLotsValue(budgetCurrency: String): UpdateSelectiveCnData {
        this.tender.lots.forEach { lot ->
            if (lot.value.amount.compareTo(BigDecimal.ZERO) == 0)
                throw ErrorException(
                    error = ErrorType.INVALID_LOT_AMOUNT,
                    message = "The lot with id: ${lot.id} contain invalid amount."
                )
            if (lot.value.currency != budgetCurrency)
                throw ErrorException(
                    error = ErrorType.INVALID_LOT_CURRENCY,
                    message = "The lot with id: ${lot.id} contain currency which not equals budget currency."
                )
        }

        return this
    }

    /**
     * VR-1.0.1.5.1 id (item)
     * eAccess analyzes item.ID from Request:
     *   a. IF every item.ID from Request is included once in list from Request, validation is successful;
     *   b. ELSE eAccess throws Exception: "Item ID are repeated in list";
     */
    private fun UpdateSelectiveCnData.checkUniqueIdsItems(): UpdateSelectiveCnData {
        this.tender.items.isNotUniqueIds {
            throw ErrorException(
                error = ErrorType.ITEM_ID_DUPLICATED,
                message = "The list items of tender contain duplicates."
            )
        }
        return this
    }

    /**
     * VR-1.0.1.5.4 relatedLot (item) (there are lots in DB)
     * 1. Gets all Lots object from proceeded CN in DB;
     * 2. Gets.Lot.ID values from Lots (got before) and saves them as a set of IDs in memory;
     * 3. Adds to set of lot IDs (got before) all lot.ID from Request;
     * 4. eAccess analyzes the values of item.relatedLot values in all Items object from Request
     *    (all items should be connected to some lots from DB && every lot from Request should be connected
     *    at least to one item):
     *   a. IF set of item.relatedLot values from Request containsAll values from set of lot.ID from Request
     *      && set of lot.ID (got on step 3) containsAll values from set of  item.relatedLot from Request,
     *      validation is successful;
     *   b. ELSE eAccess throws Exception: "Incorrect relatedLot value in Item";
     */

    private fun UpdateSelectiveCnData.checkRelatedLotItems(allLotsIds: Set<LotId>): UpdateSelectiveCnData {
        this.tender.items.forEach { item ->
            item.validation(allLotsIds) { relatedLot ->
                throw ErrorException(
                    error = ErrorType.INVALID_RELATED_LOT,
                    message = "Unknown relatedLot '$relatedLot' in item '${item.id}'."
                )
            }
        }
        return this
    }

    /**
     * VR-1.0.1.6.1
     * 1. Gets all budgetBreakdown objects from planning object and executes next:
     *   a. Gets.budgetBreakdown.period.endDate values from all budgetBreakdown objects (got before) and saves them as a set of in memory;
     *   b. Gets.budgetBreakdown.period.startDate values from all budgetBreakdown objects (got on step 1) and saves them as a set of in memory;
     * 2. Calculates tender.contractPeriod object according to the following order:
     *   a. Get.lot.contractPeriod.startDate values from all Lots objects of Request and saves them as a list to memory;
     *   b. Sets tender.contractPeriod.startDate == (earliest) MIN value from list of  lot.contractPeriod.startDate values (got before);
     *   c. Get.lot.contractPeriod.endDate values from all Lots objects of Request and saves them as a list to memory;
     *   d. Sets tender.contractPeriod.endDate == (latest) MAX value from list of  lot.contractPeriod.startDate values (got before);
     * 3. Compares all values budgetBreakdown.period.endDate && budgetBreakdown.period.startDate from lists with tender.contractPeriod:
     *   a. IF all values budgetBreakdown.period.endDate >= (more || equal to) tender.contractPeriod.startDate && budgetBreakdown.period.startDate <= (less || equal to) tender.contractPeriod.endDate, validation is successful;
     *   b. ELSE eAccess throws Exception: "Invalid date-time values in tender contract period";
     */
    private fun CNEntity.checkContractPeriod(calculatedTenderPeriod: CNEntity.Tender.ContractPeriod): CNEntity {
        this.planning.budget.budgetBreakdowns.forEach { budgetBreakdown ->
            if (calculatedTenderPeriod.isInvalid(budgetBreakdown))
                throw ErrorException(
                    error = ErrorType.INVALID_TENDER,
                    message = "The calculated contract period of tender is invalid."
                )
        }
        return this
    }

    private fun CNEntity.Tender.ContractPeriod.isInvalid(budgetBreakdown: CNEntity.Planning.Budget.BudgetBreakdown): Boolean =
        budgetBreakdown.period.endDate < this.startDate || budgetBreakdown.period.startDate > this.endDate

    private fun UpdateSelectiveCnData.calculateTenderContractPeriod(): CNEntity.Tender.ContractPeriod {
        val iterator = this.tender.lots.iterator()
        val first = iterator.next()
        var minStartDate: LocalDateTime = first.contractPeriod.startDate
        var maxEndDate: LocalDateTime = first.contractPeriod.endDate
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next.contractPeriod.startDate < minStartDate)
                minStartDate = next.contractPeriod.startDate
            if (next.contractPeriod.endDate > maxEndDate)
                maxEndDate = next.contractPeriod.endDate
        }

        return CNEntity.Tender.ContractPeriod(startDate = minStartDate, endDate = maxEndDate)
    }

    /**
     * VR-1.0.1.10.1 id (procuringEntity)
     *
     * eAccess compares procuringEntity.ID related to saved PN || CN from DB and procuringEntity.ID from Request:
     * a. IF [procuringEntity.ID value in DB ==  (equal to) procuringEntity.ID from Request] then: validation is successful;
     * b. ELSE eAccess throws Exception: "Invalid identifier of procuring entity";
     */
    private fun UpdateSelectiveCnData.checkIdsProcuringEntity(cn: CNEntity): UpdateSelectiveCnData {
        if (this.tender.procuringEntity != null && this.tender.procuringEntity.id != cn.tender.procuringEntity.id)
            throw ErrorException(
                error = ErrorType.INVALID_PROCURING_ENTITY,
                message = "The id of procuring entity not equals saved id of procuring entity."
            )

        return this
    }

    /**
     * VR-1.0.1.10.3 id (persones.identifier)
     * eAccess checks the uniqueness of all Persones.identifier.ID values from every object of Persones array of Request:
     * a. IF [there is NO repeated values of identifier.ID in Request] then validation is successful;
     * b. ELSE eAccess throws Exception: "Persones objects should be unique in Request";
     */
    private fun UpdateSelectiveCnData.checkIdsPersons(): UpdateSelectiveCnData {
        val isUnique = this.tender.procuringEntity?.persons?.isUnique {
            Pair(it.identifier.scheme, it.identifier.id)
        } ?: true

        if (!isUnique)
            throw ErrorException(
                error = ErrorType.INVALID_PERSON,
                message = "The list person contain duplicates."
            )
        return this
    }

    /**
     * VR-1.0.1.10.5 id (businessFunctions)
     * eAccess checks the uniqueness of all Persones.businessFunctions.ID values from every object of businessFunctions array of Request:
     * a. IF [there is NO repeated values of businessFunctions.ID] then validation is successful;
     * b. ELSE eAccess throws Exception: "businessFunctions objects should be unique in every Person from Request";
     *
     * VR-1.0.1.10.8 (1)
     * 1. eAccess checks persones.businessFunctions.type values in all businessFuctions object from Request;
     *   a. IF businessFunctions.type == oneOf procuringEntityBusinessFuncTypeEnum value (link), validation is successful;
     *   b. ELSE  eAccess throws Exception: "Invalid business functions type";
     *
     * VR-1.0.1.10.7 startDate (businessFunctions.period)
     * eAccess compares businessFunctions.period.startDate and startDate from the context of Request:
     * a. IF [businessFunctions.period.startDate <= (less || equal to) startDate from Request] then: validation is successful;
     * b. ELSE eAccess throws Exception: "Invalid period in bussiness function specification";
     *
     * VR-1.0.1.2.1
     * VR-1.0.1.2.8
     */
    private fun UpdateSelectiveCnData.checkBusinessFunctions(startDate: LocalDateTime): UpdateSelectiveCnData {
        val uniqueBusinessFunctionId = mutableSetOf<String>()
        var uniqueDocumentId: Set<String> = emptySet()
        this.tender.procuringEntity?.persons
            ?.asSequence()
            ?.flatMap { person ->
                person.businessFunctions.asSequence()
            }
            ?.forEach { businessFunction ->
                if (!uniqueBusinessFunctionId.add(businessFunction.id))
                    throw ErrorException(
                        error = ErrorType.INVALID_BUSINESS_FUNCTION,
                        message = "Ids of business function are not unique."
                    )

                //VR-1.0.1.10.8 (1)
                when (businessFunction.type) {
                    BusinessFunctionType.CHAIRMAN,
                    BusinessFunctionType.PROCURMENT_OFFICER,
                    BusinessFunctionType.CONTACT_POINT,
                    BusinessFunctionType.TECHNICAL_EVALUATOR,
                    BusinessFunctionType.TECHNICAL_OPENER,
                    BusinessFunctionType.PRICE_OPENER,
                    BusinessFunctionType.PRICE_EVALUATOR -> Unit
                    BusinessFunctionType.AUTHORITY -> throw ErrorException(
                        error = ErrorType.INVALID_BUSINESS_FUNCTION,
                        message = "Type '${BusinessFunctionType.AUTHORITY}' was deprecated. Use '${BusinessFunctionType.CHAIRMAN}' instead of it"
                    )
                }

                if (businessFunction.period.startDate > startDate)
                    throw ErrorException(
                        error = ErrorType.INVALID_BUSINESS_FUNCTION,
                        message = "Start date of business function more than start date from request."
                    )

                uniqueDocumentId = businessFunction.documents.isNotUniqueIds(uniqueDocumentId) {
                    throw ErrorException(
                        error = ErrorType.INVALID_BUSINESS_FUNCTION,
                        message = "Ids of documents in business function are not unique."
                    )
                }

                businessFunction.checkDocuments()
            }

        return this
    }

    /**
     *
     *
     * VR-1.0.1.2.8
     * eAccess checks documents.documentType values in all Documents object from Request;
     * IF document.documentType == oneOf BussinesFunctionsDocumentType value, validation is successful;
     * ELSE eAccess throws Exception: "Invalid document type";
     */
    private fun UpdateSelectiveCnData.Tender.ProcuringEntity.Person.BusinessFunction.checkDocuments() {
        this.documents.forEach { document ->
            when (document.documentType) {
                BusinessFunctionDocumentType.REGULATORY_DOCUMENT -> Unit
            }
        }
    }

    private fun updateLot(src: UpdateSelectiveCnData.Tender.Lot, dst: CNEntity.Tender.Lot): CNEntity.Tender.Lot =
        dst.copy(
            internalId = src.internalId.takeIfNotNullOrDefault(dst.internalId),
            title = src.title,
            description = src.description,
            placeOfPerformance = dst.placeOfPerformance.copy(
                description = src.placeOfPerformance.description,
                address = dst.placeOfPerformance.address.copy(
                    streetAddress = src.placeOfPerformance.address.streetAddress,
                    postalCode = src.placeOfPerformance.address.postalCode,
                    addressDetails = dst.placeOfPerformance.address.addressDetails.copy(
                        country = dst.placeOfPerformance.address.addressDetails.country.copy(
                            scheme = src.placeOfPerformance.address.addressDetails.country.scheme,
                            id = src.placeOfPerformance.address.addressDetails.country.id,
                            description = src.placeOfPerformance.address.addressDetails.country.description,
                            uri = src.placeOfPerformance.address.addressDetails.country.uri
                        ),
                        region = dst.placeOfPerformance.address.addressDetails.region.copy(
                            scheme = src.placeOfPerformance.address.addressDetails.region.scheme,
                            id = src.placeOfPerformance.address.addressDetails.region.id,
                            description = src.placeOfPerformance.address.addressDetails.region.description,
                            uri = src.placeOfPerformance.address.addressDetails.region.uri
                        ),
                        locality = dst.placeOfPerformance.address.addressDetails.locality.copy(
                            scheme = src.placeOfPerformance.address.addressDetails.locality.scheme,
                            id = src.placeOfPerformance.address.addressDetails.locality.id,
                            description = src.placeOfPerformance.address.addressDetails.locality.description,
                            uri = src.placeOfPerformance.address.addressDetails.locality.uri
                        )
                    )
                )
            ),
            contractPeriod = dst.contractPeriod.copy(
                startDate = src.contractPeriod.startDate,
                endDate = src.contractPeriod.endDate
            )
        )

    fun CNEntity.updateItems(data: UpdateSelectiveCnData): List<CNEntity.Tender.Item> =
        this.tender.items.update(sources = data.tender.items) { dst, src ->
            dst.copy(
                description = src.description,
                relatedLot = src.relatedLot.toString(),
                internalId = src.internalId.takeIfNotNullOrDefault(dst.internalId)
            )
        }

    private fun CNEntity.Tender.ProcuringEntity.update(
        persons: List<UpdateSelectiveCnData.Tender.ProcuringEntity.Person>
    ): CNEntity.Tender.ProcuringEntity {
        val receivedPersonsById = persons.associateBy { it.identifier.id }
        val savedPersonsById = this.persones?.associateBy { it.identifier.id } ?: emptyMap()

        val receivedPersonsIds = receivedPersonsById.keys
        val savedPersonsIds = savedPersonsById.keys

        val idsAllPersons = receivedPersonsIds.union(savedPersonsIds)
        val idsNewPersons = getNewElements(receivedPersonsIds, savedPersonsIds)
        val idsUpdatePersons = getElementsForUpdate(receivedPersonsIds, savedPersonsIds)
        val idsOldPersons = getElementsForRemove(receivedPersonsIds, savedPersonsIds)

        val updatedPersons = idsAllPersons.asSequence()
            .map { id ->
                when (id) {
                    in idsNewPersons -> createPerson(receivedPersonsById.getValue(id))
                    in idsUpdatePersons -> savedPersonsById.getValue(id).update(receivedPersonsById.getValue(id))
                    in idsOldPersons -> savedPersonsById.getValue(id)
                    else -> throw IllegalStateException()
                }
            }
            .toList()

        return this.copy(persones = updatedPersons)
    }

    private fun createPerson(
        person: UpdateSelectiveCnData.Tender.ProcuringEntity.Person
    ) = CNEntity.Tender.ProcuringEntity.Persone(
        id = PersonId.generate(
            scheme = person.identifier.scheme,
            id = person.identifier.id
        ),
        title = person.title,
        name = person.name,
        identifier = CNEntity.Tender.ProcuringEntity.Persone.Identifier(
            scheme = person.identifier.scheme,
            id = person.identifier.id,
            uri = person.identifier.uri
        ),
        businessFunctions = person.businessFunctions.map { businessFunction ->
            CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction(
                id = businessFunction.id,
                jobTitle = businessFunction.jobTitle,
                type = businessFunction.type,
                period = CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Period(
                    startDate = businessFunction.period.startDate
                ),
                documents = businessFunction.documents.map { document ->
                    CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Document(
                        id = document.id,
                        documentType = document.documentType,
                        title = document.title,
                        description = document.description
                    )
                }
            )
        }
    )

    private fun CNEntity.Tender.ProcuringEntity.Persone.update(
        person: UpdateSelectiveCnData.Tender.ProcuringEntity.Person
    ) = this.copy(
        title = person.title,
        name = person.name,
        businessFunctions = updateBusinessFunctions(person.businessFunctions, this.businessFunctions)
    )

    /**
     * BR-1.0.1.15.4
     */
    private fun updateBusinessFunctions(
        receivedBusinessFunctions: List<UpdateSelectiveCnData.Tender.ProcuringEntity.Person.BusinessFunction>,
        savedBusinessFunctions: List<CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction>
    ): List<CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction> {
        val receivedBusinessFunctionsByIds = receivedBusinessFunctions.associateBy { it.id }
        val savedBusinessFunctionsByIds = savedBusinessFunctions.associateBy { it.id }

        val receivedBusinessFunctionsIds = receivedBusinessFunctionsByIds.keys
        val savedBusinessFunctionsIds = savedBusinessFunctionsByIds.keys

        val idsAllPersons = receivedBusinessFunctionsIds.union(savedBusinessFunctionsIds)
        val idsNewBusinessFunctions = getNewElements(receivedBusinessFunctionsIds, savedBusinessFunctionsIds)
        val idsUpdateBusinessFunctions = getElementsForUpdate(receivedBusinessFunctionsIds, savedBusinessFunctionsIds)

        return idsAllPersons.asSequence()
            .map { id ->
                when (id) {
                    in idsNewBusinessFunctions -> createBusinessFunction(receivedBusinessFunctionsByIds.getValue(id))
                    in idsUpdateBusinessFunctions ->
                        savedBusinessFunctionsByIds.getValue(id)
                            .update(receivedBusinessFunctionsByIds.getValue(id))
                    else -> savedBusinessFunctionsByIds.getValue(id)
                }
            }
            .toList()
    }

    private fun createBusinessFunction(
        businessFunction: UpdateSelectiveCnData.Tender.ProcuringEntity.Person.BusinessFunction
    ) = CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction(
        id = businessFunction.id,
        type = businessFunction.type,
        jobTitle = businessFunction.jobTitle,
        period = businessFunction.period.let { period ->
            CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Period(
                startDate = period.startDate
            )
        },
        documents = businessFunction.documents.map { document ->
            CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Document(
                documentType = document.documentType,
                id = document.id,
                title = document.title,
                description = document.description
            )
        }
    )

    private fun CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.update(
        businessFunction: UpdateSelectiveCnData.Tender.ProcuringEntity.Person.BusinessFunction
    ) = this.copy(
        type = businessFunction.type,
        jobTitle = businessFunction.jobTitle,
        period = this.period.copy(startDate = businessFunction.period.startDate),
        documents = updateBusinessFunctionDocuments(businessFunction.documents, this.documents ?: emptyList())
    )

    /**
     * BR-1.0.1.5.1
     */
    private fun updateBusinessFunctionDocuments(
        receivedDocuments: List<UpdateSelectiveCnData.Tender.ProcuringEntity.Person.BusinessFunction.Document>,
        savedDocuments: List<CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Document>
    ): List<CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Document> {
        val receivedDocumentsByIds = receivedDocuments.associateBy { it.id }
        val savedDocumentsByIds = savedDocuments.associateBy { it.id }

        val receivedBusinessFunctionsIds = receivedDocumentsByIds.keys
        val savedBusinessFunctionsIds = savedDocumentsByIds.keys

        val idsAllPersons = receivedBusinessFunctionsIds.union(savedBusinessFunctionsIds)
        val idsNewBusinessFunctions = getNewElements(receivedBusinessFunctionsIds, savedBusinessFunctionsIds)
        val idsUpdateBusinessFunctions = getElementsForUpdate(receivedBusinessFunctionsIds, savedBusinessFunctionsIds)

        return idsAllPersons.asSequence()
            .map { id ->
                when (id) {
                    in idsNewBusinessFunctions -> createBusinessFunctionDocument(receivedDocumentsByIds.getValue(id))
                    in idsUpdateBusinessFunctions ->
                        savedDocumentsByIds.getValue(id)
                            .update(receivedDocumentsByIds.getValue(id))
                    else -> savedDocumentsByIds.getValue(id)
                }
            }
            .toList()
    }

    private fun createBusinessFunctionDocument(
        document: UpdateSelectiveCnData.Tender.ProcuringEntity.Person.BusinessFunction.Document
    ) = CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Document(
        documentType = document.documentType,
        id = document.id,
        title = document.title,
        description = document.description
    )

    private fun CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Document.update(
        document: UpdateSelectiveCnData.Tender.ProcuringEntity.Person.BusinessFunction.Document
    ) = this.copy(
        title = document.title,
        description = document.description.takeIfNotNullOrDefault(this.description)
    )

    /**
     * BR-1.0.1.5.2 documents (tender)
     *
     * a. IF there are NO Documents in DB, system executes next operations:
     *   i. Saves Documents objects from Request to object in DB;
     * b. ELSE (Documents are presented in saved object), system executes next operations:
     *   i. Compares set of Documents.[ID] values from Request and set of Documents.[ID] from DB;
     *     1. IF there are new Documents.ID in Request (that are not presented in DB), system executes next operations:
     *       a. Determines all new documents objects from Request after comparison;
     *       b. Adds Documents objects (determined on step b.i.1.a) in DB;
     *       c. Determines all documents objects from Request that are presented in Documents object of saved object in DB;
     *       d. Updates saved versions of Documents (determined on step b.i.1.c) getting the values from next fields of Documents objects from Request:
     *         i.   Document.title;
     *         ii.  Document.description;
     *         iii. Document.relaredLots;
     *     2. ELSE (there are NO new Documents.ID in Request), system executes next operations:
     *       a. Updates saved versions of Documents getting the values from next fields of Documents objects from Request:
     *         i.   Document.title;
     *         ii.  Document.description;
     *         iii. Document.relaredLots;
     */
    private fun CNEntity.updateTenderDocuments(documentsFromRequest: List<UpdateSelectiveCnData.Tender.Document>): List<CNEntity.Tender.Document> {
        val documentsById = this.tender.documents.associateBy { it.id }
        val documentsFromRequestById = documentsFromRequest.associateBy { it.id }

        val allDocumentsIds = documentsById.keys + documentsFromRequestById.keys
        return allDocumentsIds.map { id ->
            documentsFromRequestById[id]
                ?.let { document ->
                    documentsById[id]
                        ?.copy(
                            title = document.title,
                            description = document.description,
                            relatedLots = document.relatedLots.map { it.toString() }
                        )
                        ?: CNEntity.Tender.Document(
                            id = document.id,
                            documentType = DocumentType.creator(document.documentType.key),
                            title = document.title,
                            description = document.description,
                            relatedLots = document.relatedLots.map { it.toString() }
                        )
                }
                ?: documentsById.getValue(id)
        }
    }

    private fun <T> getNewElements(received: Set<T>, saved: Set<T>) = received.subtract(saved)

    private fun <T> getElementsForUpdate(received: Set<T>, saved: Set<T>) = saved.intersect(received)

    private fun <T> getElementsForRemove(received: Set<T>, saved: Set<T>) = saved.subtract(received)

    private fun getResponse(
        cn: CNEntity,
        data: UpdateSelectiveCnData,
        lotsChanged: Boolean
    ) = UpdatedSelectiveCn(
        lotsChanged = lotsChanged,
        planning = cn.planning
            .let { planning ->
                UpdatedSelectiveCn.Planning(
                    rationale = planning.rationale,
                    budget = planning.budget
                        .let { budget ->
                            UpdatedSelectiveCn.Planning.Budget(
                                description = budget.description,
                                amount = budget.amount
                                    .let { amount ->
                                        Money(
                                            amount = amount.amount,
                                            currency = amount.currency
                                        )
                                    },
                                isEuropeanUnionFunded = budget.isEuropeanUnionFunded,
                                budgetBreakdowns = budget.budgetBreakdowns
                                    .map { budgetBreakdown ->
                                        UpdatedSelectiveCn.Planning.Budget.BudgetBreakdown(
                                            id = budgetBreakdown.id,
                                            description = budgetBreakdown.description,
                                            amount = budgetBreakdown.amount
                                                .let { amount ->
                                                    Money(
                                                        amount = amount.amount,
                                                        currency = amount.currency
                                                    )
                                                },
                                            period = budgetBreakdown.period
                                                .let { period ->
                                                    UpdatedSelectiveCn.Planning.Budget.BudgetBreakdown.Period(
                                                        startDate = period.startDate,
                                                        endDate = period.endDate
                                                    )
                                                },
                                            sourceParty = budgetBreakdown.sourceParty
                                                .let { sourceParty ->
                                                    UpdatedSelectiveCn.Planning.Budget.BudgetBreakdown.SourceParty(
                                                        id = sourceParty.id,
                                                        name = sourceParty.name
                                                    )
                                                },
                                            europeanUnionFunding = budgetBreakdown.europeanUnionFunding
                                                ?.let { europeanUnionFunding ->
                                                    UpdatedSelectiveCn.Planning.Budget.BudgetBreakdown.EuropeanUnionFunding(
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
        tender = cn.tender
            .let { tender ->
                UpdatedSelectiveCn.Tender(
                    id = tender.id,
                    status = tender.status,
                    statusDetails = tender.statusDetails,
                    title = tender.title,
                    description = tender.description,
                    classification = tender.classification
                        .let { classification ->
                            UpdatedSelectiveCn.Tender.Classification(
                                scheme = classification.scheme,
                                id = classification.id,
                                description = classification.description
                            )
                        },
                    requiresElectronicCatalogue = tender.requiresElectronicCatalogue,
                    acceleratedProcedure = tender.acceleratedProcedure
                        .let { acceleratedProcedure ->
                            UpdatedSelectiveCn.Tender.AcceleratedProcedure(
                                isAcceleratedProcedure = acceleratedProcedure.isAcceleratedProcedure
                            )
                        },
                    designContest = tender.designContest
                        .let { designContest ->
                            UpdatedSelectiveCn.Tender.DesignContest(
                                serviceContractAward = designContest.serviceContractAward
                            )
                        },
                    electronicWorkflows = tender.electronicWorkflows
                        .let { electronicWorkflows ->
                            UpdatedSelectiveCn.Tender.ElectronicWorkflows(
                                useOrdering = electronicWorkflows.useOrdering,
                                usePayment = electronicWorkflows.usePayment,
                                acceptInvoicing = electronicWorkflows.acceptInvoicing
                            )
                        },
                    jointProcurement = tender.jointProcurement
                        .let { jointProcurement ->
                            UpdatedSelectiveCn.Tender.JointProcurement(
                                isJointProcurement = jointProcurement.isJointProcurement
                            )
                        },
                    procedureOutsourcing = tender.procedureOutsourcing
                        .let { procedureOutsourcing ->
                            UpdatedSelectiveCn.Tender.ProcedureOutsourcing(
                                procedureOutsourced = procedureOutsourcing.procedureOutsourced
                            )
                        },
                    framework = tender.framework
                        .let { framework ->
                            UpdatedSelectiveCn.Tender.Framework(
                                isAFramework = framework.isAFramework
                            )
                        },
                    dynamicPurchasingSystem = tender.dynamicPurchasingSystem
                        .let { dynamicPurchasingSystem ->
                            UpdatedSelectiveCn.Tender.DynamicPurchasingSystem(
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
                    contractPeriod = tender.contractPeriod!!
                        .let { contractPeriod ->
                            UpdatedSelectiveCn.Tender.ContractPeriod(
                                startDate = contractPeriod.startDate,
                                endDate = contractPeriod.endDate
                            )
                        },
                    procurementMethodModalities = tender.procurementMethodModalities
                        ?.toList() ?: emptyList(),
                    electronicAuctions = data.tender.electronicAuctions
                        ?.let { electronicAuctions ->
                            UpdatedSelectiveCn.Tender.ElectronicAuctions(
                                details = electronicAuctions.details
                                    .map { detail ->
                                        UpdatedSelectiveCn.Tender.ElectronicAuctions.Detail(
                                            id = detail.id,
                                            relatedLot = detail.relatedLot,
                                            electronicAuctionModalities = detail.electronicAuctionModalities
                                                .map { modality ->
                                                    UpdatedSelectiveCn.Tender.ElectronicAuctions.Detail.ElectronicAuctionModality(
                                                        eligibleMinimumDifference = modality.eligibleMinimumDifference
                                                            .let { emd ->
                                                                Money(
                                                                    amount = emd.amount,
                                                                    currency = emd.currency
                                                                )
                                                            }
                                                    )
                                                }
                                        )
                                    }
                            )
                        },
                    procuringEntity = tender.procuringEntity
                        .let { procuringEntity ->
                            UpdatedSelectiveCn.Tender.ProcuringEntity(
                                id = procuringEntity.id,
                                name = procuringEntity.name,
                                identifier = procuringEntity.identifier
                                    .let { identifier ->
                                        UpdatedSelectiveCn.Tender.ProcuringEntity.Identifier(
                                            scheme = identifier.scheme,
                                            id = identifier.id,
                                            legalName = identifier.legalName,
                                            uri = identifier.uri
                                        )
                                    },
                                additionalIdentifiers = procuringEntity.additionalIdentifiers
                                    .mapOrEmpty { additionalIdentifier ->
                                        UpdatedSelectiveCn.Tender.ProcuringEntity.AdditionalIdentifier(
                                            scheme = additionalIdentifier.scheme,
                                            id = additionalIdentifier.id,
                                            legalName = additionalIdentifier.legalName,
                                            uri = additionalIdentifier.uri
                                        )
                                    },
                                address = procuringEntity.address
                                    .let { address ->
                                        UpdatedSelectiveCn.Tender.ProcuringEntity.Address(
                                            streetAddress = address.streetAddress,
                                            postalCode = address.postalCode,
                                            addressDetails = address.addressDetails
                                                .let { addressDetails ->
                                                    UpdatedSelectiveCn.Tender.ProcuringEntity.Address.AddressDetails(
                                                        country = addressDetails.country
                                                            .let { country ->
                                                                UpdatedSelectiveCn.Tender.ProcuringEntity.Address.AddressDetails.Country(
                                                                    scheme = country.scheme,
                                                                    id = country.id,
                                                                    description = country.description,
                                                                    uri = country.uri
                                                                )
                                                            },
                                                        region = addressDetails.region
                                                            .let { region ->
                                                                UpdatedSelectiveCn.Tender.ProcuringEntity.Address.AddressDetails.Region(
                                                                    scheme = region.scheme,
                                                                    id = region.id,
                                                                    description = region.description,
                                                                    uri = region.uri
                                                                )
                                                            },
                                                        locality = addressDetails.locality
                                                            .let { locality ->
                                                                UpdatedSelectiveCn.Tender.ProcuringEntity.Address.AddressDetails.Locality(
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
                                contactPoint = procuringEntity.contactPoint
                                    .let { contactPoint ->
                                        UpdatedSelectiveCn.Tender.ProcuringEntity.ContactPoint(
                                            name = contactPoint.name,
                                            email = contactPoint.email,
                                            telephone = contactPoint.telephone,
                                            faxNumber = contactPoint.faxNumber,
                                            url = contactPoint.url
                                        )
                                    },
                                persons = procuringEntity.persones
                                    .mapOrEmpty { person ->
                                        UpdatedSelectiveCn.Tender.ProcuringEntity.Person(
                                            id = person.id,
                                            name = person.name,
                                            title = person.title,
                                            identifier = UpdatedSelectiveCn.Tender.ProcuringEntity.Person.Identifier(
                                                id = person.identifier.id,
                                                scheme = person.identifier.scheme,
                                                uri = person.identifier.uri
                                            ),
                                            businessFunctions = person.businessFunctions
                                                .map { businessFunction ->
                                                    UpdatedSelectiveCn.Tender.ProcuringEntity.Person.BusinessFunction(
                                                        id = businessFunction.id,
                                                        type = businessFunction.type,
                                                        jobTitle = businessFunction.jobTitle,
                                                        period = UpdatedSelectiveCn.Tender.ProcuringEntity.Person.BusinessFunction.Period(
                                                            startDate = businessFunction.period.startDate
                                                        ),
                                                        documents = businessFunction.documents
                                                            .mapOrEmpty { document ->
                                                                UpdatedSelectiveCn.Tender.ProcuringEntity.Person.BusinessFunction.Document(
                                                                    id = document.id,
                                                                    documentType = document.documentType,
                                                                    title = document.title,
                                                                    description = document.description
                                                                )
                                                            }

                                                    )
                                                }
                                        )
                                    }
                            )
                        },
                    value = tender.value
                        .let { value ->
                            Money(
                                amount = value.amount,
                                currency = value.currency
                            )
                        },
                    lotGroups = tender.lotGroups
                        .map { lotGroup ->
                            UpdatedSelectiveCn.Tender.LotGroup(
                                optionToCombine = lotGroup.optionToCombine
                            )
                        },
                    lots = tender.lots
                        .map { lot ->
                            UpdatedSelectiveCn.Tender.Lot(
                                id = LotId.fromString(lot.id),
                                internalId = lot.internalId,
                                title = lot.title,
                                description = lot.description,
                                status = lot.status,
                                statusDetails = lot.statusDetails,
                                value = lot.value
                                    .let { value ->
                                        Money(
                                            amount = value.amount,
                                            currency = value.currency
                                        )
                                    },
                                options = lot.options
                                    .map { option ->
                                        UpdatedSelectiveCn.Tender.Lot.Option(
                                            hasOptions = option.hasOptions
                                        )
                                    },
                                variants = lot.variants
                                    .map { variant ->
                                        UpdatedSelectiveCn.Tender.Lot.Variant(
                                            hasVariants = variant.hasVariants
                                        )
                                    },
                                renewals = lot.renewals
                                    .map { renewal ->
                                        UpdatedSelectiveCn.Tender.Lot.Renewal(
                                            hasRenewals = renewal.hasRenewals
                                        )
                                    },
                                recurrentProcurements = lot.recurrentProcurement
                                    .map { recurrentProcurement ->
                                        UpdatedSelectiveCn.Tender.Lot.RecurrentProcurement(
                                            isRecurrent = recurrentProcurement.isRecurrent
                                        )
                                    },
                                contractPeriod = lot.contractPeriod
                                    .let { contractPeriod ->
                                        UpdatedSelectiveCn.Tender.Lot.ContractPeriod(
                                            startDate = contractPeriod.startDate,
                                            endDate = contractPeriod.endDate
                                        )
                                    },
                                placeOfPerformance = lot.placeOfPerformance
                                    .let { placeOfPerformance ->
                                        UpdatedSelectiveCn.Tender.Lot.PlaceOfPerformance(
                                            description = placeOfPerformance.description,
                                            address = placeOfPerformance.address
                                                .let { address ->
                                                    UpdatedSelectiveCn.Tender.Lot.PlaceOfPerformance.Address(
                                                        streetAddress = address.streetAddress,
                                                        postalCode = address.postalCode,
                                                        addressDetails = address.addressDetails
                                                            .let { addressDetails ->
                                                                UpdatedSelectiveCn.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                                                    country = addressDetails.country
                                                                        .let { country ->
                                                                            UpdatedSelectiveCn.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                                                                scheme = country.scheme,
                                                                                id = country.id,
                                                                                description = country.description,
                                                                                uri = country.uri
                                                                            )
                                                                        },
                                                                    region = addressDetails.region
                                                                        .let { region ->
                                                                            UpdatedSelectiveCn.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                                                                scheme = region.scheme,
                                                                                id = region.id,
                                                                                description = region.description,
                                                                                uri = region.uri
                                                                            )
                                                                        },
                                                                    locality = addressDetails.locality
                                                                        .let { locality ->
                                                                            UpdatedSelectiveCn.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
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
                    items = tender.items
                        .map { item ->
                            UpdatedSelectiveCn.Tender.Item(
                                id = item.id,
                                internalId = item.internalId,
                                classification = item.classification
                                    .let { classification ->
                                        UpdatedSelectiveCn.Tender.Item.Classification(
                                            scheme = classification.scheme,
                                            id = classification.id,
                                            description = classification.description
                                        )
                                    },
                                additionalClassifications = item.additionalClassifications
                                    .mapOrEmpty { additionalClassification ->
                                        UpdatedSelectiveCn.Tender.Item.AdditionalClassification(
                                            scheme = additionalClassification.scheme,
                                            id = additionalClassification.id,
                                            description = additionalClassification.description
                                        )
                                    },
                                quantity = item.quantity,
                                unit = item.unit
                                    .let { unit ->
                                        UpdatedSelectiveCn.Tender.Item.Unit(
                                            id = unit.id,
                                            name = unit.name
                                        )
                                    },
                                description = item.description,
                                relatedLot = LotId.fromString(item.relatedLot)
                            )
                        },
                    submissionMethod = tender.submissionMethod,
                    submissionMethodRationale = tender.submissionMethodRationale,
                    submissionMethodDetails = tender.submissionMethodDetails,
                    documents = tender.documents
                        .map { document ->
                            UpdatedSelectiveCn.Tender.Document(
                                documentType = document.documentType,
                                id = document.id,
                                title = document.title,
                                description = document.description,
                                relatedLots = document.relatedLots
                                    .mapOrEmpty { LotId.fromString(it) }
                            )
                        },
                    secondStage = tender.secondStage
                        ?.let { otherCriteria ->
                            UpdatedSelectiveCn.Tender.SecondStage(
                                minimumCandidates = otherCriteria.minimumCandidates,
                                maximumCandidates = otherCriteria.maximumCandidates
                            )
                        },
                    otherCriteria = tender.otherCriteria!!
                        .let { otherCriteria ->
                            UpdatedSelectiveCn.Tender.OtherCriteria(
                                reductionCriteria = otherCriteria.reductionCriteria,
                                qualificationSystemMethods = otherCriteria.qualificationSystemMethods
                            )
                        }
                )
            },
        amendment = cn.amendment
            ?.let { amendment ->
                if (amendment.relatedLots.isNotEmpty())
                    UpdatedSelectiveCn.Amendment(
                        relatedLots = amendment.relatedLots
                            .map { relatedLot ->
                                LotId.fromString(relatedLot)
                            }
                    )
                else
                    null
            }
    )
}
