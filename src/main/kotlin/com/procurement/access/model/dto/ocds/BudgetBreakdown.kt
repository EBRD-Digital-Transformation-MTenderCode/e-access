package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import javax.validation.Valid

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BudgetBreakdown @JsonCreator constructor(

        val id: String,

        val description: String?,

        @field:Valid
        val amount: Value,

        @field:Valid
        val period: Period,

        @field:Valid
        val sourceParty: SourceParty?,

        @field:Valid
        val europeanUnionFunding: EuropeanUnionFunding?
)