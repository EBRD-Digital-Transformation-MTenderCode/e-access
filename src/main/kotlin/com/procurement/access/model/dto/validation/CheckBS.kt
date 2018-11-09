package com.procurement.access.model.dto.validation

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.procurement.access.databinding.MoneyDeserializer
import java.math.BigDecimal

data class CheckBSRq @JsonCreator constructor(

        var planning: Planning
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Planning @JsonCreator constructor(

        val budget: Budget
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Budget @JsonCreator constructor(

        val budgetSource: Set<PlanningBudgetSource>
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PlanningBudgetSource @JsonCreator constructor(

        var budgetBreakdownID: String
)