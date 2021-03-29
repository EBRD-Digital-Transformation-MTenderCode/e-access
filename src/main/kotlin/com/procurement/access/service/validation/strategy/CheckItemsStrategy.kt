package com.procurement.access.service.validation.strategy

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.CPVCode
import com.procurement.access.domain.model.CPVCodePattern
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.domain.model.patternBySymbols
import com.procurement.access.domain.model.patternOfGroups
import com.procurement.access.domain.model.startsWithPattern
import com.procurement.access.domain.model.toCPVCode
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.api.v1.CommandMessage
import com.procurement.access.infrastructure.api.v1.cpid
import com.procurement.access.infrastructure.api.v1.operationType
import com.procurement.access.infrastructure.api.v1.prevStage
import com.procurement.access.infrastructure.api.v1.stage
import com.procurement.access.infrastructure.entity.APEntity
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.handler.v1.model.request.CheckItemsRequest
import com.procurement.access.infrastructure.handler.v1.model.response.CheckItemsResponse
import com.procurement.access.lib.extension.toSet
import com.procurement.access.model.dto.ocds.Classification
import com.procurement.access.model.dto.ocds.TenderProcess
import com.procurement.access.utils.toObject

class CheckItemsStrategy(private val tenderProcessDao: TenderProcessDao) {

