package com.procurement.access.model.dto.lots

import com.fasterxml.jackson.annotation.JsonCreator

data class LotDto @JsonCreator constructor(

        val id: String?
)