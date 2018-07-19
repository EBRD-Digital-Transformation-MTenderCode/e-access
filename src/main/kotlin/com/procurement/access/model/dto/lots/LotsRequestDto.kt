package com.procurement.access.model.dto.lots

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class LotsRequestDto @JsonCreator constructor(

        val unsuccessfulLots: HashSet<LotDto>?
)
