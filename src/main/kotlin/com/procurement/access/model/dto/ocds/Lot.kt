package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import javax.validation.Valid
import javax.validation.constraints.NotEmpty

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("id", "title", "description", "status", "statusDetails", "value", "options", "recurrentProcurement", "renewals", "variants", "contractPeriod", "placeOfPerformance")
data class Lot(

        @JsonProperty("id")
        val id: String,

        @JsonProperty("title")
        val title: String,

        @JsonProperty("description")
        val description: String,

        @JsonProperty("status")
        val status: TenderStatus?,

        @JsonProperty("statusDetails")
        val statusDetails: TenderStatusDetails?,

        @Valid
        @JsonProperty("value")
        val value: Value,

        @Valid
        @NotEmpty
        @JsonProperty("options")
        val options: List<Option>,

        @Valid
        @NotEmpty
        @JsonProperty("recurrentProcurement")
        val recurrentProcurement: List<RecurrentProcurement>,

        @Valid
        @NotEmpty
        @JsonProperty("renewals")
        val renewals: List<Renewal>,

        @Valid
        @NotEmpty
        @JsonProperty("variants")
        val variants: List<Variant>,

        @Valid
        @NotEmpty
        @JsonProperty("contractPeriod")
        val contractPeriod: Period,

        @Valid
        @NotEmpty
        @JsonProperty("placeOfPerformance")
        val placeOfPerformance: PlaceOfPerformance
)