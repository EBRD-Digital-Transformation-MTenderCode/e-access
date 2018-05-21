package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonProperty

data class ProcedureOutsourcing(

        @JsonProperty("procedureOutsourced")
        @get:JsonProperty("procedureOutsourced")
        val procedureOutsourced: Boolean
)