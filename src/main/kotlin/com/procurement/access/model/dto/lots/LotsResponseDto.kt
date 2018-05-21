package com.procurement.access.model.dto.lots

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class LotsResponseDto(

        @JsonProperty("awardCriteria")
        val awardCriteria: String?,

        @JsonProperty("lots")
        val lots: List<LotDto>?
)
