package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import javax.validation.Valid
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("id", "description", "amount", "period", "sourceParty")
data class BudgetBreakdown(

        @JsonProperty("id")
        val id: String,

        @JsonProperty("description")
        val description: String?,

        @Valid
        @param:JsonProperty("amount")
        val amount: Value,

        @Valid
        @JsonProperty("period")
        val period: Period,

        @Valid
        @JsonProperty("sourceParty")
        val sourceParty: OrganizationReference
)