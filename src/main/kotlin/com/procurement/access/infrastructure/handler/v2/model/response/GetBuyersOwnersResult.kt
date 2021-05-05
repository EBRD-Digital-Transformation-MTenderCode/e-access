package com.procurement.access.infrastructure.handler.v2.model.response


import com.fasterxml.jackson.annotation.JsonProperty

data class GetBuyersOwnersResult(
    @param:JsonProperty("buyers") @field:JsonProperty("buyers") val buyers: List<Buyer>
) {
    data class Buyer(
        @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
        @param:JsonProperty("name") @field:JsonProperty("name") val name: String,
        @param:JsonProperty("owner") @field:JsonProperty("owner") val owner: String
    )
}