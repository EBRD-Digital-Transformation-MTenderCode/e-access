package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Address(

        @JsonProperty("streetAddress") @NotNull
        val streetAddress: String,

        @JsonProperty("locality") @NotNull
        val locality: String,

        @JsonProperty("region") @NotNull
        val region: String,

        @JsonProperty("postalCode")
        val postalCode: String?,

        @JsonProperty("countryName") @NotNull
        val countryName: String
)