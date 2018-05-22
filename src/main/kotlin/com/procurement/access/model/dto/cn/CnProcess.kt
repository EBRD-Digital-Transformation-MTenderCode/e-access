package com.procurement.access.model.dto.cn

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.procurement.access.model.dto.ocds.Planning
import javax.validation.Valid

//@JsonPropertyOrder("token", "ocid", "planning", "tender")
@JsonInclude(value = JsonInclude.Include.NON_NULL)
data class CnProcess(

        @JsonProperty("token")
        var token: String?,

        @JsonProperty("ocid")
        var ocId: String?,

        @JsonProperty("planning") @Valid
        var planning: Planning,

        @JsonProperty("tender") @Valid
        var tender: CnTender)
