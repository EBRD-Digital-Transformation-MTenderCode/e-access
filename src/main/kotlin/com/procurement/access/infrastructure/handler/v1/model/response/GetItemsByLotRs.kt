package com.procurement.access.infrastructure.handler.v1.model.response

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.access.infrastructure.handler.v1.model.request.ItemDto

@JsonInclude(JsonInclude.Include.NON_NULL)
data class GetItemsByLotRs @JsonCreator constructor(
    val items: List<ItemDto>?
)
