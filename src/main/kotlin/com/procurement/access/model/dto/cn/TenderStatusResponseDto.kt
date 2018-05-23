package com.procurement.access.model.dto.cn

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(value = JsonInclude.Include.NON_NULL)
data class TenderStatusResponseDto(

        @JsonProperty("status")
        val status: String?,

        @JsonProperty("statusDetails")
        val statusDetails: String?
)
