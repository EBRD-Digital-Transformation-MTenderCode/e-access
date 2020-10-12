package com.procurement.access.infrastructure.handler.calculate.value

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.access.domain.model.amount.Amount
import com.procurement.access.infrastructure.bind.amount.AmountDeserializer
import com.procurement.access.infrastructure.bind.amount.AmountSerializer

data class CalculateAPValueResult(
    @field:JsonProperty("tender") @param:JsonProperty("tender") val tender: Tender
) {
    data class Tender(
        @field:JsonProperty("value") @param:JsonProperty("value") val value: Value
    ) {
        data class Value(
            @JsonDeserialize(using = AmountDeserializer::class)
            @JsonSerialize(using = AmountSerializer::class)
            @field:JsonProperty("amount") @param:JsonProperty("amount") val amount: Amount,
            @field:JsonProperty("currency") @param:JsonProperty("currency") val currency: String
        )
    }
}
