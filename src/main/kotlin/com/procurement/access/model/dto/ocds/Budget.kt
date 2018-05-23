package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.Valid
import javax.validation.constraints.NotEmpty

@JsonInclude(JsonInclude.Include.NON_NULL)
//@JsonPropertyOrder("id", "description", "amount", "project", "projectID", "uri", "source", "europeanUnionFunding", "isEuropeanUnionFunded", "budgetBreakdown")
data class Budget(

        @JsonProperty("id")
        val id: String?,

        @JsonProperty("description")
        val description: String?,

        @Valid
        @JsonProperty("amount")
        val amount: Value,

        @JsonProperty("project")
        val project: String?,

        @JsonProperty("projectID")
        val projectID: String?,

        @JsonProperty("uri")
        val uri: String?,

        @JsonProperty("source")
        val source: String?,

        @Valid
        @JsonProperty("europeanUnionFunding")
        val europeanUnionFunding: EuropeanUnionFunding?,

        @JsonProperty("isEuropeanUnionFunded")
        @get:JsonProperty("isEuropeanUnionFunded")
        val isEuropeanUnionFunded: Boolean,

        @Valid
        @NotEmpty
        @JsonProperty("budgetBreakdown")
        val budgetBreakdown: List<BudgetBreakdown>
)