package com.procurement.access.service.handler

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.lot.GetLotIdsParams
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.dto.converter.convert
import com.procurement.access.infrastructure.dto.lot.GetLotIdsRequest
import com.procurement.access.infrastructure.web.dto.ApiSuccessResponse
import com.procurement.access.model.dto.bpe.getId
import com.procurement.access.model.dto.bpe.getParams
import com.procurement.access.model.dto.bpe.getVersion
import com.procurement.access.model.dto.ocds.Lot
import com.procurement.access.model.dto.ocds.TenderProcess
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service

@Service
class GetLotIdsHandler(
    private val tenderProcessDao: TenderProcessDao
) {

    fun handle(request: JsonNode): ApiSuccessResponse {

        val id = request.getId()
        val version = request.getVersion()
        val params = request.getParams()
        val data = toObject(GetLotIdsRequest::class.java, params).convert()

        val stage = data.ocid.split("-")[4]
        val tenderEntity = tenderProcessDao.getByCpIdAndStage(cpId = data.cpid, stage = stage)
            ?: throw ErrorException(ErrorType.DATA_NOT_FOUND)
        val tenderProcess = toObject(TenderProcess::class.java, tenderEntity.jsonData)

        return when {
            data.states.isEmpty() ->
                ApiSuccessResponse(
                    id = id,
                    result = tenderProcess.tender.lots
                        .map { lot -> LotId.fromString(lot.id) },
                    version = version
                )
            else                  -> {
                val sortedStatuses = data.states.sorted()

                val lotIds = getLotsOnStates(
                    lots = tenderProcess.tender.lots,
                    states = sortedStatuses
                ).map { lot -> LotId.fromString(lot.id) }

                ApiSuccessResponse(
                    id = id,
                    result = lotIds,
                    version = version
                )
            }
        }
    }

    private fun getLotsOnStates(
        lots: List<Lot>,
        states: List<GetLotIdsParams.State>
    ): List<Lot> {
        return lots.filter { lot ->
            val state = states.firstOrNull { state ->
                checkLotOnStatusAndStatusDetails(
                    lot = lot,
                    status = state.status,
                    statusDetails = state.statusDetails
                )
            }
            state != null
        }
    }

    private fun checkLotOnStatusAndStatusDetails(
        lot: Lot,
        status: LotStatus?,
        statusDetails: LotStatusDetails?
    ): Boolean {
        return when {
            status == null        -> lot.statusDetails == statusDetails

            statusDetails == null -> lot.status == status

            else                  -> lot.statusDetails == statusDetails && lot.status == status

        }
    }
}