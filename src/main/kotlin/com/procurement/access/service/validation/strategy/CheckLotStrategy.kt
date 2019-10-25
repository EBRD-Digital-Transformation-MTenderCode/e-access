package com.procurement.access.service.validation.strategy

import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.model.dto.bpe.CommandMessage
import com.procurement.access.model.dto.ocds.Lot
import com.procurement.access.model.dto.ocds.LotStatusDetails
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
        val cpid = getCPID(cm)
        val lotId = getLotId(cm)
        val stage = getStage(cm)
        val process: TenderProcess = loadTenderProcess(cpid, stage)

        val lot = process.tender.lots.firstOrNull {
            UUID.fromString(it.id) == lotId
        } ?: throw ErrorException(
            error = ErrorType.LOT_NOT_FOUND,
            message = "Lot with id: $lotId is not found."
        )

        checkLotStatuses(lot)
    }

    private fun loadTenderProcess(cpid: String, stage: String): TenderProcess {
        val entity = tenderProcessDao.getByCpIdAndStage(cpid, stage)
            ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        return toObject(TenderProcess::class.java, entity.jsonData)
    }

    private fun checkLotStatuses(lot: Lot) {
        if (lot.status != LotStatus.ACTIVE)
            throw ErrorException(
                error = ErrorType.INVALID_LOT_STATUS,
                message = "Lot must be with status: 'ACTIVE'."
            )
        if (lot.statusDetails != LotStatusDetails.EMPTY)
            throw ErrorException(
                error = ErrorType.INVALID_LOT_STATUS_DETAILS,
                message = "Lot must be with status details: 'EMPTY'."
            )
    }

    private fun getCPID(cm: CommandMessage): String {
        return cm.context.cpid
            ?: throw ErrorException(
                error = ErrorType.CONTEXT,
                message = "Missing the 'cpid' attribute in context."
            )
    }

    private fun getStage(cm: CommandMessage): String {
        return cm.context.stage
            ?: throw ErrorException(
                error = ErrorType.CONTEXT,
                message = "Missing the 'stage' attribute in context."
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
}