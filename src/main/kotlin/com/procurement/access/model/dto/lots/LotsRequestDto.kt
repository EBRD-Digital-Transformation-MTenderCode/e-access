package com.procurement.access.model.dto.lots

import com.fasterxml.jackson.annotation.JsonCreator

data class LotsRequestDto @JsonCreator constructor(

        val unsuccessfulLots: HashSet<LotDto>?
)
