package com.procurement.access.service

import com.procurement.access.application.service.cn.update.UpdateCnContext
import com.procurement.access.application.service.cn.update.UpdateCnData
import com.procurement.access.application.service.cn.update.UpdateCnWithPermanentId
import com.procurement.access.application.service.cn.update.UpdatedCn
import com.procurement.access.application.service.cn.update.replaceTemplateLotIds
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.enums.BusinessFunctionDocumentType
import com.procurement.access.domain.model.enums.BusinessFunctionType
import com.procurement.access.domain.model.enums.DocumentType
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.domain.model.enums.ProcurementMethod
import com.procurement.access.domain.model.enums.TenderDocumentType
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.isNotUniqueIds
import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.domain.model.money.Money
import com.procurement.access.domain.model.money.sum
import com.procurement.access.domain.model.uniqueIds
import com.procurement.access.domain.model.update
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.dto.cn.criteria.Period
import com.procurement.access.infrastructure.dto.cn.criteria.Requirement
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.lib.mapOrEmpty
import com.procurement.access.lib.orThrow
import com.procurement.access.lib.takeIfNotNullOrDefault
import com.procurement.access.lib.toSetBy
import com.procurement.access.lib.uniqueBy
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toDate
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service
import java.time.LocalDateTime

interface CNService {
    fun update(context: UpdateCnContext, data: UpdateCnData): UpdatedCn
}

