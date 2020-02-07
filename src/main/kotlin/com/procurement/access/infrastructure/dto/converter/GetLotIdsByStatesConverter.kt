package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.service.lot.GetLotIdsByStatesParams
import com.procurement.access.application.service.lot.GetLotIdsByStatesResult
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.dto.lot.GetLotIdsByStatesRequest
import com.procurement.access.infrastructure.dto.lot.GetLotIdsByStatesResponse
import com.procurement.access.lib.mapIfNotEmpty
import com.procurement.access.lib.orThrow

fun GetLotIdsByStatesRequest.convert() = GetLotIdsByStatesParams(
    cpid = this.cpid,
    ocid = this.ocid,
    states = this.states.mapIfNotEmpty { state ->
        GetLotIdsByStatesParams.State(
            status = state.status,
            statusDetails = state.statusDetails
        )
    }.orThrow {
        ErrorException(
            error = ErrorType.IS_EMPTY,
            message = "GetLotIdsByStatesRequest.states is empty")
    }
)

fun GetLotIdsByStatesResult.convert() = GetLotIdsByStatesResponse(
    result = this.lotIds.toList()
)
