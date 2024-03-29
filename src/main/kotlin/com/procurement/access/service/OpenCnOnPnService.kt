package com.procurement.access.service

import com.procurement.access.application.model.context.CheckOpenCnOnPnContext
import com.procurement.access.application.service.CheckedOpenCnOnPn
import com.procurement.access.application.service.CreateOpenCnOnPnContext
import com.procurement.access.domain.model.coefficient.CoefficientValue
import com.procurement.access.domain.model.conversion.buildConversion
import com.procurement.access.domain.model.criteria.buildCriterion
import com.procurement.access.domain.model.criteria.generatePermanentRequirementIds
import com.procurement.access.domain.model.criteria.replaceTemporalItemId
import com.procurement.access.domain.model.enums.AwardCriteria
import com.procurement.access.domain.model.enums.AwardCriteriaDetails
import com.procurement.access.domain.model.enums.BusinessFunctionDocumentType
import com.procurement.access.domain.model.enums.BusinessFunctionType
import com.procurement.access.domain.model.enums.DocumentType
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.domain.model.enums.MainProcurementCategory
import com.procurement.access.domain.model.enums.ProcurementMethodModalities
import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import com.procurement.access.domain.model.persone.PersonId
import com.procurement.access.domain.model.requirement.EligibleEvidenceId
import com.procurement.access.domain.model.requirement.ExpectedValue
import com.procurement.access.domain.model.requirement.Requirement
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.exception.ErrorType.DATA_NOT_FOUND
import com.procurement.access.exception.ErrorType.INVALID_DOCS_ID
import com.procurement.access.exception.ErrorType.INVALID_DOCS_RELATED_LOTS
import com.procurement.access.exception.ErrorType.INVALID_ITEMS_RELATED_LOTS
import com.procurement.access.exception.ErrorType.INVALID_LOT_CONTRACT_PERIOD
import com.procurement.access.exception.ErrorType.INVALID_LOT_CURRENCY
import com.procurement.access.exception.ErrorType.INVALID_OWNER
import com.procurement.access.exception.ErrorType.INVALID_PMM
import com.procurement.access.exception.ErrorType.INVALID_PROCURING_ENTITY
import com.procurement.access.exception.ErrorType.INVALID_TENDER_AMOUNT
import com.procurement.access.exception.ErrorType.INVALID_TOKEN
import com.procurement.access.exception.ErrorType.ITEM_ID_DUPLICATED
import com.procurement.access.exception.ErrorType.LOT_ID_DUPLICATED
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.entity.PNEntity
import com.procurement.access.infrastructure.handler.v1.model.request.OpenCnOnPnRequest
import com.procurement.access.infrastructure.handler.v1.model.request.document.DocumentRequest
import com.procurement.access.infrastructure.handler.v1.model.response.CriterionClassificationResponse
import com.procurement.access.infrastructure.handler.v1.model.response.OpenCnOnPnResponse
import com.procurement.access.infrastructure.repository.CassandraTenderProcessRepositoryV1
import com.procurement.access.infrastructure.service.command.checkCriteriaAndConversion
import com.procurement.access.lib.errorIfBlank
import com.procurement.access.lib.extension.getDuplicate
import com.procurement.access.lib.extension.isUnique
import com.procurement.access.lib.extension.toSet
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.util.*

