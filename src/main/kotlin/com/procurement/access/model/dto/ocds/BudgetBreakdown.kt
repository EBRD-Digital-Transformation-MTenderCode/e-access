package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.Valid
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BudgetBreakdown(

        @JsonProperty("id") @NotNull
        val id: String,

        @JsonProperty("description")
        val description: String?,

        @param:JsonProperty("amount") @Valid @NotNull
        val amount: Value,

        @JsonProperty("period") @Valid @NotNull
        val period: Period,

        @JsonProperty("sourceParty") @Valid @NotNull
        val sourceParty: OrganizationReference
)