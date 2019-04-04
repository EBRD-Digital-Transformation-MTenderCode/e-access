package com.procurement.access.infrastructure.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.CPVCode

@JsonIgnoreProperties(ignoreUnknown = true)
data class CheckItemsRequest(
    @field:JsonProperty("items") @param:JsonProperty("items") val items: List<Item>
) {

    data class Item(
        @field:JsonProperty("classification") @param:JsonProperty("classification") val classification: Classification
    ) {

        data class Classification(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: CPVCode
        )
    }
}
