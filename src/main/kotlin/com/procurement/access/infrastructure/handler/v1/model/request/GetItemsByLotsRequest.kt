package com.procurement.access.infrastructure.handler.v1.model.request


import com.fasterxml.jackson.annotation.JsonProperty

data class GetItemsByLotsRequest(
    @param:JsonProperty("lots") @field:JsonProperty("lots") val lots: List<Lot>
) {
    data class Lot(
        @param:JsonProperty("id") @field:JsonProperty("id") val id: String
    )
}