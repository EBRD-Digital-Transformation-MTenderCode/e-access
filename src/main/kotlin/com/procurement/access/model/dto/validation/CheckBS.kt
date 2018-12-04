package com.procurement.access.model.dto.validation

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude

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