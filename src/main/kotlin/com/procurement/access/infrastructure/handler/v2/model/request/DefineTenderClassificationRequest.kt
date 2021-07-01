package com.procurement.access.infrastructure.handler.v2.model.request


import com.fasterxml.jackson.annotation.JsonProperty

data class DefineTenderClassificationRequest(
    @field:JsonProperty("relatedCpid") @param:JsonProperty("relatedCpid") val relatedCpid: String,
    @field:JsonProperty("relatedOcid") @param:JsonProperty("relatedOcid") val relatedOcid: String,
    @field:JsonProperty("tender") @param:JsonProperty("tender") val tender: Tender
) {
    data class Tender(
        @field:JsonProperty("items") @param:JsonProperty("items") val items: List<Item>
    ) {
        data class Item(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
            @field:JsonProperty("classification") @param:JsonProperty("classification") val classification: Classification
        ) {
            data class Classification(
                @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String
            )
        }
    }
}