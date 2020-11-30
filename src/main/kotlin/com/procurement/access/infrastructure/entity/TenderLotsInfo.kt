package com.procurement.access.infrastructure.entity

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails

data class TenderLotsInfo(
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @field:JsonProperty("lots") @param:JsonProperty("lots") val lots: List<Lot>?
) {
    data class Lot(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("status") @param:JsonProperty("status") val status: LotStatus?,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("statusDetails") @param:JsonProperty("statusDetails") val statusDetails: LotStatusDetails?
    )
}