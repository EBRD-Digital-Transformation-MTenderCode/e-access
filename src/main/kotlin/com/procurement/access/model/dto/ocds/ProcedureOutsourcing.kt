package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotNull

data class ProcedureOutsourcing(

        @NotNull
        @JsonProperty("procedureOutsourced")
        @get:JsonProperty("procedureOutsourced")
        val procedureOutsourced: Boolean
)