    /**
     * VR-3.14.1
     *
     * eAccess provides next steps:
     * eAccess checks the value of "operation type":
     * a. IF "operation type" == createCNonPN || createPINonPN || createNegotiationCnOnPn,
     *      eAccess checks the availability of Items object in saved version of tender (PN):
     *      IF there are NO Items object in saved version of tender, eAccess performs the next steps:
     *      1. Validates Item.Classification.ID received in Request by rule VR-3.14.2;
     *      2. Calculates tender.Classification.ID by rule BR-3.14.1;
     *      3. Compares tender.Classification.ID (got on step 1.a.i.2) with tender.Classification.ID
     *         from saved version of tender by rule VR-3.14.3;
     *      4. IF checking on step (1.a.i.3) is successful,
     *         eAccess returns "mdmValidation" == TRUE && calculated tender.Classification.ID && "itemsAdd" == TRUE;
     *    ELSE eAccess returns "mdmValidation" ==  FALSE & "itemsAdd" == TRUE;
     *
     * b. IF "operation type" == createPN || createPIN || createCN} eAccess performs the next steps:
     *   1. Validates Item.Classification.ID received in Request by rule VR-3.14.2;
     *   2. Calculates tender.Classification.ID by rule BR-3.14.1;
     *   3. eAccess returns "mdmValidation" == TRUE && calculated tender.Classification.ID && "itemsAdd" == TRUE;
     *
     * c. IF "operation type" == updatePN}, eAccess checks the availability of Items object in saved version of tender (PN):
     *      IF there are NO Items object in saved version of tender, eAccess performs the next steps:
     *      1. Validates Item.Classification.ID received in Request by rule VR-3.14.2;
     *      2. Calculates tender.Classification.ID by rule BR-3.14.1;
     *      3. Compares tender.Classification.ID (got on step 1.c.i.2) with tender.Classification.ID
     *         from saved version of tender by rule VR-3.14.3;
     *      4. IF checking on step (1.c.i.3) is successful,
     *         eAccess returns "mdmValidation" == TRUE && "itemsAdd" == TRUE && calculated tender.Classification.ID;
     *    ELSE Items object in saved version of tender is presented, eAccess returns "mdmValidation" == TRUE && "itemsAdd" == FALSE;
     */
    fun check(cm: CommandMessage): CheckItemsResponse {
        val operationType = cm.operationType
        val request: CheckItemsRequest = toObject(CheckItemsRequest::class.java, cm.data)
        return when (operationType) {
            OperationType.CREATE_CN_ON_PN,
            OperationType.CREATE_PIN_ON_PN,
            OperationType.CREATE_NEGOTIATION_CN_ON_PN -> {
                val cpid = cm.cpid
                val prevStage = cm.prevStage
                val process: TenderProcess = loadTenderProcess(cpid, prevStage)
                if (process.tender.items.isEmpty()) {
                    val itemsCpvCodes = getCPVCodes(request)
                    val itemsAreHomogeneous = areHomogeneous(itemsCpvCodes)

                    val homogeneousItemsCpvCodes = if (itemsAreHomogeneous)
                        itemsCpvCodes
                    else
                        getCpvCodesHomogeneousWithTenderClassification(itemsCpvCodes, process.tender.classification)

                    if (homogeneousItemsCpvCodes.isEmpty())
                        throw ErrorException(ErrorType.MISSING_HOMOGENEOUS_ITEMS)

                    val calculatedCPVCode = calculateCPVCode(homogeneousItemsCpvCodes)
                        .also {
                            checkCalculatedCPVCode(
                                calculatedCPVCode = it,
                                tenderCPVCode = process.tender.classification.id
                            )
                        }

                    CheckItemsResponse(
                        mdmValidation = true,
                        itemsAdd = true,
                        tender = CheckItemsResponse.Tender(
                            classification = CheckItemsResponse.Tender.Classification(
                                id = calculatedCPVCode
                            )
                        ),
                        mainProcurementCategory = process.tender.mainProcurementCategory,
                        items = request.items.map { item ->
                            CheckItemsResponse.Item(
                                id = item.id,
                                relatedLot = item.relatedLot
                            )
                        }
                    )
                } else {
                    CheckItemsResponse(
                        mdmValidation = false,
                        itemsAdd = true,
                        mainProcurementCategory = process.tender.mainProcurementCategory,
                        items = process.tender.items.map { item ->
                            CheckItemsResponse.Item(
                                id = item.id!!,
                                relatedLot = item.relatedLot
                            )
                        }
                    )
                }
            }

            OperationType.CREATE_CN,
            OperationType.CREATE_PN,
            OperationType.CREATE_PIN -> {
                val cpvCodes = getCPVCodes(request)
                    .also {
                        checkItemsCPVCodes(it)
                    }

                val calculatedTenderCPVCode = calculateCPVCode(cpvCodes)
                CheckItemsResponse(
                    mdmValidation = true,
                    itemsAdd = true,
                    tender = CheckItemsResponse.Tender(
                        classification = CheckItemsResponse.Tender.Classification(
                            id = calculatedTenderCPVCode
                        )
                    )
                )
            }

            OperationType.UPDATE_AP -> {
                val cpid = cm.cpid
                val stage = cm.stage
                val process: APEntity = loadAP(cpid, stage)
                if (request.items.isNotEmpty()) {
                    val cpvCodes = getCPVCodes(request)
                        .also {
                            checkItemsCPVCodes(it)
                        }

                    val calculatedCPVCode = calculateCPVCode(cpvCodes)
                        .also {
                            checkCalculatedCPVCode(
                                calculatedCPVCode = it,
                                tenderCPVCode = process.tender.classification.id
                            )
                        }

                    CheckItemsResponse(
                        mdmValidation = true,
                        itemsAdd = true,
                        tender = CheckItemsResponse.Tender(
                            classification = CheckItemsResponse.Tender.Classification(
                                id = calculatedCPVCode
                            )
                        )
                    )
                } else {
                    CheckItemsResponse(mdmValidation = true, itemsAdd = false)
                }
            }
            OperationType.UPDATE_PN -> {
                val cpid = cm.cpid
                val stage = cm.stage
                val process: TenderProcess = loadTenderProcess(cpid, stage)
                if (process.tender.items.isEmpty()) {
                    val cpvCodes = getCPVCodes(request)
                        .also {
                            checkItemsCPVCodes(it)
                        }

                    val calculatedCPVCode = calculateCPVCode(cpvCodes)
                        .also {
                            checkCalculatedCPVCode(
                                calculatedCPVCode = it,
                                tenderCPVCode = process.tender.classification.id
                            )
                        }

                    CheckItemsResponse(
                        mdmValidation = true,
                        itemsAdd = true,
                        tender = CheckItemsResponse.Tender(
                            classification = CheckItemsResponse.Tender.Classification(
                                id = calculatedCPVCode
                            )
                        )
                    )
                } else {
                    CheckItemsResponse(mdmValidation = true, itemsAdd = false)
                }
            }

            OperationType.UPDATE_CN -> {
                val cpid = cm.cpid
                val stage = cm.stage
                val cn: CNEntity = loadCN(cpid, stage)

                checkItems(request = request, cn = cn)

                CheckItemsResponse(
                    mdmValidation = true,
                    itemsAdd = true,
                    mainProcurementCategory = cn.tender.mainProcurementCategory,
                    items = request.items.map { item ->
                        CheckItemsResponse.Item(
                            id = item.id,
                            relatedLot = item.relatedLot
                        )
                    }
                )
            }

            OperationType.CREATE_CN_ON_PIN -> CheckItemsResponse.resultUndefined()

            OperationType.AMEND_FE,
            OperationType.APPLY_QUALIFICATION_PROTOCOL,
            OperationType.AWARD_CONSIDERATION,
            OperationType.COMPLETE_QUALIFICATION,
            OperationType.CREATE_AWARD,
            OperationType.CREATE_FE,
            OperationType.CREATE_PCR,
            OperationType.CREATE_RFQ,
            OperationType.CREATE_SUBMISSION,
            OperationType.DECLARE_NON_CONFLICT_OF_INTEREST,
            OperationType.DIVIDE_LOT,
            OperationType.ISSUING_FRAMEWORK_CONTRACT,
            OperationType.OUTSOURCING_PN,
            OperationType.QUALIFICATION,
            OperationType.QUALIFICATION_CONSIDERATION,
            OperationType.QUALIFICATION_DECLARE_NON_CONFLICT_OF_INTEREST,
            OperationType.QUALIFICATION_PROTOCOL,
            OperationType.RELATION_AP,
            OperationType.START_SECONDSTAGE,
            OperationType.SUBMIT_BID,
            OperationType.SUBMISSION_PERIOD_END,
            OperationType.TENDER_PERIOD_END,
            OperationType.UPDATE_AWARD,
            OperationType.WITHDRAW_BID,
            OperationType.WITHDRAW_QUALIFICATION_PROTOCOL -> throw ErrorException(
                error = ErrorType.INVALID_OPERATION_TYPE,
                message = "Operation type $operationType is not allowed for this command"
            )
        }
    }

