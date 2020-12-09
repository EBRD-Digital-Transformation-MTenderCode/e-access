package com.procurement.access.infrastructure.handler.v2.model.request

import com.fasterxml.jackson.annotation.JsonProperty

data class ValidateClassificationRequest(
    @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: String,
    @field:JsonProperty("ocid") @param:JsonProperty("ocid") val ocid: String,
    @field:JsonProperty("tender") @param:JsonProperty("tender") val tender: Tender
) {
    data class Tender(
        @field:JsonProperty("classification") @param:JsonProperty("classification") val classification: Classification
    ) {
        data class Classification(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String
        )
    }
}