@Service
class OpenCnOnPnService(
    private val generationService: GenerationService,
    private val tenderRepository: CassandraTenderProcessRepositoryV1,
    private val rulesService: RulesService
) {

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

    fun check(context: CheckOpenCnOnPnContext, data: OpenCnOnPnRequest): CheckedOpenCnOnPn {
        data.validateTextAttributes()
        data.validateDuplicates()
        data.validateEmptyObject()

        val entity: TenderProcessEntity =
            tenderRepository.getByCpidAndOcid(context.cpid, context.ocid)
                ?: throw ErrorException(DATA_NOT_FOUND)

        val pnEntity: PNEntity = toObject(PNEntity::class.java, entity.jsonData)

        //VR-3.8.18 Tender status
        checkTenderStatus(pnEntity)

        //VR-3.8.3 Documents (duplicate)
        checkDocuments(documentsFromRequest = data.tender.documents, documentsFromPN = pnEntity.tender.documents)
        //VR-3.6.1
        checkTenderDocumentsTypes(data)

        data.tender.procuringEntity?.also { requestProcuringEntity ->
            //VR-1.0.1.10.1
            checkProcuringEntityIdentifier(requestProcuringEntity, pnEntity.tender.procuringEntity!!)

            // VR-1.0.1.10.2, VR-1.0.1.10.3, VR-1.0.1.10.6
            checkProcuringEntityPersones(requestProcuringEntity)

            // VR-1.0.1.10.4, VR-1.0.1.10.5
            checkPersonesBusinessFunctions(requestProcuringEntity)

            // VR-1.0.1.10.7
            checkBusinessFunctionPeriod(requestProcuringEntity, context)

            // VR-1.0.1.2.1, VR-1.0.1.2.7, VR-1.0.1.2.8
            checkBusinessFunctionDocuments(requestProcuringEntity)
        }

        // VR-1.0.1.2.7
        checkTenderDocumentsNotEmpty(data.tender)

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
            checkContractPeriodInLotsWhenPNWithoutItemsFromRequest(tenderFromRequest = data.tender)

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
                tenderPeriodEndDate = data.tender.tenderPeriod.endDate,
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
            val lotsIdsFromPN = pnEntity.tender.lots.toSet { it.id }
            checkRelatedLotsInDocumentsFromRequestWhenPNWithItems(
                lotsIdsFromPN = lotsIdsFromPN,
                documentsFromRequest = data.tender.documents
            )
            /** End check Documents */
        }

        check(data, context)
        data.tender.lots.forEach { lot ->
            lot.apply {
                checkOptions()
                checkRecurrence()
                checkRenewal()
            }
        }

        val requireAuction = isAuctionRequired(data.tender.electronicAuctions, data.tender.procurementMethodModalities)

        return CheckedOpenCnOnPn(requireAuction = requireAuction)
    }

    fun create(context: CreateOpenCnOnPnContext, data: OpenCnOnPnRequest): OpenCnOnPnResponse {
        val tenderProcessEntity = tenderRepository.getByCpidAndOcid(context.cpid, context.ocid)
            ?: throw ErrorException(DATA_NOT_FOUND)

        val pnEntity: PNEntity = toObject(PNEntity::class.java, tenderProcessEntity.jsonData)

        val tender: CNEntity.Tender = if (pnEntity.tender.items.isEmpty())
            createTenderBasedPNWithoutItems(datePublished = context.startDate, request = data, pnEntity = pnEntity)
        else
            createTenderBasedPNWithItems(datePublished = context.startDate, request = data, pnEntity = pnEntity)

        val newOcid = generationService.generateOcid(cpid = context.cpid, stage = Stage.EV.key)

        val cnEntity = CNEntity(
            ocid = newOcid.value,
            planning = planning(pnEntity), //BR-3.8.1
            tender = tender,
            relatedProcesses = pnEntity.relatedProcesses
        )

        tenderRepository.save(
            TenderProcessEntity(
                cpId = context.cpid,
                token = tenderProcessEntity.token,
                ocid = newOcid,
                owner = tenderProcessEntity.owner,
                createdDate = context.startDate,
                jsonData = toJson(cnEntity)
            )
        )
        val responseCnEntity = cnEntity.copy(ocid = newOcid.value)

        return getResponse(responseCnEntity, tenderProcessEntity.token)
    }

    private fun OpenCnOnPnRequest.validateTextAttributes() {
        tender.electronicAuctions?.details
            ?.forEachIndexed { detailIdx, detail ->
                detail.id.checkForBlank("tender.electronicAuctions.details[$detailIdx].id")
            }

        tender.criteria
            ?.forEachIndexed { criterionIdx, criterion ->
                criterion.id.checkForBlank("tender.criteria[$criterionIdx].id")
                criterion.title.checkForBlank("tender.criteria[$criterionIdx].title")
                criterion.description.checkForBlank("tender.criteria[$criterionIdx].description")

                criterion.requirementGroups
                    .forEachIndexed { requirementGroupIdx, requirementGroup ->
                        requirementGroup.id.checkForBlank("tender.criteria[$criterionIdx].requirementGroups[$requirementGroupIdx].id")
                        requirementGroup.description.checkForBlank("tender.criteria[$criterionIdx].requirementGroups[$requirementGroupIdx].description")

                        requirementGroup.requirements
                            .forEachIndexed { requirementIdx, requirement ->
                                requirement.id.checkForBlank("tender.criteria[$criterionIdx].requirementGroups[$requirementGroupIdx].requirements[$requirementIdx].id")
                                requirement.title.checkForBlank("tender.criteria[$criterionIdx].requirementGroups[$requirementGroupIdx].requirements[$requirementIdx].title")
                                requirement.description.checkForBlank("tender.criteria[$criterionIdx].requirementGroups[$requirementGroupIdx].requirements[$requirementIdx].description")
                                requirement.value
                                    .also {
                                        if (it is ExpectedValue.AsString)
                                            it.value.checkForBlank("tender.criteria[$criterionIdx].requirementGroups[$requirementGroupIdx].requirements[$requirementIdx].expectedValue")
                                    }
                                requirement.eligibleEvidences
                                    ?.forEachIndexed { eligibleEvidenceIdx, eligibleEvidence ->
                                        eligibleEvidence.id.checkForBlank("tender.criteria[$criterionIdx].requirementGroups[$requirementGroupIdx].requirements[$requirementIdx].eligibleEvidences[$eligibleEvidenceIdx].id")
                                        eligibleEvidence.title.checkForBlank("tender.criteria[$criterionIdx].requirementGroups[$requirementGroupIdx].requirements[$requirementIdx].eligibleEvidences[$eligibleEvidenceIdx].title")
                                        eligibleEvidence.description.checkForBlank("tender.criteria[$criterionIdx].requirementGroups[$requirementGroupIdx].requirements[$requirementIdx].eligibleEvidences[$eligibleEvidenceIdx].description")
                                        eligibleEvidence.relatedDocument?.id.checkForBlank("tender.criteria[$criterionIdx].requirementGroups[$requirementGroupIdx].requirements[$requirementIdx].eligibleEvidences[$eligibleEvidenceIdx].relatedDocument.id")
                                    }
                            }
                    }
            }

        tender.conversions
            ?.forEachIndexed { conversionIdx, conversion ->
                conversion.id.checkForBlank("tender.conversions[$conversionIdx].id")
                conversion.description.checkForBlank("tender.conversions[$conversionIdx].description")
                conversion.relatedItem.checkForBlank("tender.conversions[$conversionIdx].relatedItem")
                conversion.rationale.checkForBlank("tender.conversions[$conversionIdx].rationale")
                conversion.coefficients
                    .forEachIndexed { coefficientIdx, coefficient ->
                        coefficient.id.checkForBlank("tender.conversions[$conversionIdx].coefficients[$coefficientIdx].id")
                        coefficient.relatedOption.checkForBlank("tender.conversions[$conversionIdx].coefficients[$coefficientIdx].relatedOption")
                        coefficient.value.also {
                            if (it is CoefficientValue.AsString)
                                it.value.checkForBlank("tender.conversions[$conversionIdx].coefficients[$coefficientIdx].value")
                        }
                    }
            }

        tender.procuringEntity?.let { procuringEntity ->
            procuringEntity.id.checkForBlank("tender.procuringEntity.id")
            procuringEntity.persones
                ?.forEachIndexed { i, person ->
                    person.title.checkForBlank("tender.procuringEntity.persones[$i].title")
                    person.name.checkForBlank("tender.procuringEntity.persones[$i].name")
                    person.identifier.uri.checkForBlank("tender.procuringEntity.persones[$i].uri")
                    person.identifier.scheme.checkForBlank("tender.procuringEntity.persones[$i].scheme")
                    person.identifier.id.checkForBlank("tender.procuringEntity.persones[$i].id")
                    person.businessFunctions.forEachIndexed { j, businessFunction ->
                        businessFunction.id.checkForBlank("tender.procuringEntity.persones[$i].businessFunctions[$j].id")
                        businessFunction.jobTitle.checkForBlank("tender.procuringEntity.persones[$i].businessFunctions[$j].jobTitle")
                        businessFunction.documents?.forEachIndexed { h, document ->
                            document.title.checkForBlank("tender.procuringEntity.persones[$i].businessFunctions[$j].documents[$h].title")
                            document.description.checkForBlank("tender.procuringEntity.persones[$i].businessFunctions[$j].documents[$h].description")
                        }
                    }
                }
        }

        tender.lots
            .forEachIndexed { lotIdx, lot ->
                lot.id.checkForBlank("tender.lots[$lotIdx].id")
                lot.internalId.checkForBlank("tender.lots[$lotIdx].internalId")
                lot.title.checkForBlank("tender.lots[$lotIdx].title")
                lot.description.checkForBlank("tender.lots[$lotIdx].description")
                lot.options
                    ?.forEachIndexed { optionIdx, option ->
                        option.description.checkForBlank("tender.lots[$lotIdx].options[$optionIdx].description")
                    }
                lot.recurrence?.description.checkForBlank("tender.lots[$lotIdx].recurrence.description")
                lot.renewal?.description.checkForBlank("tender.lots[$lotIdx].renewal.description")

                lot.placeOfPerformance
                    .apply {
                        description.checkForBlank("tender.lots[$lotIdx].placeOfPerformance.description")

                        address.apply {
                            postalCode.checkForBlank("tender.lots[$lotIdx].placeOfPerformance.address.postalCode")
                            streetAddress.checkForBlank("tender.lots[$lotIdx].placeOfPerformance.address.streetAddress")

                            addressDetails.apply {
                                locality.description.checkForBlank("tender.lots[$lotIdx].placeOfPerformance.address.addressDetails.locality.description")
                                locality.id.checkForBlank("tender.lots[$lotIdx].placeOfPerformance.address.addressDetails.locality.id")
                                locality.scheme.checkForBlank("tender.lots[$lotIdx].placeOfPerformance.address.addressDetails.locality.scheme")
                                locality.uri.checkForBlank("tender.lots[$lotIdx].placeOfPerformance.address.addressDetails.locality.uri")
                            }
                        }
                    }
            }

        tender.items
            .forEachIndexed { itemIdx, item ->
                item.id.checkForBlank("tender.items[$itemIdx].id")
                item.internalId.checkForBlank("tender.items[$itemIdx].internalId")
                item.classification.id.checkForBlank("tender.items[$itemIdx].classification.id")
                item.classification.description.checkForBlank("tender.items[$itemIdx].classification.description")
                item.additionalClassifications
                    ?.forEachIndexed { additionalClassificationIdx, additionalClassification ->
                        additionalClassification.id.checkForBlank("tender.items[$itemIdx].additionalClassifications[$additionalClassificationIdx].id")
                        additionalClassification.description.checkForBlank("tender.items[$itemIdx].additionalClassifications[$additionalClassificationIdx].description")
                    }
                item.unit.id.checkForBlank("tender.items[$itemIdx].unit.id")
                item.unit.name.checkForBlank("tender.items[$itemIdx].unit.name")
                item.description.checkForBlank("tender.items[$itemIdx].description")
                item.relatedLot.checkForBlank("tender.items[$itemIdx].relatedLot")
            }

        tender.documents
            .forEachIndexed { documentIdx, document ->
                document.title.checkForBlank("tender.documents[$documentIdx].title")
                document.description.checkForBlank("tender.documents[$documentIdx].description")
                document.relatedLots
                    ?.forEachIndexed { index, relatedLot -> relatedLot.checkForBlank("tender.documents[$documentIdx].relatedLots[$index]") }
            }

        tender.procurementMethodRationale.checkForBlank("tender.procurementMethodRationale")
        tender.procurementMethodAdditionalInfo.checkForBlank("tender.procurementMethodAdditionalInfo")
    }

    private fun String?.checkForBlank(name: String) = this.errorIfBlank {
        ErrorException(
            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
            message = "The attribute '$name' is empty or blank."
        )
    }

    private fun OpenCnOnPnRequest.validateEmptyObject() {
        tender.apply {
            lots.forEachIndexed { idxLot, lot ->
                lot.apply {
                    options
                        ?.forEachIndexed { idxOption, option ->
                            if (option.isEmpty())
                                emptyObjectError("tender.lots[$idxLot].options[$idxOption]")
                            if (option.period != null && option.period.isEmpty())
                                emptyObjectError("tender.lots[$idxLot].options[$idxOption].period")

                        }

                    if (recurrence != null) {
                        if (recurrence.isEmpty())
                            emptyObjectError("tender.lots[$idxLot].recurrence")
                        recurrence.dates
                            ?.forEachIndexed { dateIdx, date ->
                                if (date.startDate == null)
                                    emptyObjectError("tender.lots[$idxLot].recurrence.dates[$dateIdx]")
                            }
                    }

                    if (renewal != null) {
                        if (renewal.isEmpty())
                            emptyObjectError("tender.lots[$idxLot].renewal")
                        if (renewal.period != null && renewal.period.isEmpty())
                            emptyObjectError("tender.lots[$idxLot].renewal.period")
                    }
                }
            }
        }
    }

    private fun emptyObjectError(name: String) {
        throw ErrorException(
            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
            message = "The attribute '$name' is empty object."
        )
    }

    private fun OpenCnOnPnRequest.validateDuplicates() {
        tender.items
            .forEachIndexed { index, item ->
                val duplicate =
                    item.additionalClassifications?.getDuplicate { it.scheme.key + it.id.toUpperCase() }

                if (duplicate != null)
                    throw ErrorException(
                        error = ErrorType.DUPLICATE,
                        message = "Attribute 'tender.items[$index].additionalClassifications' has duplicate by scheme '${duplicate.scheme}' and id '${duplicate.id}'."
                    )
            }

        tender.documents
            .forEachIndexed { index, document ->
                val duplicate = document.relatedLots?.getDuplicate { it }
                if (duplicate != null)
                    throw ErrorException(
                        error = ErrorType.DUPLICATE,
                        message = "Attribute 'tender.documents[$index].relatedLots' has duplicate '$duplicate'."
                    )
            }

        val uniqueEligibleEvidenceIds = mutableSetOf<EligibleEvidenceId>()
        tender.criteria
            ?.forEachIndexed { criterionIdx, criterion ->
                criterion.requirementGroups
                    .forEachIndexed { requirementGroupIdx, requirementGroup ->
                        requirementGroup.requirements
                            .forEachIndexed { requirementIdx, requirement ->
                                requirement.eligibleEvidences
                                    ?.forEachIndexed { eligibleEvidenceIdx, eligibleEvidence ->

                                        // FReq-1.1.1.37
                                        if (!uniqueEligibleEvidenceIds.add(eligibleEvidence.id))
                                            throw ErrorException(
                                                error = ErrorType.DUPLICATE,
                                                message = "Attribute 'tender.criteria[$criterionIdx].requirementGroups[$requirementGroupIdx].requirements[$requirementIdx].eligibleEvidences' has duplicate by id '${eligibleEvidence.id}'."
                                            )
                                    }
                            }
                    }
            }
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
        documentsFromRequest: List<DocumentRequest>,
        documentsFromPN: List<PNEntity.Tender.Document>?
    ) {
        val uniqueIdsDocumentsFromRequest: Set<String> = documentsFromRequest.toSet { it.id }
        if (uniqueIdsDocumentsFromRequest.size != documentsFromRequest.size)
            throw ErrorException(INVALID_DOCS_ID)

        documentsFromPN?.toSet { it.id }
            ?.forEach { id ->
                if (id !in uniqueIdsDocumentsFromRequest) {
                    throw ErrorException(
                        error = INVALID_DOCS_ID,
                        message = "The request is missing a document with id '$id'"
                    )
                }
            }
    }

    private fun checkTenderDocumentsTypes(data: OpenCnOnPnRequest) {
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
     * VR-1.0.1.10.1
     *
     * eAccess compares procuringEntity.ID related to saved PN || CN from DB and procuringEntity.ID from Request:
     * IF [procuringEntity.ID value in DB ==  (equal to) procuringEntity.ID from Request] then: validation is successful; }
     * else {  eAccess throws Exception: "Invalid identifier of procuring entity";}
     *
     */
    private fun checkProcuringEntityIdentifier(
        procuringEntityRequest: OpenCnOnPnRequest.Tender.ProcuringEntity,
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
        procuringEntityRequest: OpenCnOnPnRequest.Tender.ProcuringEntity
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
        procuringEntityRequest: OpenCnOnPnRequest.Tender.ProcuringEntity
    ) {

        procuringEntityRequest.persones
            ?.map { it.businessFunctions }
            ?.forEach { businessfunctions ->
                if (businessfunctions.isEmpty()) throw ErrorException(
                    error = INVALID_PROCURING_ENTITY,
                    message = "At least one businessFunctions detalization should be added. "
                )
                if (businessfunctions.toSet { it.id }.size != businessfunctions.size) throw ErrorException(
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

                    BusinessFunctionType.AUTHORITY -> throw ErrorException(
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
        procuringEntityRequest: OpenCnOnPnRequest.Tender.ProcuringEntity,
        context: CheckOpenCnOnPnContext
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
    private fun checkBusinessFunctionDocuments(procuringEntityRequest: OpenCnOnPnRequest.Tender.ProcuringEntity) {
        procuringEntityRequest.persones
            ?.flatMap { it.businessFunctions }
            ?.forEach { businessFunction ->
                val documents = businessFunction.documents
                if (documents != null) {
                    if (documents.isEmpty())
                        throw ErrorException(
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
        tender: OpenCnOnPnRequest.Tender
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
        lotsFromRequest: List<OpenCnOnPnRequest.Tender.Lot>,
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
        lotsFromRequest: List<OpenCnOnPnRequest.Tender.Lot>,
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
        documentsFromRequest: List<DocumentRequest>
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
    private fun checkContractPeriodInLotsWhenPNWithoutItemsFromRequest(tenderFromRequest: OpenCnOnPnRequest.Tender) {
        val tenderPeriodEndDate = tenderFromRequest.tenderPeriod.endDate
        tenderFromRequest.lots.forEach { lot ->
            checkRangeContractPeriodInLotFromRequest(lot)
            if (lot.contractPeriod.startDate <= tenderPeriodEndDate)
                throw ErrorException(
                    error = INVALID_LOT_CONTRACT_PERIOD,
                    message = "The start date [${lot.contractPeriod.startDate}] of the contract period of the lot [${lot.id}] before or eq that the end date of the tender period [$tenderPeriodEndDate]."
                )
        }
    }

    private fun checkRangeContractPeriodInLotFromRequest(lot: OpenCnOnPnRequest.Tender.Lot) {
        if (lot.contractPeriod.startDate >= lot.contractPeriod.endDate)
            throw ErrorException(INVALID_LOT_CONTRACT_PERIOD)
    }

    /**
     * VR-3.8.9(CN on PN) "Quantity" (item) -> VR-3.6.11(CN)
     *
     * VR-3.6.11 "Quantity" (item)
     * eAccess проверяет, что значению "Quantity" (tender/items/quantity) каждого объекта секции Items больше нуля.
     */
    private fun checkQuantityInItems(itemsFromRequest: List<OpenCnOnPnRequest.Tender.Item>) {
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
        itemsFromRequest: List<OpenCnOnPnRequest.Tender.Item>
    ) {
        if (lotsIdsFromRequest.isEmpty())
            throw ErrorException(ErrorType.EMPTY_LOTS)

        val itemsRelatedLots: Set<String> = itemsFromRequest.toSet { it.relatedLot }
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
        itemsFromRequest: List<OpenCnOnPnRequest.Tender.Item>
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
    private fun checkLotIdFromRequest(lotsFromRequest: List<OpenCnOnPnRequest.Tender.Lot>) {
        val idsAreUniques = lotsFromRequest.isUnique { it.id }
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
    private fun checkItemIdFromRequest(itemsFromRequest: List<OpenCnOnPnRequest.Tender.Item>) {
        val idsAreUniques = itemsFromRequest.isUnique { it.id }
        if (idsAreUniques.not())
            throw throw ErrorException(ITEM_ID_DUPLICATED)
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
        tenderPeriodEndDate: LocalDateTime,
        lotsFromPN: List<PNEntity.Tender.Lot>
    ) {
        lotsFromPN.forEach { lot ->
            if (lot.contractPeriod.startDate <= tenderPeriodEndDate)
                throw ErrorException(
                    error = INVALID_LOT_CONTRACT_PERIOD,
                    message = "The start date [${lot.contractPeriod.startDate}] of the contract period of the lot [${lot.id}] less or eq that the tender period end date [$tenderPeriodEndDate]. "
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
        documentsFromRequest: List<DocumentRequest>
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
        context: CheckOpenCnOnPnContext,
        data: OpenCnOnPnRequest,
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

    private fun isAuctionRequired(
        electronicAuctions: OpenCnOnPnRequest.Tender.ElectronicAuctions?,
        pmm: Set<ProcurementMethodModalities>?
    ): Boolean =
        // VR-1.0.1.7.9
        if (electronicAuctions != null) {
            if (pmm == null || !pmm.contains(ProcurementMethodModalities.ELECTRONIC_AUCTION))
                throw ErrorException(
                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                    message = "Auction sign must be passed"
                )
            true
        } else {      // VR-1.0.1.7.10
            if (pmm != null && pmm.contains(ProcurementMethodModalities.ELECTRONIC_AUCTION))
                throw ErrorException(
                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                    message = "Auction sign must be not passed"
                )
            false
        }

    /** Begin Business Rules */
    private fun createTenderBasedPNWithoutItems(
        datePublished: LocalDateTime,
        request: OpenCnOnPnRequest,
        pnEntity: PNEntity
    ): CNEntity.Tender {
        //BR-3.6.5
        val relatedTemporalWithPermanentLotId: Map<String, String> = generatePermanentLotId(request.tender.lots)
        val relatedTemporalWithPermanentItemId: Map<String, String> = generatePermanentItemId(request.tender.items)

        /** Begin BR-3.8.3 */
        val classification: CNEntity.Tender.Classification =
            classificationFromRequest(classificationFromRequest = request.tender.classification!!)
        val lots: List<CNEntity.Tender.Lot> = convertRequestLots(request.tender, relatedTemporalWithPermanentLotId)
        val items: List<CNEntity.Tender.Item> =
            convertRequestItems(
                request.tender.items,
                relatedTemporalWithPermanentLotId,
                relatedTemporalWithPermanentItemId
            )

        /** End BR-3.8.3 */

        val relatedTemporalWithPermanentRequirementId = generatePermanentRequirementIds(request.tender.criteria)
        val criteria = request.tender.criteria
            ?.map { criterion ->
                buildCriterion(datePublished, criterion, relatedTemporalWithPermanentRequirementId)
                    .replaceTemporalItemId(
                        relatedTemporalWithPermanentLotId = relatedTemporalWithPermanentLotId,
                        relatedTemporalWithPermanentItemId = relatedTemporalWithPermanentItemId
                    )
            }

        val conversions = request.tender.conversions
            ?.map { conversion ->
                buildConversion(conversion, relatedTemporalWithPermanentRequirementId)
            }

        /** Begin BR-3.8.4 */
        //BR-3.8.14 -> BR-3.6.30
        val value: CNEntity.Tender.Value = calculateTenderValueFromLots(request.tender.lots)
        //BR-3.8.15 -> BR-3.6.31
        val contractPeriod: CNEntity.Tender.ContractPeriod = calculationTenderContractPeriod(lots = request.tender.lots)

        /** End BR-3.8.4 */

        //BR-3.8.5 -> BR-3.6.5
        val electronicAuctions: CNEntity.Tender.ElectronicAuctions? = convertElectronicAuctionsFromRequest(
            tenderFromRequest = request.tender,
            relatedTemporalWithPermanentLotId = relatedTemporalWithPermanentLotId
        )

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
            criteria = criteria,
            conversions = conversions,
            lots = lots,
            items = items,
            value = value,
            contractPeriod = contractPeriod,
            electronicAuctions = electronicAuctions,
            updatedDocuments = updatedDocuments
        )
    }

    private fun createTenderBasedPNWithItems(
        datePublished: LocalDateTime,
        request: OpenCnOnPnRequest,
        pnEntity: PNEntity
    ): CNEntity.Tender {
        /** Begin BR-3.8.3 */
        val classification: CNEntity.Tender.Classification =
            classificationFromPNToCN(classificationFromPN = pnEntity.tender.classification)
        val lots: List<CNEntity.Tender.Lot> = lotsFromPNToCN(lotsFromPN = pnEntity.tender.lots)
        val items: List<CNEntity.Tender.Item> = itemsFromPNToCN(itemsFromPN = pnEntity.tender.items)

        /** End BR-3.8.3 */

        val relatedTemporalWithPermanentRequirementId = generatePermanentRequirementIds(request.tender.criteria)
        val criteria = request.tender.criteria
            ?.map { criterion ->
                buildCriterion(datePublished, criterion, relatedTemporalWithPermanentRequirementId)
            }

        val conversions = request.tender.conversions
            ?.map { conversion ->
                buildConversion(conversion, relatedTemporalWithPermanentRequirementId)
            }

        /** Begin BR-3.8.4 */
        val value: CNEntity.Tender.Value = pnEntity.tender.value
            .let {
                CNEntity.Tender.Value(
                    amount = it.amount,
                    currency = it.currency
                )
            }
        val contractPeriod: CNEntity.Tender.ContractPeriod = pnEntity.tender.contractPeriod!!
            .let {
                CNEntity.Tender.ContractPeriod(
                    startDate = it.startDate,
                    endDate = it.endDate
                )
            }

        /** End BR-3.8.4 */

        //BR-3.8.5 -> BR-3.6.5
        val electronicAuctions: CNEntity.Tender.ElectronicAuctions? = convertElectronicAuctionsFromRequest(
            tenderFromRequest = request.tender
        )

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
            criteria = criteria,
            conversions = conversions,
            lots = lots,
            items = items,
            value = value,
            contractPeriod = contractPeriod,
            electronicAuctions = electronicAuctions,
            updatedDocuments = updatedDocuments
        )
    }

    private fun planning(pnEntity: PNEntity): CNEntity.Planning {
        return CNEntity.Planning(
            rationale = pnEntity.planning.rationale,
            budget = CNEntity.Planning.Budget(
                description = pnEntity.planning.budget.description,
                amount = pnEntity.planning.budget.amount
                    .let {
                        CNEntity.Planning.Budget.Amount(
                            amount = it.amount,
                            currency = it.currency
                        )
                    },
                isEuropeanUnionFunded = pnEntity.planning.budget.isEuropeanUnionFunded,
                budgetBreakdowns = pnEntity.planning.budget.budgetBreakdowns
                    .map { budgetBreakdown ->
                        CNEntity.Planning.Budget.BudgetBreakdown(
                            id = budgetBreakdown.id,
                            description = budgetBreakdown.description,
                            amount = budgetBreakdown.amount
                                .let {
                                    CNEntity.Planning.Budget.BudgetBreakdown.Amount(
                                        amount = it.amount,
                                        currency = it.currency
                                    )
                                },
                            period = budgetBreakdown.period
                                .let {
                                    CNEntity.Planning.Budget.BudgetBreakdown.Period(
                                        startDate = it.startDate,
                                        endDate = it.endDate
                                    )
                                },
                            sourceParty = budgetBreakdown.sourceParty
                                .let {
                                    CNEntity.Planning.Budget.BudgetBreakdown.SourceParty(
                                        name = it.name,
                                        id = it.id
                                    )
                                },
                            europeanUnionFunding = budgetBreakdown.europeanUnionFunding
                                ?.let {
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
        request: OpenCnOnPnRequest,
        pnEntity: PNEntity,
        classification: CNEntity.Tender.Classification,
        criteria: List<CNEntity.Tender.Criteria>?,
        conversions: List<CNEntity.Tender.Conversion>?,
        lots: List<CNEntity.Tender.Lot>,
        items: List<CNEntity.Tender.Item>,
        value: CNEntity.Tender.Value,
        contractPeriod: CNEntity.Tender.ContractPeriod,
        electronicAuctions: CNEntity.Tender.ElectronicAuctions?,
        updatedDocuments: List<CNEntity.Tender.Document>
    ): CNEntity.Tender {
        /** Begin BR-3.8.8(CN on PN) Status StatusDetails (tender) -> BR-3.6.2(CN)*/
        val status = TenderStatus.ACTIVE
        val statusDetails: TenderStatusDetails = TenderStatusDetails.CLARIFICATION

        /** End BR-3.8.8(CN on PN) Status StatusDetails (tender) -> BR-3.6.2(CN)*/

        val awardCriteriaDetails = if (request.tender.awardCriteria == AwardCriteria.PRICE_ONLY)
            AwardCriteriaDetails.AUTOMATED
        else
            request.tender.awardCriteriaDetails

        return CNEntity.Tender(
            id = generationService.generatePermanentTenderId(),
            /** Begin BR-3.8.8 -> BR-3.6.2*/
            status = status,
            statusDetails = statusDetails,
            /** End BR-3.8.8 -> BR-3.6.2*/

            classification = classification,
            title = pnEntity.tender.title, //BR-3.8.1
            description = pnEntity.tender.description, //BR-3.8.1
            //BR-3.8.1
            acceleratedProcedure = pnEntity.tender.acceleratedProcedure
                .let {
                    CNEntity.Tender.AcceleratedProcedure(isAcceleratedProcedure = it.isAcceleratedProcedure)
                },
            //BR-3.8.1
            designContest = pnEntity.tender.designContest
                .let {
                    CNEntity.Tender.DesignContest(serviceContractAward = it.serviceContractAward)
                },
            //BR-3.8.1
            electronicWorkflows = pnEntity.tender.electronicWorkflows
                .let {
                    CNEntity.Tender.ElectronicWorkflows(
                        useOrdering = it.useOrdering,
                        usePayment = it.usePayment,
                        acceptInvoicing = it.acceptInvoicing
                    )
                },
            //BR-3.8.1
            jointProcurement = pnEntity.tender.jointProcurement
                .let {
                    CNEntity.Tender.JointProcurement(isJointProcurement = it.isJointProcurement)
                },
            //BR-3.8.1
            procedureOutsourcing = pnEntity.tender.procedureOutsourcing
                .let {
                    CNEntity.Tender.ProcedureOutsourcing(procedureOutsourced = it.procedureOutsourced)
                },
            //BR-3.8.1
            framework = pnEntity.tender.framework
                .let {
                    CNEntity.Tender.Framework(isAFramework = it.isAFramework)
                },
            //BR-3.8.1
            dynamicPurchasingSystem = pnEntity.tender.dynamicPurchasingSystem
                .let {
                    CNEntity.Tender.DynamicPurchasingSystem(hasDynamicPurchasingSystem = it.hasDynamicPurchasingSystem)
                },
            legalBasis = pnEntity.tender.legalBasis, //BR-3.8.1
            procurementMethod = pnEntity.tender.procurementMethod, //BR-3.8.1
            procurementMethodDetails = pnEntity.tender.procurementMethodDetails,//BR-3.8.1
            procurementMethodRationale = request.tender.procurementMethodRationale,
            procurementMethodAdditionalInfo = request.tender.procurementMethodAdditionalInfo,
            mainProcurementCategory = pnEntity.tender.mainProcurementCategory, //BR-3.8.1

            eligibilityCriteria = pnEntity.tender.eligibilityCriteria, //BR-3.8.1

            //BR-3.8.17 -> BR-3.6.22 | VR-3.6.16
            awardCriteria = request.tender.awardCriteria,
            awardCriteriaDetails = awardCriteriaDetails,
            tenderPeriod = request.tender.tenderPeriod
                .let { period ->
                    CNEntity.Tender.TenderPeriod(startDate = period.startDate, endDate = period.endDate)
                },
            contractPeriod = contractPeriod,
            enquiryPeriod = request.tender.enquiryPeriod
                .let { period ->
                    CNEntity.Tender.EnquiryPeriod(startDate = period.startDate, endDate = period.endDate)
                },
            procurementMethodModalities = request.tender.procurementMethodModalities,
            electronicAuctions = electronicAuctions, //BR-3.8.5 -> BR-3.6.5
            //BR-3.8.1
            procuringEntity = pnEntity.tender.procuringEntity!!
                .let { procuringEntity ->
                    CNEntity.Tender.ProcuringEntity(
                        id = procuringEntity.id,
                        name = procuringEntity.name,
                        identifier = procuringEntity.identifier
                            .let { identifier ->
                                CNEntity.Tender.ProcuringEntity.Identifier(
                                    scheme = identifier.scheme,
                                    id = identifier.id,
                                    legalName = identifier.legalName,
                                    uri = identifier.uri
                                )
                            },
                        additionalIdentifiers = procuringEntity.additionalIdentifiers
                            ?.map { additionalIdentifier ->
                                CNEntity.Tender.ProcuringEntity.AdditionalIdentifier(
                                    scheme = additionalIdentifier.scheme,
                                    id = additionalIdentifier.id,
                                    legalName = additionalIdentifier.legalName,
                                    uri = additionalIdentifier.uri
                                )
                            },
                        address = procuringEntity.address
                            .let { address ->
                                CNEntity.Tender.ProcuringEntity.Address(
                                    streetAddress = address.streetAddress,
                                    postalCode = address.postalCode,
                                    addressDetails = address.addressDetails
                                        .let { addressDetails ->
                                            CNEntity.Tender.ProcuringEntity.Address.AddressDetails(
                                                country = addressDetails.country
                                                    .let { country ->
                                                        CNEntity.Tender.ProcuringEntity.Address.AddressDetails.Country(
                                                            scheme = country.scheme,
                                                            id = country.id,
                                                            description = country.description,
                                                            uri = country.uri
                                                        )
                                                    },
                                                region = addressDetails.region
                                                    .let { region ->
                                                        CNEntity.Tender.ProcuringEntity.Address.AddressDetails.Region(
                                                            scheme = region.scheme,
                                                            id = region.id,
                                                            description = region.description,
                                                            uri = region.uri
                                                        )
                                                    },
                                                locality = addressDetails.locality
                                                    .let { locality ->
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
                        contactPoint = procuringEntity.contactPoint
                            .let { contactPoint ->
                                CNEntity.Tender.ProcuringEntity.ContactPoint(
                                    name = contactPoint.name,
                                    email = contactPoint.email,
                                    telephone = contactPoint.telephone,
                                    faxNumber = contactPoint.faxNumber,
                                    url = contactPoint.url
                                )
                            },
                        persones = request.tender.procuringEntity
                            ?.let { _procuringEntity ->
                                _procuringEntity.persones
                                    ?.map { person ->
                                        CNEntity.Tender.ProcuringEntity.Persone(
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
                                            businessFunctions = person.businessFunctions
                                                .map { businessFunction ->
                                                    CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction(
                                                        id = businessFunction.id,
                                                        jobTitle = businessFunction.jobTitle,
                                                        type = businessFunction.type,
                                                        period = CNEntity.Tender.ProcuringEntity.Persone.BusinessFunction.Period(
                                                            startDate = businessFunction.period.startDate
                                                        ),
                                                        documents = businessFunction.documents
                                                            ?.map { document ->
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
                                    }
                            }
                    )
                },
            value = value,
            //BR-3.8.1
            lotGroups = pnEntity.tender
                .lotGroups
                .map {
                    CNEntity.Tender.LotGroup(
                        optionToCombine = it.optionToCombine
                    )
                },
            requiresElectronicCatalogue = pnEntity.tender.requiresElectronicCatalogue,
            criteria = criteria,
            conversions = conversions,
            lots = lots, //BR-3.8.3
            items = items, //BR-3.8.3
            submissionMethod = pnEntity.tender.submissionMethod, //BR-3.8.1
            submissionMethodRationale = pnEntity.tender.submissionMethodRationale, //BR-3.8.1
            submissionMethodDetails = pnEntity.tender.submissionMethodDetails, //BR-3.8.1
            documents = updatedDocuments, //BR-3.7.13
            additionalProcurementCategories = request.tender.additionalProcurementCategories
        )
    }

    /**
     * BR-3.8.5(CN on PN) lot id (tender.lots.id) -> BR-3.6.5
     *
     * eAccess меняет временные "ID" (tender/lot/id) лотов на постоянные.
     * Постоянные "ID" (tender/lot/id) лотов формируются как уникальные для данного контрактного процесса
     * 32-символьные идентификаторы.
     */
    private fun generatePermanentLotId(lots: List<OpenCnOnPnRequest.Tender.Lot>): Map<String, String> {
        return lots.asSequence()
            .map { lot ->
                val permanentId = generationService.generatePermanentLotId() //BR-3.8.6
                lot.id to permanentId
            }
            .toMap()
    }

    private fun generatePermanentItemId(itemsFromRequest: List<OpenCnOnPnRequest.Tender.Item>): Map<String, String> {
        return itemsFromRequest.asSequence()
            .map { item ->
                val permanentId = generationService.generatePermanentItemId()
                item.id to permanentId
            }
            .toMap()
    }

    private fun updateDocuments(
        documentsFromRequest: List<DocumentRequest>,
        documentsFromDB: List<PNEntity.Tender.Document>,
        relatedTemporalWithPermanentLotId: Map<String, String>
    ): List<CNEntity.Tender.Document> {
        return if (documentsFromDB.isNotEmpty()) {
            val documentsFromRequestById: Map<String, DocumentRequest> =
                documentsFromRequest.associateBy { document -> document.id }
            val existsDocumentsById: Map<String, PNEntity.Tender.Document> =
                documentsFromDB.associateBy { document -> document.id }

            val updatedDocuments: Set<CNEntity.Tender.Document> = updateExistsDocuments(
                documentsFromRequestById = documentsFromRequestById,
                existsDocumentsById = existsDocumentsById,
                relatedTemporalWithPermanentLotId = relatedTemporalWithPermanentLotId
            )

            val newDocumentsFromRequest: Set<DocumentRequest> = extractNewDocuments(
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
        documentsFromRequestById: Map<String, DocumentRequest>,
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
        documentsFromRequest: Collection<DocumentRequest>,
        existsDocumentsById: Map<String, PNEntity.Tender.Document>
    ): Set<DocumentRequest> {
        return documentsFromRequest.asSequence()
            .filter { document -> !existsDocumentsById.containsKey(document.id) }
            .toSet()
    }

    private fun convertNewDocuments(
        newDocumentsFromRequest: Collection<DocumentRequest>,
        relatedTemporalWithPermanentLotId: Map<String, String>
    ): List<CNEntity.Tender.Document> {
        return newDocumentsFromRequest.map { document ->
            convertNewDocument(document, relatedTemporalWithPermanentLotId)
        }
    }

    private fun convertNewDocument(
        newDocumentFromRequest: DocumentRequest,
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
        itemsFromRequest: List<OpenCnOnPnRequest.Tender.Item>,
        relatedTemporalWithPermanentLotId: Map<String, String>,
        relatedTemporalWithPermanentItemId: Map<String, String>
    ): List<CNEntity.Tender.Item> {
        return itemsFromRequest.map { item ->
            CNEntity.Tender.Item(
                //BR-3.8.6(CN on PN) item id (tender.items.id) -> BR-3.6.6
                internalId = item.internalId,
                id = relatedTemporalWithPermanentItemId.getValue(item.id),
                description = item.description,
                classification = item.classification.let { classification ->
                    CNEntity.Tender.Item.Classification(
                        scheme = classification.scheme,
                        id = classification.id,
                        description = classification.description,
                        uri = null
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
        tender: OpenCnOnPnRequest.Tender,
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
                },
                hasOptions = lot.hasOptions ?: false,        // BR-1.0.1.3.9
                hasRecurrence = lot.hasRecurrence ?: false,  // BR-1.0.1.3.10
                hasRenewal = lot.hasRenewal ?: false,        // BR-1.0.1.3.11
                options = lot.options.orEmpty().map { option ->
                    CNEntity.Tender.Lot.Option(
                        hasOptions = null,
                        description = option.description,
                        period = option.period?.let { period ->
                            CNEntity.Tender.Lot.Period(
                                startDate = period.startDate,
                                endDate = period.endDate,
                                maxExtentDate = period.maxExtentDate,
                                durationInDays = period.durationInDays
                            )
                        }
                    )
                },
                recurrence = lot.recurrence?.let {  recurrence ->
                    CNEntity.Tender.Lot.Recurrence(
                        description = recurrence.description,
                        dates = recurrence.dates?.map { date ->
                            CNEntity.Tender.Lot.Recurrence.Date(
                                startDate = date.startDate
                            )
                        }
                    )
                },
                renewal = lot.renewal?.let { renewal ->
                    CNEntity.Tender.Lot.RenewalV2(
                        description = renewal.description,
                        minimumRenewals = renewal.minimumRenewals,
                        maximumRenewals = renewal.maximumRenewals,
                        period = renewal.period?.let { period ->
                            CNEntity.Tender.Lot.Period(
                                startDate = period.startDate,
                                endDate = period.endDate,
                                maxExtentDate = period.maxExtentDate,
                                durationInDays = period.durationInDays
                            )
                        }
                    )
                }
            )
        }
    }

    private fun convertElectronicAuctionsFromRequest(
        tenderFromRequest: OpenCnOnPnRequest.Tender,
        relatedTemporalWithPermanentLotId: Map<String, String> = emptyMap()
    ): CNEntity.Tender.ElectronicAuctions? {
        return tenderFromRequest.electronicAuctions?.let {
            val details = it.details.map { detail ->
                CNEntity.Tender.ElectronicAuctions.Detail(
                    id = generationService.generatePermanentAuctionId(),
                    relatedLot = if (relatedTemporalWithPermanentLotId.isNotEmpty())
                        relatedTemporalWithPermanentLotId.getValue(detail.relatedLot) //BR-3.8.6(CN on PN) -> BR-3.6.5(CN)
                    else
                        detail.relatedLot,
                    electronicAuctionModalities = detail.electronicAuctionModalities.map { electronicAuctionModalities ->
                        CNEntity.Tender.ElectronicAuctions.Detail.Modalities(
                            eligibleMinimumDifference = electronicAuctionModalities.eligibleMinimumDifference.let { eligibleMinimumDifference ->
                                CNEntity.Tender.ElectronicAuctions.Detail.Modalities.EligibleMinimumDifference(
                                    amount = eligibleMinimumDifference.amount,
                                    currency = eligibleMinimumDifference.currency
                                )
                            }
                        )
                    }
                )
            }

            CNEntity.Tender.ElectronicAuctions(
                details = details
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
    private fun calculateTenderValueFromLots(lotsFromRequest: List<OpenCnOnPnRequest.Tender.Lot>): CNEntity.Tender.Value {
        val currency = lotsFromRequest.elementAt(0).value.currency
        val totalAmount = lotsFromRequest.fold(BigDecimal.ZERO) { acc, lot ->
            acc.plus(lot.value.amount)
        }.setScale(2, RoundingMode.HALF_UP)
        return CNEntity.Tender.Value(totalAmount, currency)
    }

    private fun calculationTenderContractPeriod(lots: List<OpenCnOnPnRequest.Tender.Lot>): CNEntity.Tender.ContractPeriod {
        val contractPeriodSet = lots.asSequence().map { it.contractPeriod }.toSet()
        val startDate = contractPeriodSet.minBy { it.startDate }!!.startDate
        val endDate = contractPeriodSet.maxBy { it.endDate }!!.endDate
        return CNEntity.Tender.ContractPeriod(startDate, endDate)
    }

    /**
     * BR-3.8.3
     */
    private fun classificationFromRequest(
        classificationFromRequest: OpenCnOnPnRequest.Tender.Classification
    ): CNEntity.Tender.Classification {
        return classificationFromRequest.let {
            CNEntity.Tender.Classification(
                scheme = it.scheme,
                id = it.id,
                description = it.description,
                uri = null
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
                description = it.description,
                uri = null
            )
        }
    }

    /**
     * BR-3.8.3
     */
    private fun lotsFromPNToCN(lotsFromPN: List<PNEntity.Tender.Lot>): List<CNEntity.Tender.Lot> {
        return lotsFromPN
            .filter { lot -> lot.status == LotStatus.PLANNING }
            .map { lot ->
                CNEntity.Tender.Lot(
                    //BR-3.8.5
                    id = lot.id,

                    internalId = lot.internalId,
                    title = lot.title,
                    description = lot.description,
                    /** Begin BR-3.8.7 */
                    status = LotStatus.ACTIVE,
                    statusDetails = LotStatusDetails.EMPTY,
                    /** End BR-3.8.7 */
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
                    },
                    hasOptions = false,
                    options = emptyList(),
                    hasRenewal = false,
                    renewal = null,
                    hasRecurrence = false,
                    recurrence = null
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
                internalId = item.internalId,
                description = item.description,
                classification = item.classification.let { classification ->
                    CNEntity.Tender.Item.Classification(
                        scheme = classification.scheme,
                        id = classification.id,
                        description = classification.description,
                        uri = null
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

    private fun getResponse(cn: CNEntity, token: UUID): OpenCnOnPnResponse {
        return OpenCnOnPnResponse(
            ocid = cn.ocid,
            token = token.toString(),
            planning = cn.planning.let { planning ->
                OpenCnOnPnResponse.Planning(
                    rationale = planning.rationale,
                    budget = planning.budget.let { budget ->
                        OpenCnOnPnResponse.Planning.Budget(
                            description = budget.description,
                            amount = budget.amount.let { amount ->
                                OpenCnOnPnResponse.Planning.Budget.Amount(
                                    amount = amount.amount,
                                    currency = amount.currency
                                )
                            },
                            isEuropeanUnionFunded = budget.isEuropeanUnionFunded,
                            budgetBreakdowns = budget.budgetBreakdowns.map { budgetBreakdown ->
                                OpenCnOnPnResponse.Planning.Budget.BudgetBreakdown(
                                    id = budgetBreakdown.id,
                                    description = budgetBreakdown.description,
                                    amount = budgetBreakdown.amount.let { amount ->
                                        OpenCnOnPnResponse.Planning.Budget.BudgetBreakdown.Amount(
                                            amount = amount.amount,
                                            currency = amount.currency
                                        )
                                    },
                                    period = budgetBreakdown.period.let { period ->
                                        OpenCnOnPnResponse.Planning.Budget.BudgetBreakdown.Period(
                                            startDate = period.startDate,
                                            endDate = period.endDate
                                        )
                                    },
                                    sourceParty = budgetBreakdown.sourceParty.let { sourceParty ->
                                        OpenCnOnPnResponse.Planning.Budget.BudgetBreakdown.SourceParty(
                                            id = sourceParty.id,
                                            name = sourceParty.name
                                        )
                                    },
                                    europeanUnionFunding = budgetBreakdown.europeanUnionFunding?.let { europeanUnionFunding ->
                                        OpenCnOnPnResponse.Planning.Budget.BudgetBreakdown.EuropeanUnionFunding(
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
                OpenCnOnPnResponse.Tender(
                    id = tender.id,
                    status = tender.status,
                    statusDetails = tender.statusDetails,
                    title = tender.title,
                    description = tender.description,
                    classification = tender.classification.let { classification ->
                        OpenCnOnPnResponse.Tender.Classification(
                            scheme = classification.scheme,
                            id = classification.id,
                            description = classification.description
                        )
                    },
                    requiresElectronicCatalogue = tender.requiresElectronicCatalogue,
                    tenderPeriod = tender.tenderPeriod.let { tenderPeriod ->
                        OpenCnOnPnResponse.Tender.TenderPeriod(
                            startDate = tenderPeriod!!.startDate,
                            endDate = tenderPeriod.endDate
                        )
                    },
                    enquiryPeriod = tender.enquiryPeriod.let { enquiryPeriod ->
                        OpenCnOnPnResponse.Tender.EnquiryPeriod(
                            startDate = enquiryPeriod!!.startDate,
                            endDate = enquiryPeriod.endDate
                        )
                    },
                    acceleratedProcedure = tender.acceleratedProcedure.let { acceleratedProcedure ->
                        OpenCnOnPnResponse.Tender.AcceleratedProcedure(
                            isAcceleratedProcedure = acceleratedProcedure.isAcceleratedProcedure
                        )
                    },
                    designContest = tender.designContest.let { designContest ->
                        OpenCnOnPnResponse.Tender.DesignContest(
                            serviceContractAward = designContest.serviceContractAward
                        )
                    },
                    electronicWorkflows = tender.electronicWorkflows.let { electronicWorkflows ->
                        OpenCnOnPnResponse.Tender.ElectronicWorkflows(
                            useOrdering = electronicWorkflows.useOrdering,
                            usePayment = electronicWorkflows.usePayment,
                            acceptInvoicing = electronicWorkflows.acceptInvoicing
                        )
                    },
                    jointProcurement = tender.jointProcurement.let { jointProcurement ->
                        OpenCnOnPnResponse.Tender.JointProcurement(
                            isJointProcurement = jointProcurement.isJointProcurement
                        )
                    },
                    procedureOutsourcing = tender.procedureOutsourcing.let { procedureOutsourcing ->
                        OpenCnOnPnResponse.Tender.ProcedureOutsourcing(
                            procedureOutsourced = procedureOutsourcing.procedureOutsourced
                        )
                    },
                    framework = tender.framework.let { framework ->
                        OpenCnOnPnResponse.Tender.Framework(
                            isAFramework = framework.isAFramework
                        )
                    },
                    dynamicPurchasingSystem = tender.dynamicPurchasingSystem.let { dynamicPurchasingSystem ->
                        OpenCnOnPnResponse.Tender.DynamicPurchasingSystem(
                            hasDynamicPurchasingSystem = dynamicPurchasingSystem.hasDynamicPurchasingSystem
                        )
                    },
                    legalBasis = tender.legalBasis,
                    procurementMethod = tender.procurementMethod,
                    procurementMethodDetails = tender.procurementMethodDetails,
                    procurementMethodRationale = tender.procurementMethodRationale,
                    procurementMethodAdditionalInfo = tender.procurementMethodAdditionalInfo,
                    additionalProcurementCategories = tender.additionalProcurementCategories,
                    mainProcurementCategory = tender.mainProcurementCategory,
                    eligibilityCriteria = tender.eligibilityCriteria,
                    contractPeriod = tender.contractPeriod?.let { contractPeriod ->
                        OpenCnOnPnResponse.Tender.ContractPeriod(
                            startDate = contractPeriod.startDate,
                            endDate = contractPeriod.endDate
                        )
                    },
                    procurementMethodModalities = tender.procurementMethodModalities,
                    electronicAuctions = tender.electronicAuctions?.let { electronicAuctions ->
                        OpenCnOnPnResponse.Tender.ElectronicAuctions(
                            details = electronicAuctions.details.map { detail ->
                                OpenCnOnPnResponse.Tender.ElectronicAuctions.Detail(
                                    id = detail.id,
                                    relatedLot = detail.relatedLot,
                                    electronicAuctionModalities = detail.electronicAuctionModalities.map { modality ->
                                        OpenCnOnPnResponse.Tender.ElectronicAuctions.Detail.Modalities(
                                            eligibleMinimumDifference = modality.eligibleMinimumDifference.let { emd ->
                                                OpenCnOnPnResponse.Tender.ElectronicAuctions.Detail.Modalities.EligibleMinimumDifference(
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
                        OpenCnOnPnResponse.Tender.ProcuringEntity(
                            id = procuringEntity.id,
                            name = procuringEntity.name,
                            identifier = procuringEntity.identifier.let { identifier ->
                                OpenCnOnPnResponse.Tender.ProcuringEntity.Identifier(
                                    scheme = identifier.scheme,
                                    id = identifier.id,
                                    legalName = identifier.legalName,
                                    uri = identifier.uri
                                )
                            },
                            additionalIdentifiers = procuringEntity.additionalIdentifiers?.map { additionalIdentifier ->
                                OpenCnOnPnResponse.Tender.ProcuringEntity.AdditionalIdentifier(
                                    scheme = additionalIdentifier.scheme,
                                    id = additionalIdentifier.id,
                                    legalName = additionalIdentifier.legalName,
                                    uri = additionalIdentifier.uri
                                )
                            },
                            address = procuringEntity.address.let { address ->
                                OpenCnOnPnResponse.Tender.ProcuringEntity.Address(
                                    streetAddress = address.streetAddress,
                                    postalCode = address.postalCode,
                                    addressDetails = address.addressDetails.let { addressDetails ->
                                        OpenCnOnPnResponse.Tender.ProcuringEntity.Address.AddressDetails(
                                            country = addressDetails.country.let { country ->
                                                OpenCnOnPnResponse.Tender.ProcuringEntity.Address.AddressDetails.Country(
                                                    scheme = country.scheme,
                                                    id = country.id,
                                                    description = country.description,
                                                    uri = country.uri
                                                )
                                            },
                                            region = addressDetails.region.let { region ->
                                                OpenCnOnPnResponse.Tender.ProcuringEntity.Address.AddressDetails.Region(
                                                    scheme = region.scheme,
                                                    id = region.id,
                                                    description = region.description,
                                                    uri = region.uri
                                                )
                                            },
                                            locality = addressDetails.locality.let { locality ->
                                                OpenCnOnPnResponse.Tender.ProcuringEntity.Address.AddressDetails.Locality(
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
                                OpenCnOnPnResponse.Tender.ProcuringEntity.ContactPoint(
                                    name = contactPoint.name,
                                    email = contactPoint.email,
                                    telephone = contactPoint.telephone,
                                    faxNumber = contactPoint.faxNumber,
                                    url = contactPoint.url
                                )
                            },
                            persones = procuringEntity.persones?.map { persone ->
                                OpenCnOnPnResponse.Tender.ProcuringEntity.Persone(
                                    id = persone.id,
                                    name = persone.name,
                                    title = persone.title,
                                    identifier = OpenCnOnPnResponse.Tender.ProcuringEntity.Persone.Identifier(
                                        id = persone.identifier.id,
                                        scheme = persone.identifier.scheme,
                                        uri = persone.identifier.uri
                                    ),
                                    businessFunctions = persone.businessFunctions.map { businessFunction ->
                                        OpenCnOnPnResponse.Tender.ProcuringEntity.Persone.BusinessFunction(
                                            id = businessFunction.id,
                                            type = businessFunction.type,
                                            jobTitle = businessFunction.jobTitle,
                                            period = OpenCnOnPnResponse.Tender.ProcuringEntity.Persone.BusinessFunction.Period(
                                                startDate = businessFunction.period.startDate
                                            ),
                                            documents = businessFunction.documents?.map { document ->
                                                OpenCnOnPnResponse.Tender.ProcuringEntity.Persone.BusinessFunction.Document(
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
                        OpenCnOnPnResponse.Tender.Value(
                            amount = value.amount,
                            currency = value.currency
                        )
                    },
                    lotGroups = tender.lotGroups.map { lotGroup ->
                        OpenCnOnPnResponse.Tender.LotGroup(
                            optionToCombine = lotGroup.optionToCombine
                        )
                    },
                    criteria = tender.criteria?.map { criterion ->
                        OpenCnOnPnResponse.Tender.Criteria(
                            id = criterion.id,
                            title = criterion.title,
                            description = criterion.description,
                            classification = criterion.classification
                                .let { classification ->
                                    CriterionClassificationResponse(
                                        id = classification.id,
                                        scheme = classification.scheme
                                    )
                                },
                            source = criterion.source,
                            requirementGroups = criterion.requirementGroups.map {
                                OpenCnOnPnResponse.Tender.Criteria.RequirementGroup(
                                    id = it.id,
                                    description = it.description,
                                    requirements = it.requirements.map { requirement ->
                                        Requirement(
                                            id = requirement.id,
                                            description = requirement.description,
                                            title = requirement.title,
                                            period = requirement.period?.let { period ->
                                                Requirement.Period(
                                                    startDate = period.startDate,
                                                    endDate = period.endDate
                                                )
                                            },
                                            dataType = requirement.dataType,
                                            value = requirement.value,
                                            eligibleEvidences = requirement.eligibleEvidences?.toList(),
                                            status = requirement.status,
                                            datePublished = requirement.datePublished
                                        )
                                    }
                                )
                            },
                            relatesTo = criterion.relatesTo,
                            relatedItem = criterion.relatedItem
                        )
                    },
                    conversions = tender.conversions?.map { conversion ->
                        OpenCnOnPnResponse.Tender.Conversion(
                            id = conversion.id,
                            relatedItem = conversion.relatedItem,
                            relatesTo = conversion.relatesTo,
                            rationale = conversion.rationale,
                            description = conversion.description,
                            coefficients = conversion.coefficients.map { coefficient ->
                                OpenCnOnPnResponse.Tender.Conversion.Coefficient(
                                    id = coefficient.id,
                                    value = coefficient.value,
                                    coefficient = coefficient.coefficient
                                )
                            }
                        )
                    },
                    lots = tender.lots.map { lot ->
                        OpenCnOnPnResponse.Tender.Lot(
                            id = lot.id,
                            internalId = lot.internalId,
                            title = lot.title,
                            description = lot.description,
                            status = lot.status,
                            statusDetails = lot.statusDetails,
                            value = lot.value.let { value ->
                                OpenCnOnPnResponse.Tender.Lot.Value(
                                    amount = value.amount,
                                    currency = value.currency
                                )
                            },
                            hasOptions = lot.hasOptions,
                            options = lot.options.map { option ->
                                OpenCnOnPnResponse.Tender.Lot.Option(
                                    description = option.description,
                                    period = option.period?.let { period ->
                                        OpenCnOnPnResponse.Tender.Lot.Period(
                                            startDate = period.startDate,
                                            endDate = period.endDate,
                                            maxExtentDate = period.maxExtentDate,
                                            durationInDays = period.durationInDays
                                        )
                                    }
                                )
                            },
                            hasRecurrence = lot.hasRecurrence,
                            recurrence = lot.recurrence?.let { recurrence ->
                                OpenCnOnPnResponse.Tender.Lot.Recurrence(
                                    description = recurrence.description,
                                    dates = recurrence.dates?.map { date ->
                                        OpenCnOnPnResponse.Tender.Lot.Recurrence.Date(
                                            startDate = date.startDate
                                        )
                                    }
                                )
                            },
                            hasRenewal = lot.hasRenewal,
                            renewal = lot.renewal?.let { renewalV2 ->
                                OpenCnOnPnResponse.Tender.Lot.Renewal(
                                    description = renewalV2.description,
                                    minimumRenewals = renewalV2.minimumRenewals,
                                    maximumRenewals = renewalV2.maximumRenewals,
                                    period = renewalV2.period?.let { period ->
                                        OpenCnOnPnResponse.Tender.Lot.Period(
                                            startDate = period.startDate,
                                            endDate = period.endDate,
                                            maxExtentDate = period.maxExtentDate,
                                            durationInDays = period.durationInDays
                                        )
                                    }
                                )
                            },
                            contractPeriod = lot.contractPeriod.let { contractPeriod ->
                                OpenCnOnPnResponse.Tender.Lot.ContractPeriod(
                                    startDate = contractPeriod.startDate,
                                    endDate = contractPeriod.endDate
                                )
                            },
                            placeOfPerformance = lot.placeOfPerformance.let { placeOfPerformance ->
                                OpenCnOnPnResponse.Tender.Lot.PlaceOfPerformance(
                                    description = placeOfPerformance.description,
                                    address = placeOfPerformance.address.let { address ->
                                        OpenCnOnPnResponse.Tender.Lot.PlaceOfPerformance.Address(
                                            streetAddress = address.streetAddress,
                                            postalCode = address.postalCode,
                                            addressDetails = address.addressDetails.let { addressDetails ->
                                                OpenCnOnPnResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                                    country = addressDetails.country.let { country ->
                                                        OpenCnOnPnResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                                            scheme = country.scheme,
                                                            id = country.id,
                                                            description = country.description,
                                                            uri = country.uri
                                                        )
                                                    },
                                                    region = addressDetails.region.let { region ->
                                                        OpenCnOnPnResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                                            scheme = region.scheme,
                                                            id = region.id,
                                                            description = region.description,
                                                            uri = region.uri
                                                        )
                                                    },
                                                    locality = addressDetails.locality.let { locality ->
                                                        OpenCnOnPnResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
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
                        OpenCnOnPnResponse.Tender.Item(
                            id = item.id,
                            internalId = item.internalId,
                            classification = item.classification.let { classification ->
                                OpenCnOnPnResponse.Tender.Item.Classification(
                                    scheme = classification.scheme,
                                    id = classification.id,
                                    description = classification.description
                                )
                            },
                            additionalClassifications = item.additionalClassifications?.map { additionalClassification ->
                                OpenCnOnPnResponse.Tender.Item.AdditionalClassification(
                                    scheme = additionalClassification.scheme,
                                    id = additionalClassification.id,
                                    description = additionalClassification.description
                                )
                            },
                            quantity = item.quantity,
                            unit = item.unit.let { unit ->
                                OpenCnOnPnResponse.Tender.Item.Unit(
                                    id = unit.id,
                                    name = unit.name
                                )
                            },
                            description = item.description,
                            relatedLot = item.relatedLot
                        )
                    },
                    awardCriteria = tender.awardCriteria,
                    awardCriteriaDetails = tender.awardCriteriaDetails,
                    submissionMethod = tender.submissionMethod,
                    submissionMethodRationale = tender.submissionMethodRationale,
                    submissionMethodDetails = tender.submissionMethodDetails,
                    documents = tender.documents.map { document ->
                        OpenCnOnPnResponse.Tender.Document(
                            documentType = document.documentType,
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

    private fun check(data: OpenCnOnPnRequest, context: CheckOpenCnOnPnContext) {
        val tender = data.tender
        checkCriteriaAndConversion(
            data.mainProcurementCategory,
            tender.awardCriteria,
            tender.awardCriteriaDetails,
            tender.documents,
            data.items,
            data.criteria,
            tender.criteria,
            tender.conversions,
            rulesService,
            context.pmd,
            context.country
        )
    }

    private fun OpenCnOnPnRequest.Tender.Lot.checkOptions() {
        if (hasOptions == null) return

        if (!hasOptions && (options != null && options.isNotEmpty()))
            throw ErrorException(
                error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                message = "Lot should not contain options"
            )
    }

    private fun OpenCnOnPnRequest.Tender.Lot.checkRecurrence() {
        if (hasRecurrence == null) return

        if (!hasRecurrence && recurrence != null)
            throw ErrorException(
                error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                message = "Lot should not contain reccurence"
            )
    }

    private fun OpenCnOnPnRequest.Tender.Lot.checkRenewal() {
        if (hasRenewal == null) return

        if (!hasRenewal && renewal != null)
            throw ErrorException(
                error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                message = "Lot should not contain renewal"
            )
    }

}

