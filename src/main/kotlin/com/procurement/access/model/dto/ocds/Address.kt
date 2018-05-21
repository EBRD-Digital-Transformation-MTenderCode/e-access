package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("streetAddress", "locality", "region", "postalCode", "countryName")
data class Address(

        @JsonProperty("streetAddress")
        val streetAddress: String,

        @JsonProperty("locality")
        val locality: String,

        @JsonProperty("region")
        val region: String,

        @JsonProperty("postalCode")
        val postalCode: String?,

        @JsonProperty("countryName")
        val countryName: String
)