package com.procurement.access.infrastructure.handler.v1.model.response

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.access.model.dto.ocds.Item

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CheckLotStatusDetailsRs @JsonCreator constructor(

        val items: List<Item>
)
