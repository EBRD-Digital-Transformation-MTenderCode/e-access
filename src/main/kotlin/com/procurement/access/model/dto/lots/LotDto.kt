package com.procurement.access.model.dto.lots

import com.fasterxml.jackson.annotation.JsonProperty

data class LotDto(

        @JsonProperty("id")
        val id: String?
)