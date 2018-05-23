package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotNull

data class DesignContest(

        @NotNull
        @JsonProperty("serviceContractAward")
        @get:JsonProperty("serviceContractAward")
        val serviceContractAward: Boolean
)