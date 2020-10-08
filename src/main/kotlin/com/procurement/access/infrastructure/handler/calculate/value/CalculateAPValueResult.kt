package com.procurement.access.infrastructure.handler.calculate.value

import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.money.Money

data class CalculateAPValueResult(
    @field:JsonProperty("tender") @param:JsonProperty("tender") val tender: Tender
) {
    data class Tender(
        @field:JsonProperty("value") @param:JsonProperty("value") val value: Money
    )
}
