package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonProperty

data class DesignContest(

        @JsonProperty("serviceContractAward")
        @get:JsonProperty("serviceContractAward")
        val serviceContractAward: Boolean
)