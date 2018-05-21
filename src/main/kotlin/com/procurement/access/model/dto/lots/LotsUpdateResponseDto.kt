package com.procurement.access.model.dto.lots

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.model.dto.ocds.Item
import com.procurement.access.model.dto.ocds.Lot
import com.procurement.access.model.dto.ocds.TenderStatus

@JsonInclude(JsonInclude.Include.NON_NULL)
data class LotsUpdateResponseDto(

        @JsonProperty("tenderStatus")
        val tenderStatus: TenderStatus?,

        @JsonProperty("lots")
        val lots: List<Lot>?,

        @JsonProperty("items")
        val items: List<Item>?
)
