package com.procurement.access.infrastructure.handler.set.stateforlots

import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.domain.model.lot.LotId

data class SetStateForLotsResult(
    @field:JsonProperty("id") @param:JsonProperty("id") val id: LotId,
    @field:JsonProperty("status") @param:JsonProperty("status") val status: LotStatus,
    @field:JsonProperty("statusDetails") @param:JsonProperty("statusDetails") val statusDetails: LotStatusDetails
)