    private fun loadTenderProcess(cpid: String, stage: String): TenderProcess {
        val entity = tenderProcessDao.getByCpIdAndStage(cpid, stage)
            ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        return toObject(TenderProcess::class.java, entity.jsonData)
    }

    private fun loadAP(cpid: String, stage: String): APEntity {
        val entity = tenderProcessDao.getByCpIdAndStage(cpid, stage)
            ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        return toObject(APEntity::class.java, entity.jsonData)
    }

    private fun loadCN(cpid: String, stage: String): CNEntity {
        val entity = tenderProcessDao.getByCpIdAndStage(cpid, stage)
            ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        return toObject(CNEntity::class.java, entity.jsonData)
    }

    private fun getCPVCodes(request: CheckItemsRequest): List<CPVCode> = request.items.map { it.classification.id }

    /**
     * VR-3.14.2 CPV code (item)
     *
     * eAccess проверяет, что "CPV code ID"  (Item.Classification.id) каждого Item from Request
     * относятся к одному классу кодов:
     * - "CPV code ID"  (Item.Classification.id) каждого Item совпадают по первым 3-м цифрам в коде: XXXNNNNN-Y.
     * ELSE eAccess cancels the update || create process and returns error code.
     */
    private fun checkItemsCPVCodes(codes: List<CPVCode>) {
        if (codes.isEmpty())
            throw ErrorException(error = ErrorType.EMPTY_ITEMS, message = "Items must not be empty.")

        if (codes.size > 1) {
            val pattern: CPVCodePattern = codes.first().patternOfGroups
            if (!codes.startsWithPattern(pattern))
                throw ErrorException(error = ErrorType.ITEMS_CPV_CODES_NOT_CONSISTENT)
        }
    }

