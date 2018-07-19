package com.procurement.access.model.dto.lots

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.access.model.dto.ocds.Lot

@JsonInclude(JsonInclude.Include.NON_NULL)
data class LotUpdateResponseDto @JsonCreator constructor(

        val lot: Lot
)
