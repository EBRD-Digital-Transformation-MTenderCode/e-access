package com.procurement.access.model.dto.pin

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.model.dto.ocds.*
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
//@JsonPropertyOrder("id", "title", "description", "status", "statusDetails", "value", "options", "recurrentProcurement", "renewals", "variants", "contractPeriod", "placeOfPerformance")
data class PinLot(

        @JsonProperty("id")
        var id: String,

        @JsonProperty("title") @NotNull
        val title: String?,

        @JsonProperty("description") @NotNull
        val description: String?,

        @JsonProperty("status")
        var status: TenderStatus?,

        @JsonProperty("statusDetails")
        var statusDetails: TenderStatusDetails?,

        @JsonProperty("value") @Valid @NotNull
        val value: Value?,

        @JsonProperty("options") @Valid @NotEmpty
        val options: List<Option>?,

        @JsonProperty("recurrentProcurement") @Valid @NotNull
        val recurrentProcurement: List<RecurrentProcurement>?,

        @JsonProperty("renewals") @Valid @NotEmpty @NotNull
        val renewals: List<Renewal>?,

        @JsonProperty("variants") @Valid @NotEmpty @NotNull
        val variants: List<Variant>?,

        @JsonProperty("contractPeriod") @Valid @NotNull
        val contractPeriod: Period?,

        @JsonProperty("placeOfPerformance") @Valid @NotNull
        val placeOfPerformance: PlaceOfPerformance?
)