@Service
class CNServiceImpl(
    private val generationService: GenerationService,
    private val tenderProcessDao: TenderProcessDao
) : CNService {
    override fun update(context: UpdateCnContext, data: UpdateCnData): UpdatedCn {
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

        val receivedLotsByIds: Map<String, UpdateCnData.Tender.Lot> = data.tender.lots.associateBy { it.id }
        val savedLotsByIds: Map<String, CNEntity.Tender.Lot> = cn.tender.lots.associateBy { it.id }
        val allLotsIds: Set<String> = receivedLotsByIds.keys + savedLotsByIds.keys
        val documentsIds: Set<String> = cn.tender.documents.toSetBy { it.id }

        data.checkDocuments(documentsIds) //VR-1.0.1.2.1, VR-1.0.1.2.2, VR-1.0.1.2.9
            .checkLotsCurrency(budgetCurrency = cn.planning.budget.amount.currency) //VR-1.0.1.4.2
            .checkLotsContractPeriod(pmd = context.pmd, cn = cn) //VR-1.0.1.4.3
            .checkRelatedLotItems(allLotsIds) //VR-1.0.1.5.4
            .checkRelatedLotsOfDocuments(allLotsIds) //VR-1.0.1.2.6
            .checkIdsProcuringEntity(cn) //VR-1.0.1.10.1

        val calculateLotsAmount = calculateLotsAmount(savedLotsByIds, receivedLotsByIds)
        cn.checkTenderAmount(calculateLotsAmount) //VR-1.0.1.3.2

        //BR-1.0.1.2.1
        val calculatedTenderContractPeriod = data.calculateTenderContractPeriod()
        cn.checkContractPeriod(calculatedTenderContractPeriod) //VR-1.0.1.6.1

        val receivedLotsIds = receivedLotsByIds.keys
        val savedLotsIds = savedLotsByIds.keys
        val idsNewLots = getNewElements(receivedLotsIds, savedLotsIds)
        val idsUpdateLots = getElementsForUpdate(receivedLotsIds, savedLotsIds)
        val idsCancelLots = getElementsForRemove(receivedLotsIds, savedLotsIds)

        val permanentLotsIdsByTemporalIds = idsNewLots.generatePermanentId(generationService::generatePermanentLotId)
        val dataWithPermanentId: UpdateCnWithPermanentId = data.replaceTemplateLotIds(permanentLotsIdsByTemporalIds)
        val receivedLotsByPermanentIds = dataWithPermanentId.tender.lots.associateBy { it.id }

        val newLots: List<CNEntity.Tender.Lot> = idsNewLots.map { id ->
            val permanentLotId = permanentLotsIdsByTemporalIds.getValue(id)
            createNewLot(receivedLotsByPermanentIds.getValue(permanentLotId))
        }

        val updatedLots = idsUpdateLots.map { id ->
            updateLot(src = receivedLotsByPermanentIds.getValue(id), dst = savedLotsByIds.getValue(id))
        }

        val removedLots = idsCancelLots.map { id ->
            removeLot(savedLotsByIds.getValue(id))
        }

        val updatedItems = cn.updateItems(dataWithPermanentId)

        //BR-1.0.1.5.2
        val updatedTenderDocuments = cn.updateTenderDocuments(dataWithPermanentId.tender.documents)

        //BR-1.0.1.1.2
        val updatedValue = CNEntity.Tender.Value(
            amount = calculateLotsAmount.amount,
            currency = calculateLotsAmount.currency
        )

        //BR-1.0.1.15.3
        val updatedProcuringEntity = if (dataWithPermanentId.tender.procuringEntity != null)
            cn.tender.procuringEntity.update(dataWithPermanentId.tender.procuringEntity.persons)
        else
            cn.tender.procuringEntity

        val updatedCN = cn.copy(
            planning = cn.planning.copy(
                rationale = dataWithPermanentId.planning?.rationale ?: cn.planning.rationale,
                budget = cn.planning.budget.copy(
                    description = dataWithPermanentId.planning?.budget?.description
                        ?: cn.planning.budget.description
                )
            ),
            tender = cn.tender.copy(
                title = dataWithPermanentId.tender.title,
                description = dataWithPermanentId.tender.description,
                tenderPeriod = dataWithPermanentId.tender.tenderPeriod.let { tenderPeriod ->
                    CNEntity.Tender.TenderPeriod(
                        startDate = tenderPeriod.startDate,
                        endDate = tenderPeriod.endDate
                    )
                },
                contractPeriod = calculatedTenderContractPeriod,
                procurementMethodRationale = dataWithPermanentId.tender.procurementMethodRationale
                    .takeIfNotNullOrDefault(
                        cn.tender.procurementMethodRationale
                    ),
                procurementMethodAdditionalInfo = dataWithPermanentId.tender.procurementMethodAdditionalInfo
                    .takeIfNotNullOrDefault(
                        cn.tender.procurementMethodAdditionalInfo
                    ),
                procurementMethodModalities = dataWithPermanentId.tender.procurementMethodModalities?.toSet()
                    .takeIfNotNullOrDefault(
                        cn.tender.procurementMethodModalities
                    ),
                awardCriteria = dataWithPermanentId.tender.awardCriteria
                    .takeIfNotNullOrDefault(cn.tender.awardCriteria),
                awardCriteriaDetails = dataWithPermanentId.tender.awardCriteriaDetails
                    .takeIfNotNullOrDefault(cn.tender.awardCriteriaDetails),
                procuringEntity = updatedProcuringEntity, //BR-1.0.1.15.3
                value = updatedValue, //BR-1.0.1.1.2
                lots = updatedLots + removedLots + newLots,
                items = updatedItems,
                documents = updatedTenderDocuments //BR-1.0.1.5.2
            ),
            amendment = cn.amendment
                ?.let { amendment ->
                    amendment.copy(
                        relatedLots = amendment.relatedLots + idsCancelLots
                    )
                }
                ?: CNEntity.Amendment(
                    relatedLots = idsCancelLots.toList()
                )
        )

        //VR-1.0.1.4.7
        if (!updatedCN.tender.lots.any { it.status == LotStatus.ACTIVE })
            throw ErrorException(ErrorType.NO_ACTIVE_LOTS)

        tenderProcessDao.save(
            TenderProcessEntity(
                cpId = context.cpid,
                token = entity.token,
                stage = context.stage,
                owner = entity.owner,
                createdDate = context.startDate.toDate(),
                jsonData = toJson(cn)
            )
        )

        return getResponse(
            cn = updatedCN,
            data = dataWithPermanentId,
            lotsChanged = (idsNewLots.isNotEmpty() || idsCancelLots.isNotEmpty())
        )
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
    private fun UpdateCnData.checkDocuments(documentIds: Set<String>): UpdateCnData {
        //VR-1.0.1.2.1
        val uniqueDocumentIds: Set<String> = this.tender.documents.uniqueIds()
            ?: throw ErrorException(
                error = ErrorType.INVALID_DOCS_ID,
                message = "The list documents of tender contain duplicates."
            )

        //VR-1.0.1.2.2
        this.tender.documents.forEach { document ->
            when (document.documentType) {
                TenderDocumentType.TENDER_NOTICE,
                TenderDocumentType.BIDDING_DOCUMENTS,
                TenderDocumentType.TECHNICAL_SPECIFICATIONS,
                TenderDocumentType.EVALUATION_CRITERIA,
                TenderDocumentType.CLARIFICATIONS,
                TenderDocumentType.ELIGIBILITY_CRITERIA,
                TenderDocumentType.RISK_PROVISIONS,
                TenderDocumentType.BILL_OF_QUANTITY,
                TenderDocumentType.CONFLICT_OF_INTEREST,
                TenderDocumentType.PROCUREMENT_PLAN,
                TenderDocumentType.CONTRACT_DRAFT,
                TenderDocumentType.COMPLAINTS,
                TenderDocumentType.ILLUSTRATION,
                TenderDocumentType.CANCELLATION_DETAILS,
                TenderDocumentType.EVALUATION_REPORTS,
                TenderDocumentType.SHORTLISTED_FIRMS,
                TenderDocumentType.CONTRACT_ARRANGEMENTS,
                TenderDocumentType.CONTRACT_GUARANTEES -> Unit
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
    private fun UpdateCnData.checkRelatedLotsOfDocuments(allLotsIds: Set<String>): UpdateCnData {
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
    private fun CNEntity.checkTenderAmount(calculatedLotsAmount: Money): CNEntity {
        val tenderValue = this.tender.value
        if (tenderValue.currency != calculatedLotsAmount.currency)
            throw ErrorException(
                error = ErrorType.INVALID_CURRENCY,
                message = "The currency of tender not compatible with the currency of lots."
            )

        if (tenderValue.amount > calculatedLotsAmount.amount)
            throw ErrorException(
                error = ErrorType.INVALID_TENDER,
                message = "The amount of tender greater than the amount of lots."
            )
        return this
    }

    private fun calculateLotsAmount(
        savedLotsByIds: Map<String, CNEntity.Tender.Lot>,
        receivedLotsByIds: Map<String, UpdateCnData.Tender.Lot>
    ): Money {
        val allLotsIds: Set<String> = receivedLotsByIds.keys + savedLotsByIds.keys

        return allLotsIds.asSequence()
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

    /**
     * VR-1.0.1.4.1 id (lot)
     * eAccess analyzes Lot.ID from Request:
     * a. IF every lot.ID from Request is included once in list from Request, validation is successful;
     * b. ELSE eAccess throws Exception: "Lot ID are repeated in list";
     */
    private fun UpdateCnData.checkLotsIds(): UpdateCnData {
        this.tender.lots.isNotUniqueIds {
            ErrorException(
                error = ErrorType.INVALID_LOT_ID,
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
    private fun UpdateCnData.checkLotsCurrency(budgetCurrency: String): UpdateCnData {
        this.tender.lots.forEach { lot ->
            if (lot.value.currency != budgetCurrency)
                throw ErrorException(
                    error = ErrorType.INVALID_LOT_CURRENCY,
                    message = "The lot with id: ${lot.id} contain currency which not equals budget currency."
                )
        }

        return this
    }

    /**
     * VR-1.0.1.4.3 contractPeriod (lot) (no lots in DB || update lots)
     * 1. FOR every Lot object from Request eAccess checks startDate && endDate values:
     *   a. IF startDate && endDate value are present in calendar of current year, validation is successful;
     *   b. ELSE (startDate && endDate value are not found in calendar) { eAccess throws Exception: "Date doesn't not exist";
     * 2. FOR every Lot object from Request eAccess compares lot.contractPeriod.startDate && lot.contractPeriod.endDate:
     *   a. IF value of lot.contractPeriod.startDate < (earlier than) value of lot.contractPeriod.endDate, validation is successful;
     *   b. ELSE eAccess throws Exception: "Invalid date-time values in lot contract period";
     * 3. eAccess analyzes pmd value from the context of Request:
     *   a. IF pmd == "OT" || "SV" || "MV", eAccess checks lot.contractPeriod.startDate in every Lot object from Request:
     *     i.  IF value of lot.contractPeriod.startDate from Request > (later than) value of tenderPeriod.endDate from Request, validation is successful;
     *     ii. ELSE eAccess throws Exception: "Invalid date-time values in lot contract period";
     *   b. ELSE IF (pmd == "DA" || "NP" || "OP") { eAccess checks lot.contractPeriod.startDate in every Lot object from Request:
     *     i.  IF value of lot.contractPeriod.startDate from Request > (later than) value of startDate from the context of Request, validation is successful;
     *     ii. ELSE eAccess throws Exception: "Invalid date-time values in lot contract period";
     */
    private fun UpdateCnData.checkLotsContractPeriod(
        pmd: ProcurementMethod,
        cn: CNEntity
    ): UpdateCnData {
        this.tender.lots.forEach { lot ->
            if (lot.contractPeriod.startDate >= lot.contractPeriod.endDate)
                throw ErrorException(error = ErrorType.INVALID_LOT_CONTRACT_PERIOD)

            when (pmd) {
                ProcurementMethod.OT, ProcurementMethod.TEST_OT,
                ProcurementMethod.SV, ProcurementMethod.TEST_SV,
                ProcurementMethod.MV, ProcurementMethod.TEST_MV -> {
                    if (lot.contractPeriod.startDate <= cn.tender.tenderPeriod!!.endDate)
                        throw ErrorException(error = ErrorType.INVALID_LOT_CONTRACT_PERIOD)
                }

                ProcurementMethod.DA, ProcurementMethod.TEST_DA,
                ProcurementMethod.NP, ProcurementMethod.TEST_NP,
                ProcurementMethod.OP, ProcurementMethod.TEST_OP,
                ProcurementMethod.RT, ProcurementMethod.TEST_RT,
                ProcurementMethod.FA, ProcurementMethod.TEST_FA -> throw ErrorException(ErrorType.INVALID_PMD)
            }
        }
        return this
    }

    /**
     * VR-1.0.1.5.1 id (item)
     * eAccess analyzes item.ID from Request:
     *   a. IF every item.ID from Request is included once in list from Request, validation is successful;
     *   b. ELSE eAccess throws Exception: "Item ID are repeated in list";
     */
    private fun UpdateCnData.checkUniqueIdsItems(): UpdateCnData {
        this.tender.items.isNotUniqueIds {
            throw ErrorException(
                error = ErrorType.INVALID_LOT_ID,
                message = "The list lots of tender contain duplicates."
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

    private fun UpdateCnData.checkRelatedLotItems(allLotsIds: Set<String>): UpdateCnData {
        this.tender.items.forEach { item ->
            if (item.validation(allLotsIds)) {
                throw ErrorException(
                    error = ErrorType.INVALID_LOT_ID,
                    message = "The list lots of tender contain duplicates."
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

    private fun UpdateCnData.calculateTenderContractPeriod(): CNEntity.Tender.ContractPeriod {
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
    private fun UpdateCnData.checkIdsProcuringEntity(cn: CNEntity): UpdateCnData {
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
    private fun UpdateCnData.checkIdsPersons(): UpdateCnData {
        val isUnique = this.tender.procuringEntity?.persons?.uniqueBy {
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
     * VR-1.0.1.10.6
     * 1. eAccess checks the availability in procurinfEntity from request of one Persones object with
     *    one businessFunctions object where persones.businessFunctions.type == "authority" in Persones array from Request:
     *   a. IF [there is Persones object with businessFunctions object where type == "authority"] then validation is successful;
     *   b. ELSE eAccess throws Exception: "Authority person shoud be specified in Request";
     * 2. eAccess checks persones.businessFunctions.type values in all businessFuctions object from Request;
     *   a. IF businessFunctions.type == oneOf procuringEntityBusinessFuncTypeEnum value (link), validation is successful;
     *   b. ELSE eAccess throws Exception: "Invalid business functions type";
     *
     * VR-1.0.1.10.7 startDate (businessFunctions.period)
     * eAccess compares businessFunctions.period.startDate and startDate from the context of Request:
     * a. IF [businessFunctions.period.startDate <= (less || equal to) startDate from Request] then: validation is successful;
     * b. ELSE eAccess throws Exception: "Invalid period in bussiness function specification";
     *
     * VR-1.0.1.2.1
     * VR-1.0.1.2.8
     */
    private fun UpdateCnData.checkBusinessFunctions(startDate: LocalDateTime): UpdateCnData {
        var containAuthority = false
        val uniqueBusinessFunctionId = mutableSetOf<String>()
        var uniqueDocumentId: Set<String> = emptySet()
        this.businessFunctions()
            ?.forEach { businessFunction ->
                if (!uniqueBusinessFunctionId.add(businessFunction.id))
                    throw ErrorException(
                        error = ErrorType.INVALID_BUSINESS_FUNCTION,
                        message = "Ids of business function are not unique."
                    )

                when (businessFunction.type) {
                    BusinessFunctionType.AUTHORITY,
                    BusinessFunctionType.PROCURMENT_OFFICER,
                    BusinessFunctionType.CONTACT_POINT,
                    BusinessFunctionType.TECHNICAL_EVALUATOR,
                    BusinessFunctionType.TECHNICAL_OPENER,
                    BusinessFunctionType.PRICE_OPENER,
                    BusinessFunctionType.PRICE_EVALUATOR -> Unit
                }

                if (businessFunction.type == BusinessFunctionType.AUTHORITY) {
                    if (containAuthority)
                        throw ErrorException(
                            error = ErrorType.INVALID_BUSINESS_FUNCTION,
                            message = "More than one business function with type 'AUTHORITY'."
                        )
                    else
                        containAuthority = true
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

/*    private fun UpdateCnData.checkBusinessFunctions(startDate: LocalDateTime): UpdateCnData {

        data class CheckContext(
            val containAuthority: Boolean = false,
            val uniqueDocumentId: Set<String> = emptySet(),
            val uniqueBusinessFunctionId: Set<String> = emptySet()
        )

        this.businessFunctions()
            ?.check(context = CheckContext()) { ctx, businessFunction ->
                if (businessFunction.id in ctx.uniqueBusinessFunctionId)
                    throw ErrorException(
                        error = ErrorType.INVALID_BUSINESS_FUNCTION,
                        message = "Ids of business function are not unique."
                    )
                ctx.copy(uniqueBusinessFunctionId = ctx.uniqueBusinessFunctionId + businessFunction.id)
            }

        var containAuthority = false
        val uniqueBusinessFunctionId = mutableSetOf<String>()
        var uniqueDocumentId: Set<String> = emptySet()
        this.businessFunctions()
            ?.forEach { businessFunction ->
                if (!uniqueBusinessFunctionId.add(businessFunction.id))
                    throw ErrorException(
                        error = ErrorType.INVALID_BUSINESS_FUNCTION,
                        message = "Ids of business function are not unique."
                    )

                when (businessFunction.type) {
                    BusinessFunctionType.AUTHORITY,
                    BusinessFunctionType.PROCURMENT_OFFICER,
                    BusinessFunctionType.CONTACT_POINT,
                    BusinessFunctionType.TECHNICAL_EVALUATOR,
                    BusinessFunctionType.TECHNICAL_OPENER,
                    BusinessFunctionType.PRICE_OPENER,
                    BusinessFunctionType.PRICE_EVALUATOR -> Unit
                }

                if (businessFunction.type == BusinessFunctionType.AUTHORITY) {
                    if (containAuthority)
                        throw ErrorException(
                            error = ErrorType.INVALID_BUSINESS_FUNCTION,
                            message = "More than one business function with type 'AUTHORITY'."
                        )
                    else
                        containAuthority = true
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
    }*/

    private fun UpdateCnData.businessFunctions() =
        this.tender.procuringEntity?.persons
            ?.asSequence()
            ?.flatMap { person ->
                person.businessFunctions.asSequence()
            }

    /**
     *
     *
     * VR-1.0.1.2.8
     * eAccess checks documents.documentType values in all Documents object from Request;
     * IF document.documentType == oneOf BussinesFunctionsDocumentType value, validation is successful;
     * ELSE eAccess throws Exception: "Invalid document type";
     */
    private fun UpdateCnData.Tender.ProcuringEntity.Person.BusinessFunction.checkDocuments() {
        this.documents.forEach { document ->
            when (document.documentType) {
                BusinessFunctionDocumentType.REGULATORY_DOCUMENT -> Unit
            }
        }
    }

    private fun <T, R> Collection<T>.generatePermanentId(generator: () -> R): Map<T, R> =
        this.asSequence()
            .map { id ->
                id to generator()
            }
            .toMap()

    private fun createNewLot(lot: UpdateCnWithPermanentId.Tender.Lot): CNEntity.Tender.Lot = CNEntity.Tender.Lot(
        id = generationService.generatePermanentLotId(),
        internalId = lot.internalId,
        title = lot.title,
        description = lot.description,
        value = lot.value.let { value ->
            CNEntity.Tender.Lot.Value(
                amount = value.amount,
                currency = value.currency
            )
        },
        contractPeriod = lot.contractPeriod.let { contractPeriod ->
            CNEntity.Tender.Lot.ContractPeriod(
                startDate = contractPeriod.startDate,
                endDate = contractPeriod.endDate
            )
        },
        options = listOf(CNEntity.Tender.Lot.Option(false)),
        variants = listOf(CNEntity.Tender.Lot.Variant(false)),
        renewals = listOf(CNEntity.Tender.Lot.Renewal(false)),
        recurrentProcurement = listOf(CNEntity.Tender.Lot.RecurrentProcurement(false)),
        status = LotStatus.ACTIVE,
        statusDetails = LotStatusDetails.EMPTY,
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
                                        scheme = country.scheme,
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

    private fun updateLot(src: UpdateCnWithPermanentId.Tender.Lot, dst: CNEntity.Tender.Lot): CNEntity.Tender.Lot =
        dst.copy(
            internalId = src.internalId,
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

    private fun removeLot(lot: CNEntity.Tender.Lot): CNEntity.Tender.Lot = lot.copy(
        status = LotStatus.CANCELLED,
        statusDetails = LotStatusDetails.EMPTY
    )

    fun CNEntity.updateItems(data: UpdateCnWithPermanentId): List<CNEntity.Tender.Item> {
        return this.tender.items.update(sources = data.tender.items) { dst, src ->
            dst.copy(
                description = src.description,
                relatedLot = src.relatedLot
            )
        }
    }

    private fun CNEntity.Tender.ProcuringEntity.update(persons: List<UpdateCnWithPermanentId.Tender.ProcuringEntity.Person>): CNEntity.Tender.ProcuringEntity {
        val receivedPersonsById = persons.associateBy { it.identifier.id }
        val savedPersonsById = this.persones?.associateBy { it.identifier.id } ?: emptyMap()

        val receivedPersonsIds = receivedPersonsById.keys
        val savedPersonsIds = savedPersonsById.keys

        val idsAllPersons = receivedPersonsIds.union(savedPersonsIds)
        val idsNewPersons = getNewElements(receivedPersonsIds, savedPersonsIds)
        val idsUpdatePersons = getElementsForUpdate(receivedPersonsIds, savedPersonsIds)
        val idsRemovePersons = getElementsForRemove(receivedPersonsIds, savedPersonsIds)

        val updatedPersons = idsAllPersons.asSequence()
            .filter { id ->
                id !in idsRemovePersons
            }
            .map { id ->
                when (id) {
                    in idsNewPersons -> createPerson(receivedPersonsById.getValue(id))
                    in idsUpdatePersons -> savedPersonsById.getValue(id).update(receivedPersonsById.getValue(id))
                    else -> throw IllegalStateException()
                }
            }
            .toList()

        return this.copy(persones = updatedPersons)
    }

    private fun createPerson(person: UpdateCnWithPermanentId.Tender.ProcuringEntity.Person): CNEntity.Tender.ProcuringEntity.Persone =
        CNEntity.Tender.ProcuringEntity.Persone(
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

    private fun CNEntity.Tender.ProcuringEntity.Persone.update(person: UpdateCnWithPermanentId.Tender.ProcuringEntity.Person): CNEntity.Tender.ProcuringEntity.Persone {
        return this.copy(
            title = person.title,
            name = person.name
        )
    }

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
    private fun CNEntity.updateTenderDocuments(documentsFromRequest: List<UpdateCnWithPermanentId.Tender.Document>): List<CNEntity.Tender.Document> {
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
                            relatedLots = document.relatedLots.toList()
                        )
                        ?: CNEntity.Tender.Document(
                            id = document.id,
                            documentType = DocumentType.fromString(document.documentType.value),
                            title = document.title,
                            description = document.description,
                            relatedLots = document.relatedLots.toList()
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
        data: UpdateCnWithPermanentId,
        lotsChanged: Boolean
    ) = UpdatedCn(
        lotsChanged = lotsChanged,
        planning = cn.planning.let { planning ->
            UpdatedCn.Planning(
                rationale = planning.rationale,
                budget = planning.budget.let { budget ->
                    UpdatedCn.Planning.Budget(
                        description = budget.description,
                        amount = budget.amount.let { amount ->
                            Money(
                                amount = amount.amount,
                                currency = amount.currency
                            )
                        },
                        isEuropeanUnionFunded = budget.isEuropeanUnionFunded,
                        budgetBreakdowns = budget.budgetBreakdowns.map { budgetBreakdown ->
                            UpdatedCn.Planning.Budget.BudgetBreakdown(
                                id = budgetBreakdown.id,
                                description = budgetBreakdown.description,
                                amount = budgetBreakdown.amount.let { amount ->
                                    Money(
                                        amount = amount.amount,
                                        currency = amount.currency
                                    )
                                },
                                period = budgetBreakdown.period.let { period ->
                                    UpdatedCn.Planning.Budget.BudgetBreakdown.Period(
                                        startDate = period.startDate,
                                        endDate = period.endDate
                                    )
                                },
                                sourceParty = budgetBreakdown.sourceParty.let { sourceParty ->
                                    UpdatedCn.Planning.Budget.BudgetBreakdown.SourceParty(
                                        id = sourceParty.id,
                                        name = sourceParty.name
                                    )
                                },
                                europeanUnionFunding = budgetBreakdown.europeanUnionFunding?.let { europeanUnionFunding ->
                                    UpdatedCn.Planning.Budget.BudgetBreakdown.EuropeanUnionFunding(
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
            UpdatedCn.Tender(
                id = tender.id,
                status = tender.status,
                statusDetails = tender.statusDetails,
                title = tender.title,
                description = tender.description,
                classification = tender.classification.let { classification ->
                    UpdatedCn.Tender.Classification(
                        scheme = classification.scheme,
                        id = classification.id,
                        description = classification.description
                    )
                },
                requiresElectronicCatalogue = tender.requiresElectronicCatalogue,
                tenderPeriod = tender.tenderPeriod.let { tenderPeriod ->
                    UpdatedCn.Tender.TenderPeriod(
                        startDate = tenderPeriod!!.startDate,
                        endDate = tenderPeriod.endDate
                    )
                },
                acceleratedProcedure = tender.acceleratedProcedure.let { acceleratedProcedure ->
                    UpdatedCn.Tender.AcceleratedProcedure(
                        isAcceleratedProcedure = acceleratedProcedure.isAcceleratedProcedure
                    )
                },
                designContest = tender.designContest.let { designContest ->
                    UpdatedCn.Tender.DesignContest(
                        serviceContractAward = designContest.serviceContractAward
                    )
                },
                electronicWorkflows = tender.electronicWorkflows.let { electronicWorkflows ->
                    UpdatedCn.Tender.ElectronicWorkflows(
                        useOrdering = electronicWorkflows.useOrdering,
                        usePayment = electronicWorkflows.usePayment,
                        acceptInvoicing = electronicWorkflows.acceptInvoicing
                    )
                },
                jointProcurement = tender.jointProcurement.let { jointProcurement ->
                    UpdatedCn.Tender.JointProcurement(
                        isJointProcurement = jointProcurement.isJointProcurement
                    )
                },
                procedureOutsourcing = tender.procedureOutsourcing.let { procedureOutsourcing ->
                    UpdatedCn.Tender.ProcedureOutsourcing(
                        procedureOutsourced = procedureOutsourcing.procedureOutsourced
                    )
                },
                framework = tender.framework.let { framework ->
                    UpdatedCn.Tender.Framework(
                        isAFramework = framework.isAFramework
                    )
                },
                dynamicPurchasingSystem = tender.dynamicPurchasingSystem.let { dynamicPurchasingSystem ->
                    UpdatedCn.Tender.DynamicPurchasingSystem(
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
                contractPeriod = tender.contractPeriod!!.let { contractPeriod ->
                    UpdatedCn.Tender.ContractPeriod(
                        startDate = contractPeriod.startDate,
                        endDate = contractPeriod.endDate
                    )
                },
                procurementMethodModalities = tender.procurementMethodModalities?.toList() ?: emptyList(),
                electronicAuctions = data.tender.electronicAuctions?.let { electronicAuctions ->
                    UpdatedCn.Tender.ElectronicAuctions(
                        details = electronicAuctions.details.map { detail ->
                            UpdatedCn.Tender.ElectronicAuctions.Detail(
                                id = detail.id,
                                relatedLot = LotId.fromString(detail.relatedLot),
                                electronicAuctionModalities = detail.electronicAuctionModalities.map { modality ->
                                    UpdatedCn.Tender.ElectronicAuctions.Detail.ElectronicAuctionModality(
                                        eligibleMinimumDifference = modality.eligibleMinimumDifference.let { emd ->
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
                procuringEntity = tender.procuringEntity.let { procuringEntity ->
                    UpdatedCn.Tender.ProcuringEntity(
                        id = procuringEntity.id,
                        name = procuringEntity.name,
                        identifier = procuringEntity.identifier.let { identifier ->
                            UpdatedCn.Tender.ProcuringEntity.Identifier(
                                scheme = identifier.scheme,
                                id = identifier.id,
                                legalName = identifier.legalName,
                                uri = identifier.uri
                            )
                        },
                        additionalIdentifiers = procuringEntity.additionalIdentifiers.mapOrEmpty { additionalIdentifier ->
                            UpdatedCn.Tender.ProcuringEntity.AdditionalIdentifier(
                                scheme = additionalIdentifier.scheme,
                                id = additionalIdentifier.id,
                                legalName = additionalIdentifier.legalName,
                                uri = additionalIdentifier.uri
                            )
                        },
                        address = procuringEntity.address.let { address ->
                            UpdatedCn.Tender.ProcuringEntity.Address(
                                streetAddress = address.streetAddress,
                                postalCode = address.postalCode,
                                addressDetails = address.addressDetails.let { addressDetails ->
                                    UpdatedCn.Tender.ProcuringEntity.Address.AddressDetails(
                                        country = addressDetails.country.let { country ->
                                            UpdatedCn.Tender.ProcuringEntity.Address.AddressDetails.Country(
                                                scheme = country.scheme,
                                                id = country.id,
                                                description = country.description,
                                                uri = country.uri
                                            )
                                        },
                                        region = addressDetails.region.let { region ->
                                            UpdatedCn.Tender.ProcuringEntity.Address.AddressDetails.Region(
                                                scheme = region.scheme,
                                                id = region.id,
                                                description = region.description,
                                                uri = region.uri
                                            )
                                        },
                                        locality = addressDetails.locality.let { locality ->
                                            UpdatedCn.Tender.ProcuringEntity.Address.AddressDetails.Locality(
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
                            UpdatedCn.Tender.ProcuringEntity.ContactPoint(
                                name = contactPoint.name,
                                email = contactPoint.email,
                                telephone = contactPoint.telephone,
                                faxNumber = contactPoint.faxNumber,
                                url = contactPoint.url
                            )
                        },
                        persones = procuringEntity.persones.mapOrEmpty { person ->
                            UpdatedCn.Tender.ProcuringEntity.Person(
                                name = person.name,
                                title = person.title,
                                identifier = UpdatedCn.Tender.ProcuringEntity.Person.Identifier(
                                    id = person.identifier.id,
                                    scheme = person.identifier.scheme,
                                    uri = person.identifier.uri
                                ),
                                businessFunctions = person.businessFunctions.map { businessFunction ->
                                    UpdatedCn.Tender.ProcuringEntity.Person.BusinessFunction(
                                        id = businessFunction.id,
                                        type = businessFunction.type,
                                        jobTitle = businessFunction.jobTitle,
                                        period = UpdatedCn.Tender.ProcuringEntity.Person.BusinessFunction.Period(
                                            startDate = businessFunction.period.startDate
                                        ),
                                        documents = businessFunction.documents.mapOrEmpty { document ->
                                            UpdatedCn.Tender.ProcuringEntity.Person.BusinessFunction.Document(
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
                value = tender.value.let { value ->
                    Money(
                        amount = value.amount,
                        currency = value.currency
                    )
                },
                lotGroups = tender.lotGroups.map { lotGroup ->
                    UpdatedCn.Tender.LotGroup(
                        optionToCombine = lotGroup.optionToCombine
                    )
                },
                criteria = data.tender.criteria.mapOrEmpty { criteria ->
                    UpdatedCn.Tender.Criteria(
                        id = criteria.id,
                        title = criteria.title,
                        description = criteria.description,
                        requirementGroups = criteria.requirementGroups.map {
                            UpdatedCn.Tender.Criteria.RequirementGroup(
                                id = it.id,
                                description = it.description,
                                requirements = it.requirements.map { requirement ->
                                    Requirement(
                                        id = requirement.id,
                                        description = requirement.description,
                                        title = requirement.title,
                                        period = requirement.period?.let { period ->
                                            Period(
                                                startDate = period.startDate,
                                                endDate = period.endDate
                                            )
                                        },
                                        dataType = requirement.dataType,
                                        value = requirement.value
                                    )
                                }
                            )
                        },
                        relatesTo = criteria.relatesTo,
                        relatedItem = criteria.relatedItem
                    )
                },
                conversions = tender.conversions.mapOrEmpty { conversion ->
                    UpdatedCn.Tender.Conversion(
                        id = conversion.id,
                        relatedItem = conversion.relatedItem,
                        relatesTo = conversion.relatesTo,
                        rationale = conversion.rationale,
                        description = conversion.description,
                        coefficients = conversion.coefficients.map { coefficient ->
                            UpdatedCn.Tender.Conversion.Coefficient(
                                id = coefficient.id,
                                value = coefficient.value,
                                coefficient = coefficient.coefficient
                            )
                        }
                    )
                },
                lots = tender.lots.map { lot ->
                    UpdatedCn.Tender.Lot(
                        id = LotId.fromString(lot.id),
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
                        options = lot.options.map { option ->
                            UpdatedCn.Tender.Lot.Option(
                                hasOptions = option.hasOptions
                            )
                        },
                        variants = lot.variants.map { variant ->
                            UpdatedCn.Tender.Lot.Variant(
                                hasVariants = variant.hasVariants
                            )
                        },
                        renewals = lot.renewals.map { renewal ->
                            UpdatedCn.Tender.Lot.Renewal(
                                hasRenewals = renewal.hasRenewals
                            )
                        },
                        recurrentProcurements = lot.recurrentProcurement.map { recurrentProcurement ->
                            UpdatedCn.Tender.Lot.RecurrentProcurement(
                                isRecurrent = recurrentProcurement.isRecurrent
                            )
                        },
                        contractPeriod = lot.contractPeriod.let { contractPeriod ->
                            UpdatedCn.Tender.Lot.ContractPeriod(
                                startDate = contractPeriod.startDate,
                                endDate = contractPeriod.endDate
                            )
                        },
                        placeOfPerformance = lot.placeOfPerformance.let { placeOfPerformance ->
                            UpdatedCn.Tender.Lot.PlaceOfPerformance(
                                description = placeOfPerformance.description,
                                address = placeOfPerformance.address.let { address ->
                                    UpdatedCn.Tender.Lot.PlaceOfPerformance.Address(
                                        streetAddress = address.streetAddress,
                                        postalCode = address.postalCode,
                                        addressDetails = address.addressDetails.let { addressDetails ->
                                            UpdatedCn.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                                country = addressDetails.country.let { country ->
                                                    UpdatedCn.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                                        scheme = country.scheme,
                                                        id = country.id,
                                                        description = country.description,
                                                        uri = country.uri
                                                    )
                                                },
                                                region = addressDetails.region.let { region ->
                                                    UpdatedCn.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                                        scheme = region.scheme,
                                                        id = region.id,
                                                        description = region.description,
                                                        uri = region.uri
                                                    )
                                                },
                                                locality = addressDetails.locality.let { locality ->
                                                    UpdatedCn.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
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
                    UpdatedCn.Tender.Item(
                        id = item.id,
                        internalId = item.internalId,
                        classification = item.classification.let { classification ->
                            UpdatedCn.Tender.Item.Classification(
                                scheme = classification.scheme,
                                id = classification.id,
                                description = classification.description
                            )
                        },
                        additionalClassifications = item.additionalClassifications.mapOrEmpty { additionalClassification ->
                            UpdatedCn.Tender.Item.AdditionalClassification(
                                scheme = additionalClassification.scheme,
                                id = additionalClassification.id,
                                description = additionalClassification.description
                            )
                        },
                        quantity = item.quantity,
                        unit = item.unit.let { unit ->
                            UpdatedCn.Tender.Item.Unit(
                                id = unit.id,
                                name = unit.name
                            )
                        },
                        description = item.description,
                        relatedLot = LotId.fromString(item.relatedLot)
                    )
                },
                awardCriteria = data.tender.awardCriteria,
                awardCriteriaDetails = data.tender.awardCriteriaDetails,
                submissionMethod = tender.submissionMethod,
                submissionMethodRationale = tender.submissionMethodRationale,
                submissionMethodDetails = tender.submissionMethodDetails,
                documents = tender.documents.map { document ->
                    UpdatedCn.Tender.Document(
                        documentType = TenderDocumentType.fromString(document.documentType.value),
                        id = document.id,
                        title = document.title,
                        description = document.description,
                        relatedLots = document.relatedLots.mapOrEmpty { LotId.fromString(it) }
                    )
                }
            )
        },
        amendment = cn.amendment.let { amendment ->
            UpdatedCn.Amendment(
                relatedLots = amendment?.relatedLots.mapOrEmpty { relatedLot ->
                    LotId.fromString(relatedLot)
                }
            )
        }
    )
}
