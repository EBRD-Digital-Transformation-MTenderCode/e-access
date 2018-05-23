package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
//@JsonPropertyOrder("id", "description", "scheme", "uri")
data class Classification(

        @param:JsonProperty("scheme")
        val scheme: Scheme,

        @param:JsonProperty("id")
        val id: String,

        @param:JsonProperty("description")
        val description: String,

        @param:JsonProperty("uri")
        val uri: String?
)
