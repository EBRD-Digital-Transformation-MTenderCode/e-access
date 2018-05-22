package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonInclude(JsonInclude.Include.NON_NULL)
//@JsonPropertyOrder("id", "scheme", "legalName", "uri")
data class Identifier(

        @JsonProperty("id")
        val id: String,

        @JsonProperty("scheme")
        val scheme: String,

        @JsonProperty("legalName")
        val legalName: String,

        @JsonProperty("uri")
        val uri: String
)