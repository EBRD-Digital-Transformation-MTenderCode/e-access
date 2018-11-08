package com.procurement.access.model.dto.lots

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.access.model.dto.ocds.Value

@JsonInclude(JsonInclude.Include.NON_NULL)
data class LotDto @JsonCreator constructor(

        val id: String?,

        var title: String?,

        var description: String?,

        val value: Value?
)