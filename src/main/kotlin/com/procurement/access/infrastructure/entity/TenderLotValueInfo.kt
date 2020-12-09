package com.procurement.access.infrastructure.entity

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class TenderLotValueInfo(
    @field:JsonProperty("tender") @param:JsonProperty("tender") val tender: Tender
) {
    data class Tender(
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @field:JsonProperty("lots") @param:JsonProperty("lots") val lots: List<Lot>?
    ) {
        data class Lot(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
            @field:JsonProperty("value") @param:JsonProperty("value") val value: Value
        ) {
            data class Value(
                @field:JsonProperty("amount") @param:JsonProperty("amount") val amount: BigDecimal,
                @field:JsonProperty("currency") @param:JsonProperty("currency") val currency: String
            )
        }
    }
}