package com.procurement.access.infrastructure.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class TenderClassificationInfo(
    @field:JsonProperty("tender") @param:JsonProperty("tender") val tender: TenderState
) {
    data class TenderState(
        @field:JsonProperty("classification") @param:JsonProperty("classification") val classification: Classification
    ) {
        data class Classification(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String
        )
    }
}