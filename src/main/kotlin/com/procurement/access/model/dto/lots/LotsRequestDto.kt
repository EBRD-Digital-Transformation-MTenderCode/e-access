package com.procurement.access.model.dto.lots

import com.fasterxml.jackson.annotation.JsonProperty

data class LotsRequestDto(

        @JsonProperty("unsuccessfulLots")
        val lots: List<LotDto>?
)
