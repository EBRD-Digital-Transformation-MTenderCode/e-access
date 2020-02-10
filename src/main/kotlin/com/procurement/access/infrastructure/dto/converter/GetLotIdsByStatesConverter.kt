package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.service.lot.GetLotIdsParams
import com.procurement.access.application.service.lot.GetLotIdsResult
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.dto.lot.GetLotIdsRequest
import com.procurement.access.infrastructure.dto.lot.GetLotIdsResponse
import com.procurement.access.lib.mapIfNotEmpty
import com.procurement.access.lib.orThrow

fun GetLotIdsRequest.convert() = GetLotIdsParams(
    cpid = this.cpid,
    ocid = this.ocid,
    states = this.states.mapIfNotEmpty { state ->
        GetLotIdsParams.State(
            status = state.status,
            statusDetails = state.statusDetails
        )
    }.orThrow {
        ErrorException(
            error = ErrorType.IS_EMPTY,
            message = "GetLotIdsByStatesRequest.states is empty")
    }
)

fun GetLotIdsResult.convert() = GetLotIdsResponse(
    result = this.lotIds.toList()
)
