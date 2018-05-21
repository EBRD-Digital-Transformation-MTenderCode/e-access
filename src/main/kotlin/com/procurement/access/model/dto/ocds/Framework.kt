package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonProperty

data class Framework(

        @JsonProperty("isAFramework")
        @get:JsonProperty("isAFramework")
        val isAFramework: Boolean
)