package com.procurement.access.model.dto.lots

import com.fasterxml.jackson.annotation.JsonCreator

data class CheckLotStatusRq @JsonCreator constructor(

        val relatedLot: String
)
