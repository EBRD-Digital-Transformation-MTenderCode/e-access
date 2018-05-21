package com.procurement.access.model.dto.cn

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.procurement.access.model.dto.ocds.Planning
import javax.validation.Valid

@JsonPropertyOrder("token", "ocid", "planning", "tender")
@JsonInclude(value = JsonInclude.Include.NON_NULL)
data class CnProcess(

        @JsonProperty("token")
        val token: String?,

        @JsonProperty("ocid")
        val ocId: String?,

        @JsonProperty("planning") @Valid
        val planning: Planning,

        @JsonProperty("tender") @Valid
        val tender: CnTender)
