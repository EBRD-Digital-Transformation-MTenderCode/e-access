package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotNull

data class Framework(

        @NotNull
        @JsonProperty("isAFramework")
        @get:JsonProperty("isAFramework")
        val isAFramework: Boolean
)