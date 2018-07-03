package com.procurement.access.model.dto.lots

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.access.model.dto.ocds.Item
import com.procurement.access.model.dto.ocds.Lot
import com.procurement.access.model.dto.ocds.TenderStatus

@JsonInclude(JsonInclude.Include.NON_NULL)
data class LotsUpdateResponseDto @JsonCreator constructor(

        val tenderStatus: TenderStatus?,

        val lots: List<Lot>,

        val items: List<Item>?
)
