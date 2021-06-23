package com.procurement.access.service.validation.strategy

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.api.v1.CommandMessage
import com.procurement.access.infrastructure.api.v1.cpidParsed
import com.procurement.access.infrastructure.api.v1.ocidParsed
import com.procurement.access.infrastructure.entity.RfqEntity
import com.procurement.access.model.dto.ocds.TenderProcess
import com.procurement.access.utils.toObject
import java.util.*

class CheckLotStrategy(private val tenderProcessDao: TenderProcessDao) {

    /**
     * CR-1.5.1.1
     *
     * eAccess executes next steps:
     * Finds saved Tender in DB by value of CPID parameter from the context of Request;
     * Selects tender.lot object in DB where lot.ID == lotId from context of comunda;
     * Validates the values of lot.status && lot.statusDetails in lot object found before by rule VR-1.5.1.1;
     */
    fun check(cm: CommandMessage) {
        val cpid = cm.cpidParsed
        val lotId = getLotId(cm)
        val ocid = cm.ocidParsed

        val entity = tenderProcessDao.getByCpidAndOcid(cpid, ocid)
            ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)

        val lotState = when (ocid.stage) {
            Stage.AC,
            Stage.EV,
            Stage.FE,
            Stage.NP,
            Stage.TP -> {
                val process = toObject(TenderProcess::class.java, entity.jsonData)
                process.tender.lots
                    .find { UUID.fromString(it.id) == lotId }
                    ?.let { LotState(it.status, it.statusDetails) }
            }

            Stage.RQ -> {
                val rfq = toObject(RfqEntity::class.java, entity.jsonData)
                rfq.tender.lots
                    .find { it.id == lotId }
                    ?.let { LotState(it.status, it.statusDetails) }
            }

            Stage.AP,
            Stage.EI,
            Stage.FS,
            Stage.PC,
            Stage.PN -> throw ErrorException(
                error = ErrorType.INVALID_STAGE,
                message = "Stage ${ocid.stage} not allowed at the command."
            )
        }

        if (lotState == null)
            throw ErrorException(
                error = ErrorType.LOT_NOT_FOUND,
                message = "Lot with id: $lotId is not found."
            )

        checkLotState(lotState)
    }

    private fun checkLotState(lotState: LotState) {
        if (lotState.status != LotStatus.ACTIVE)
            throw ErrorException(
                error = ErrorType.INVALID_LOT_STATUS,
                message = "Lot must be with status: 'ACTIVE'."
            )
        if (lotState.statusDetails != LotStatusDetails.EMPTY)
            throw ErrorException(
                error = ErrorType.INVALID_LOT_STATUS_DETAILS,
                message = "Lot must be with status details: 'EMPTY'."
            )
    }

    private fun getLotId(cm: CommandMessage): UUID = cm.context.id
        ?.let { id ->
            try {
                UUID.fromString(id)
            } catch (exception: Exception) {
                throw ErrorException(error = ErrorType.INVALID_FORMAT_LOT_ID)
            }
        }
        ?: throw ErrorException(error = ErrorType.CONTEXT, message = "Missing the 'id' attribute in context.")

    private data class LotState(val status: LotStatus?, val statusDetails: LotStatusDetails?)
}