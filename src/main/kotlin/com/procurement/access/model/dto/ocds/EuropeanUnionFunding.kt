package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
//@JsonPropertyOrder("projectIdentifier", "projectName", "uri")
data class EuropeanUnionFunding(

        @JsonProperty("projectIdentifier")
        val projectIdentifier: String,

        @JsonProperty("projectName")
        val projectName: String,

        @JsonProperty("uri")
        val uri: String?
)