package com.procurement.access.infrastructure.handler.v1.model.response

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class GetItemsByLotsResponse(
    @param:JsonProperty("items") @field:JsonProperty("items") val items: List<Item>
) {
    data class Item(
        @param:JsonProperty("id") @field:JsonProperty("id") val id: String,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @param:JsonProperty("internalId") @field:JsonProperty("internalId") val internalId: String?,

        @param:JsonProperty("description") @field:JsonProperty("description") val description: String,
        @param:JsonProperty("quantity") @field:JsonProperty("quantity") val quantity: BigDecimal,
        @param:JsonProperty("classification") @field:JsonProperty("classification") val classification: Classification,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @param:JsonProperty("additionalClassifications") @field:JsonProperty("additionalClassifications") val additionalClassifications: List<AdditionalClassification>?,

        @param:JsonProperty("unit") @field:JsonProperty("unit") val unit: Unit,
        @param:JsonProperty("relatedLot") @field:JsonProperty("relatedLot") val relatedLot: String
    ) {
        data class Classification(
            @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
            @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: String,
            @param:JsonProperty("description") @field:JsonProperty("description") val description: String
        )

        data class AdditionalClassification(
            @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
            @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: String,
            @param:JsonProperty("description") @field:JsonProperty("description") val description: String
        )

        data class Unit(
            @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
            @param:JsonProperty("name") @field:JsonProperty("name") val name: String
        )
    }
}