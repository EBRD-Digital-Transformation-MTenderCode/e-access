package com.procurement.access.model.dto.lots

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.access.model.dto.ocds.Lot

data class UpdateLotByBidRq @JsonCreator constructor(

        val lotId: String,

        val lotAwarded: Boolean
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UpdateLotByBidRs @JsonCreator constructor(

        val lot: Lot
)
