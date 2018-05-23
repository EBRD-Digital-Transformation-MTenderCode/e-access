package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotNull

data class Renewal(

        @NotNull
        @JsonProperty("hasRenewals")
        @get:JsonProperty("hasRenewals")
        private val hasRenewals: Boolean
)
