package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Budget(

        @JsonProperty("id")
        val id: String?,

        @JsonProperty("description")
        val description: String?,

        @JsonProperty("amount") @Valid @NotNull
        val amount: Value,

        @JsonProperty("project")
        val project: String?,

        @JsonProperty("projectID")
        val projectID: String?,

        @JsonProperty("uri")
        val uri: String?,

        @JsonProperty("source")
        val source: String?,

        @JsonProperty("europeanUnionFunding") @Valid
        val europeanUnionFunding: EuropeanUnionFunding?,

        @JsonProperty("isEuropeanUnionFunded") @NotNull
        @get:JsonProperty("isEuropeanUnionFunded")
        val isEuropeanUnionFunded: Boolean,

        @JsonProperty("budgetBreakdown") @Valid @NotEmpty
        val budgetBreakdown: List<BudgetBreakdown>
)