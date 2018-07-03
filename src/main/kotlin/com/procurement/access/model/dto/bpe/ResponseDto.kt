package com.procurement.access.model.bpe

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

data class ResponseDto(

        @get:JsonProperty("success")
        val success: Boolean,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        val details: List<ResponseDetailsDto>?,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        val data: Any?
)
