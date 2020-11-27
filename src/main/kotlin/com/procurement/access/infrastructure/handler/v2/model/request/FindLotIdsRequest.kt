package com.procurement.access.infrastructure.handler.v2.model.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

data class FindLotIdsRequest(
    @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: String,
    @field:JsonProperty("ocid") @param:JsonProperty("ocid") val ocid: String,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @field:JsonProperty("states") @param:JsonProperty("states") val states: List<State>?
) {
    data class State(
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("status") @param:JsonProperty("status") val status: String?,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("statusDetails") @param:JsonProperty("statusDetails") val statusDetails: String?
    )
}
