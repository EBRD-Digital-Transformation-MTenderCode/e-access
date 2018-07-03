package com.procurement.access.model.dto.pn

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.procurement.access.model.dto.ocds.*
import javax.validation.Valid
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PnLot @JsonCreator constructor(

        @field:NotNull
        var id: String,

        val title: String?,

        val description: String?,

        var status: TenderStatus?,

        var statusDetails: TenderStatusDetails?,

        @field:Valid
        val value: Value?,

        @field:Valid
        val options: List<Option>?,

        @field:Valid
        val recurrentProcurement: List<RecurrentProcurement>?,

        @field:Valid
        val renewals: List<Renewal>?,

        @field:Valid
        val variants: List<Variant>?,

        @field:Valid
        val contractPeriod: Period?,

        @field:Valid
        val placeOfPerformance: PlaceOfPerformance?
)