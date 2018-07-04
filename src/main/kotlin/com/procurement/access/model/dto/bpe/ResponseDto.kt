package com.procurement.access.model.bpe

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.procurement.access.model.dto.databinding.BooleansDeserializer

data class ResponseDto(

        @field:JsonDeserialize(using = BooleansDeserializer::class)
        @get:JsonProperty("success")
        val success: Boolean,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        val details: List<ResponseDetailsDto>?,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        val data: Any?
)
