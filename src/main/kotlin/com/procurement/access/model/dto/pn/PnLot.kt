package com.procurement.access.model.dto.pn

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.model.dto.ocds.*
import javax.validation.Valid
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PnLot(

        @JsonProperty("id") @NotNull
        var id: String,

        @JsonProperty("title")
        val title: String?,

        @JsonProperty("description")
        val description: String?,

        @JsonProperty("status")
        var status: TenderStatus?,

        @JsonProperty("statusDetails")
        var statusDetails: TenderStatusDetails?,

        @JsonProperty("value") @Valid
        val value: Value?,

        @JsonProperty("options") @Valid
        val options: List<Option>?,

        @JsonProperty("recurrentProcurement") @Valid
        val recurrentProcurement: List<RecurrentProcurement>?,

        @JsonProperty("renewals") @Valid
        val renewals: List<Renewal>?,

        @JsonProperty("variants") @Valid
        val variants: List<Variant>?,

        @JsonProperty("contractPeriod") @Valid
        val contractPeriod: Period?,

        @JsonProperty("placeOfPerformance") @Valid
        val placeOfPerformance: PlaceOfPerformance?
)