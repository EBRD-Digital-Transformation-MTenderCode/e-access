package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Renewal @JsonCreator constructor(

        @field:NotNull
        @get:JsonProperty("hasRenewals")
        private val hasRenewals: Boolean
)
