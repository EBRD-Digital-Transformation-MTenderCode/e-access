package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotNull

data class ProcedureOutsourcing @JsonCreator constructor(

        @field:NotNull
        @get:JsonProperty("procedureOutsourced")
        val procedureOutsourced: Boolean
)