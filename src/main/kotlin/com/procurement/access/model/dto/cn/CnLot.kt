package com.procurement.access.model.dto.cn

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.model.dto.ocds.*
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CnLot(

        @JsonProperty("id") @NotNull
        var id: String?,

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

        @JsonProperty("variants") @Valid @NotEmpty
        val variants: List<Variant>?,

        @JsonProperty("renewals") @Valid @NotEmpty
        val renewals: List<Renewal>?,

        @JsonProperty("recurrentProcurement") @Valid @NotEmpty
        val recurrentProcurement: List<RecurrentProcurement>?,

        @JsonProperty("contractPeriod") @Valid @NotNull
        val contractPeriod: Period?,

        @param:JsonProperty("placeOfPerformance") @Valid @NotNull
        val placeOfPerformance: PlaceOfPerformance?
)