package com.procurement.access.application.service.lot

import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails

data class GetLotIdsByStatesParams(
    val cpid: String,
    val ocid: String,
    val states: List<State>
) {
    data class State(
        val status: LotStatus?,
        val statusDetails: LotStatusDetails?
    )
}
