package com.procurement.access.model.dto.lots

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.access.model.dto.ocds.Item

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CheckLotStatusDetailsRs @JsonCreator constructor(

        val items: List<Item>
)
