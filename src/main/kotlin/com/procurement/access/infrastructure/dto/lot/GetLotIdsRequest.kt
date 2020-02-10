package com.procurement.access.infrastructure.dto.lot

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails

data class GetLotIdsRequest(
    @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: String,
    @field:JsonProperty("ocid") @param:JsonProperty("ocid") val ocid: String,
    @field:JsonProperty("states") @param:JsonProperty("states") val states: List<State>
) {
    data class State(
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("status") @param:JsonProperty("status") val status: LotStatus?,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("statusDetails") @param:JsonProperty("statusDetails") val statusDetails: LotStatusDetails?
    )
}
