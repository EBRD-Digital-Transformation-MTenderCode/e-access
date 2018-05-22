package com.procurement.access.model.dto.pn

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.procurement.access.model.dto.ocds.*
import javax.validation.Valid
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
//@JsonPropertyOrder("id", "title", "description", "status", "statusDetails", "value", "options", "variants", "renewals", "recurrentProcurement", "contractPeriod", "placeOfPerformance")
data class PnLot(

        @JsonProperty("id") @field:NotNull
        val id: String,

        @JsonProperty("title")
        val title: String?,

        @JsonProperty("description")
        val description: String?,

        @JsonProperty("status")
        val status: TenderStatus?,

        @JsonProperty("statusDetails")
        val statusDetails: TenderStatusDetails?,

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