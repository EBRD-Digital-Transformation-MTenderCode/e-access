package com.procurement.access.model.dto.pin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.access.model.dto.ocds.*

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PinLot @JsonCreator constructor(

        var id: String,

        val title: String?,

        val description: String?,

        var status: LotStatus?,

        var statusDetails: LotStatusDetails?,

        val value: Value?,

        val options: List<Option>?,

        val recurrentProcurement: List<RecurrentProcurement>?,

        val renewals: List<Renewal>?,

        val variants: List<Variant>?,

        val contractPeriod: Period?,

        val placeOfPerformance: PlaceOfPerformance?
)