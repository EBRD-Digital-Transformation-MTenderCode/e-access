package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.procurement.access.model.dto.databinding.BooleansDeserializer
import javax.validation.Valid
import javax.validation.constraints.NotNull

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