package com.procurement.access.model.dto.pin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.access.model.dto.ocds.LotStatus
import com.procurement.access.model.dto.ocds.LotStatusDetails
import com.procurement.access.model.dto.ocds.Option
import com.procurement.access.model.dto.ocds.Period
import com.procurement.access.model.dto.ocds.PlaceOfPerformance
import com.procurement.access.model.dto.ocds.RecurrentProcurement
import com.procurement.access.model.dto.ocds.Renewal
import com.procurement.access.model.dto.ocds.Value
import com.procurement.access.model.dto.ocds.Variant

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