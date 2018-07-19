package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PlaceOfPerformance @JsonCreator constructor(

        val address: AddressPlaceOfPerformance,

        val description: String?
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AddressPlaceOfPerformance @JsonCreator constructor(

        @field:NotNull
        val streetAddress: String,

        @field:NotNull
        val locality: String,

        @field:NotNull
        val region: String,

        val postalCode: String?,

        @field:NotNull
        val countryName: String
)