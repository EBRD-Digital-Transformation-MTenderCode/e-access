package com.procurement.access.infrastructure.handler.v1.model.request

import com.fasterxml.jackson.annotation.JsonCreator

data class UpdateLotDto @JsonCreator constructor(

        val id: String
)