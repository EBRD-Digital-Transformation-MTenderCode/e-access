package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.service.lot.GetLotIdsParams
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.dto.lot.GetLotIdsRequest
import com.procurement.access.lib.errorIfEmpty

fun GetLotIdsRequest.convert() = GetLotIdsParams(
    cpid = this.cpid,
    ocid = this.ocid,
    states = this.states.errorIfEmpty {
        ErrorException(
            error = ErrorType.IS_EMPTY,
            message = "GetLotIdsByStatesRequest.states is empty"
        )
    }
        ?.map { state ->
            GetLotIdsParams.State(
                status = if (state.status != null) LotStatus.fromString(state.status) else null,
                statusDetails = if (state.statusDetails != null) LotStatusDetails.fromString(state.statusDetails) else null
            )
        }
        .orEmpty()
)
