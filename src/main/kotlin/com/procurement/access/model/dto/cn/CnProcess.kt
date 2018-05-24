package com.procurement.access.model.dto.cn

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.procurement.access.model.dto.ocds.Planning
import javax.validation.Valid
import javax.validation.constraints.NotNull

@JsonInclude(value = JsonInclude.Include.NON_NULL)
data class CnProcess(

        @JsonProperty("ocid")
        var ocid: String?,

        @JsonProperty("token")
        var token: String?,

        @JsonProperty("planning") @Valid @NotNull
        var planning: Planning,

        @JsonProperty("tender") @Valid @NotNull
        var tender: CnTender)
