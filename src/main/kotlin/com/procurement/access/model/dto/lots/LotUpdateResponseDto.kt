package com.procurement.access.model.dto.lots

import com.fasterxml.jackson.annotation.JsonCreator
import com.procurement.access.model.dto.ocds.Lot

data class LotUpdateResponseDto @JsonCreator constructor(

        val lot: Lot
)
