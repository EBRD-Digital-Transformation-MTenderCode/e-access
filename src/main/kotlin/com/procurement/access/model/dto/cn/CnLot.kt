package com.procurement.access.model.dto.cn

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.procurement.access.model.dto.ocds.*
import javax.validation.Valid
import javax.validation.constraints.NotEmpty

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("id", "title", "description", "status", "statusDetails", "value", "options", "recurrentProcurement", "renewals", "variants", "contractPeriod", "placeOfPerformance")
data class CnLot(

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

        @JsonProperty("value") @Valid
        val value: Value,

        @JsonProperty("options") @Valid @NotEmpty
        val options: List<Option>,

        @JsonProperty("recurrentProcurement") @Valid @NotEmpty
        val recurrentProcurement: List<RecurrentProcurement>,

        @JsonProperty("renewals") @Valid @NotEmpty
        val renewals: List<Renewal>,

        @JsonProperty("variants") @Valid @NotEmpty
        val variants: List<Variant>,

        @JsonProperty("contractPeriod") @Valid
        val contractPeriod: Period,

        @param:JsonProperty("placeOfPerformance") @Valid
        val placeOfPerformance: PlaceOfPerformance
)