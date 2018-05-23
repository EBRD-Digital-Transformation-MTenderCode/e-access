package com.procurement.access.model.dto.lots

import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.model.dto.ocds.Lot

data class LotUpdateResponseDto(

        @JsonProperty("lot")
        val lot: Lot
)
