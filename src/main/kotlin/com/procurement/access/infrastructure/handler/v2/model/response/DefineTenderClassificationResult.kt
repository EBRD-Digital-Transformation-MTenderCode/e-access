package com.procurement.access.infrastructure.handler.v2.model.response


import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.enums.Scheme

data class DefineTenderClassificationResult(
    @param:JsonProperty("tender") @field:JsonProperty("tender") val tender: Tender
) { companion object;

    data class Tender(
        @param:JsonProperty("classification") @field:JsonProperty("classification") val classification: Classification
    ) {
        data class Classification(
            @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
            @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: Scheme
        )
    }
}

fun DefineTenderClassificationResult.Companion.from(id: String, scheme: Scheme) =
    DefineTenderClassificationResult(
        tender = DefineTenderClassificationResult.Tender(
            classification = DefineTenderClassificationResult.Tender.Classification(
                id = id,
                scheme = scheme
            )
        )
    )