    private fun areHomogeneous(codes: List<CPVCode>): Boolean {
        if (codes.isEmpty())
            throw ErrorException(error = ErrorType.EMPTY_ITEMS, message = "Items must not be empty.")
        val pattern: CPVCodePattern = codes.first().patternOfGroups
        return codes.startsWithPattern(pattern)
    }

    private fun getCpvCodesHomogeneousWithTenderClassification(codes: List<CPVCode>, classification: Classification): List<CPVCode> {
        val pattern: CPVCodePattern = classification.id.patternOfGroups
        return codes.filter { it.startsWithPattern(pattern) }
    }

    /**
     * VR-3.14.3 CPV code (tender)
     *
     * eAccess checks that calculated tender.Classification.ID (by rule BR-3.14.1)
     * coincides with tender.Classification.ID from saved version of tender by FIRST 3 FIGURES: : XXXNNNNN.
     * ELSE eAccess cancels the update process and returns error code.
     */
    private fun checkCalculatedCPVCode(calculatedCPVCode: CPVCode, tenderCPVCode: CPVCode) {
        if (calculatedCPVCode.patternOfGroups != tenderCPVCode.patternOfGroups)
            throw ErrorException(error = ErrorType.CALCULATED_CPV_CODE_NO_MATCH_TENDER_CPV_CODE)
    }

    /**
     * BR-3.14.1 CPV code (tender)
     *
     * eAccess определяет "CPV code" (tender.Classification) по значениям,
     * переданным в "Item Classification" (item.Classification) из запроса:
     * 1. eAccess определяет совпадающую часть (от 3 до 7 знаков) путем сравнения кодов
     *    во всех item (item.Classification.id), поступивших в запросе.
     * 2. eAccess записывает полученное на шаге 1 значение в "ID" (tender.Classification.ID),
     *    остальные цифры в коде заполняются нулями (помимо контрольного числа).
     *
     *    Пример: 12340000, где "1234" - совпадающая часть, "0000" - подставленные нули.
     */
    private fun calculateCPVCode(codes: List<CPVCode>): CPVCode =
        generalPattern(codes, 3, 7).toCPVCode()

    private fun generalPattern(codes: List<CPVCode>, countFrom: Int, countTo: Int): CPVCodePattern {
        if (codes.size == 1)
            return codes.first().take(countTo)

        val firstCode: CPVCode = codes.first()
        for (countSymbols in countTo downTo countFrom) {
            val pattern: CPVCodePattern = firstCode.patternBySymbols(countSymbols)
            if (codes.startsWithPattern(pattern))
                return pattern
        }

        throw ErrorException(
            error = ErrorType.ITEMS_CPV_CODES_NOT_CONSISTENT,
            message = "CPV codes of all items must have minimum 3 the same starting symbols."
        )
    }

    /**
     * VR-1.0.1.5.5
     * eAccess compares Items list from Request and Items list from DB:
     *   a. IF number of Items objects from Request == (equal to) number of Items objects from DB by delivered cpid
     *         && all Items.ID from Request can be included entirely in set of Items.ID from DB, validation is successful;
     *   b. ELSE eAccess throws Exception: "Incorrect Items list";
     */
    private fun checkItems(request: CheckItemsRequest, cn: CNEntity) {
        val idsSavedItems = cn.tender.items.toSet { it.id }
        val idsReceivedItems = request.items.toSet { it.id }
        if (!idsSavedItems.containsAll(idsReceivedItems))
            throw ErrorException(error = ErrorType.INVALID_ITEMS, message = "Incorrect Items list.")
    }
}
