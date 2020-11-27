package com.procurement.access.infrastructure.handler.v1.model.request

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.CPVCode

@JsonIgnoreProperties(ignoreUnknown = true)
data class CheckItemsRequest(
    @field:JsonProperty("items") @param:JsonProperty("items") val items: List<Item>
) {

    data class Item(
        @field:JsonProperty("classification") @param:JsonProperty("classification") val classification: Classification,
        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
        @field:JsonProperty("relatedLot") @param:JsonProperty("relatedLot") val relatedLot: String
    ) {

        data class Classification(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: CPVCode
        )
    }
}
