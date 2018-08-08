package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Lot @JsonCreator constructor(

        val id: String,

        val title: String?,

        val description: String?,

        var status: TenderStatus?,

        var statusDetails: TenderStatusDetails?,

        @field:Valid
        val value: Value,

        @field:Valid @field:NotEmpty
        val options: List<Option>?,

        @field:Valid @field:NotEmpty
        val recurrentProcurement: List<RecurrentProcurement>?,

        @field:Valid @field:NotEmpty
        val renewals: List<Renewal>?,

        @field:Valid @field:NotEmpty
        val variants: List<Variant>?,

        @field:Valid
        val contractPeriod: ContractPeriod,

        @field:Valid
        val placeOfPerformance: PlaceOfPerformance?
)