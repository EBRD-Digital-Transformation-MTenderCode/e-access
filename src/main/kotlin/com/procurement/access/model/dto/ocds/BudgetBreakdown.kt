package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import javax.validation.Valid
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BudgetBreakdown @JsonCreator constructor(

        @field:NotNull
        val id: String,

        val description: String?,

        @field:Valid @field:NotNull
        val amount: Value,

        @field:Valid @field:NotNull
        val period: Period,

        @field:Valid @field:NotNull
        val sourceParty: OrganizationReference
)