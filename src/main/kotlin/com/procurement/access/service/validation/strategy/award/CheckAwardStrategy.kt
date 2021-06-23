package com.procurement.access.service.validation.strategy.award

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.api.v1.CommandMessage
import com.procurement.access.infrastructure.api.v1.cpidParsed
import com.procurement.access.infrastructure.api.v1.ocidParsed
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.handler.v1.model.request.CheckAwardRequest
import com.procurement.access.infrastructure.handler.v1.model.response.CheckAwardResponse
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toObject
import java.math.BigDecimal

class CheckAwardStrategy(private val tenderProcessDao: TenderProcessDao) {

    /**
     * eAccess executes next steps:
     * 1. Finds saved Tender and related Owner & Token values in DB by values of CPID && Token parameters
     *    from the context of Request;
     * 2. Validates Owner value by rule VR-3.11.6;
     * 3. Validates Token value by rule VR-3.11.5;
     * 4. Validates ID value by rule VR-3.11.10;
     * 5. Finds tender.lot object in tender (found on step 1) by value of ID (lot.ID) from the context of Request;
     * 6. Validates the value of lot.status in lot object found before by rule VR-3.11.8;
     * 7. Validates the values of award.value.amount & award.value.currency from Award got in Request by rule VR-3.11.9;
     */
    fun check(cm: CommandMessage): CheckAwardResponse {
        val contextRequest = context(cm)
        val request: CheckAwardRequest = toObject(CheckAwardRequest::class.java, cm.data)

        val entity: TenderProcessEntity = loadTenderProcessEntity(contextRequest.cpid, contextRequest.ocid)

        //VR-3.11.6
        checkOwner(ownerFromRequest = contextRequest.owner, entity = entity)

        //VR-3.11.5
        checkToken(tokenFromRequest = contextRequest.token, entity = entity)

        val cnEntity: CNEntity = toObject(CNEntity::class.java, entity.jsonData)
        val lotsById: Map<String, CNEntity.Tender.Lot> = cnEntity.tender.lots.associateBy { it.id }

        //VR-3.11.10
        checkLotId(contextRequest = contextRequest, lotsById = lotsById)

        val lot = lotsById.getValue(contextRequest.lotId)

        //VR-3.11.8
        checkLotStatus(lot)

        //VR-3.11.9
        checkAwardValue(lot, request)

        return CheckAwardResponse()
    }

    private fun loadTenderProcessEntity(cpid: Cpid, ocid: Ocid): TenderProcessEntity {
        return tenderProcessDao.getByCpidAndOcid(cpid, ocid)
            ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
    }

    /**
     * VR-3.11.5 CPID & token
     *
     * eAccess проверяет что найденный по token из запароса тендер содержит tender.ID,
     * значение которого равно занчению параметра cpid из запроса.
     */
    private fun checkToken(tokenFromRequest: String, entity: TenderProcessEntity) {
        if (entity.token.toString() != tokenFromRequest)
            throw ErrorException(error = ErrorType.INVALID_TOKEN)
    }

    /**
     * VR-3.11.6 owner
     *
     * eAccess проверяет соответствие owner связанного CN (выбранного из БД) и owner,
     * полученного в параметре запроса (Id platform).
     */
    private fun checkOwner(ownerFromRequest: String, entity: TenderProcessEntity) {
        if (entity.owner != ownerFromRequest)
            throw ErrorException(error = ErrorType.INVALID_OWNER)
    }

    /**
     * VR-3.11.8 "status" (lot)
     *
     * Analyzes the values of lot.status in lot object:
     *   IF lot.status == "active", validation successful;
     *   ELSE (lot.status != "active"), eAccess thrown Exception: "Lot can not be awarded";
     */
    private fun checkLotStatus(lot: CNEntity.Tender.Lot) {
        if (lot.status != LotStatus.ACTIVE)
            throw ErrorException(error = ErrorType.AWARD_ON_LOT_IN_INVALID_STATUS)
    }

    /**
     * VR-3.11.9 "amount" & "currency" (award)
     *
     * eAccess executes next steps:
     * 1. Finds tender.lot object in DB by value of ID (lot.ID) from the context of Request;
     * 2. If award.value.amount present in request:
     *   Get.lot.value.amount from Lot (found on step 1);
     *   a. Compares lot.value.amount determined previously with award.value.amount from Request:
     *   b. IF value of award.value.amount > 0 && <= (less || equal to) lot.value.amount value, validation is successful;
     *     ELSE eAccess throws Exception: "Invalid Award value";
     * 3. Get.lot.value.currency from Lot (found on step 1);
     * 4. Compares lot.value.currency determined previously with award.value.currency from Request:
     *     IF value of award.value.currency == (equal to) value of lot.value.currency, validation is successful;
     *     ELSE eAccess throws Exception: "Invalid currency value in award";
     */
    private fun checkAwardValue(lot: CNEntity.Tender.Lot, request: CheckAwardRequest) {
        request.award.value.amount?.let { amount ->
            if (amount <= BigDecimal.ZERO || amount > lot.value.amount)
                throw ErrorException(error = ErrorType.AWARD_HAS_INVALID_AMOUNT_VALUE)
        }

        if (request.award.value.currency != lot.value.currency)
            throw ErrorException(error = ErrorType.AWARD_HAS_INVALID_CURRENCY_VALUE)
    }

    /**
     * VR-3.11.10
     *
     * eAccess checks the availability of lot object with lot.ID == ID value from Request in proceeded tender:
     *   IF lot object where lot.ID == ID value from Request was found in DB, validation is successful;
     *   ELSE eAccess throws Exception: "Incorrect lot ID";
     */
    private fun checkLotId(contextRequest: ContextRequest, lotsById: Map<String, CNEntity.Tender.Lot>) {
        if (lotsById.containsKey(contextRequest.lotId).not()) {
            throw ErrorException(error = ErrorType.AWARD_RELATED_TO_UNKNOWN_LOT)
        }
    }

    private fun context(cm: CommandMessage): ContextRequest {
        val cpid = cm.cpidParsed
        val token = cm.context.token
            ?: throw ErrorException(error = ErrorType.CONTEXT, message = "Missing the 'token' attribute in context.")
        val owner = cm.context.owner
            ?: throw ErrorException(error = ErrorType.CONTEXT, message = "Missing the 'owner' attribute in context.")
        val ocid = cm.ocidParsed
        val lotId: String = cm.context.id
            ?: throw ErrorException(error = ErrorType.CONTEXT, message = "Missing the 'id' attribute in context.")

        return ContextRequest(
            cpid = cpid,
            token = token,
            ocid = ocid,
            owner = owner,
            lotId = lotId
        )
    }

    data class ContextRequest(
        val cpid: Cpid,
        val token: String,
        val owner: String,
        val ocid: Ocid.SingleStage,
        val lotId: String
    )
}
