package com.procurement.access.domain.rule

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

class MinSpecificWeightPriceRule(
    @field:JsonProperty("goods") @param:JsonProperty("goods") val goods: BigDecimal,
    @field:JsonProperty("works") @param:JsonProperty("works") val works: BigDecimal,
    @field:JsonProperty("services") @param:JsonProperty("services") val services: BigDecimal
)
