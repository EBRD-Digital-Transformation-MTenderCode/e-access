package com.procurement.access.infrastructure.handler.v1.model.request

import com.fasterxml.jackson.annotation.JsonCreator

data class CheckLotStatusRq @JsonCreator constructor(

        val relatedLot: String
)
