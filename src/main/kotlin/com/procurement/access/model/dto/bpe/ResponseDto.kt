package com.procurement.notice.model.bpe

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

data class ResponseDto<out T>(

        @JsonProperty("success")
        @get:JsonProperty("success")
        val success: Boolean,

        @JsonProperty("details")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        val details: List<ResponseDetailsDto>?,

        @JsonProperty("data")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        val data: T?
)
