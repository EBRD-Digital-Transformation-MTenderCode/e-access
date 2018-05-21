package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonProperty

data class Renewal(

        @JsonProperty("hasRenewals")
        @get:JsonProperty("hasRenewals")
        private val hasRenewals: Boolean
)
