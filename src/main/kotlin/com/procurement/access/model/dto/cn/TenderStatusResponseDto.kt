package com.procurement.access.model.dto.cn

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

//@JsonPropertyOrder("status", "statusDetails")
@JsonInclude(value = JsonInclude.Include.NON_NULL)
data class TenderStatusResponseDto(

        @JsonProperty("status")
        val status: String?,

        @JsonProperty("statusDetails")
        val statusDetails: String?
)
