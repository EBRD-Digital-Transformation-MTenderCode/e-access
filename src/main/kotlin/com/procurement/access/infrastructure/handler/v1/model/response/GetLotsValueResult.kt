package com.procurement.access.infrastructure.handler.v1.model.response


import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.amount.Amount

data class GetLotsValueResult(
    @param:JsonProperty("tender") @field:JsonProperty("tender") val tender: Tender
) {
    data class Tender(
        @param:JsonProperty("lots") @field:JsonProperty("lots") val lots: List<Lot>
    ) {
        data class Lot(
            @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
            @param:JsonProperty("value") @field:JsonProperty("value") val value: Value
        ) {
            data class Value(
                @param:JsonProperty("amount") @field:JsonProperty("amount") val amount: Amount,
                @param:JsonProperty("currency") @field:JsonProperty("currency") val currency: String
            )
        }
    }
}