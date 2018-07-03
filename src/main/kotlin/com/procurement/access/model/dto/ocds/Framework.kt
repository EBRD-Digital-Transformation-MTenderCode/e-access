package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotNull

data class Framework @JsonCreator constructor(

        @field:NotNull
        @get:JsonProperty("isAFramework")
        val isAFramework: Boolean
)