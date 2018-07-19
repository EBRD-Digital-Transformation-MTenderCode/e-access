package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import javax.validation.Valid
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Address @JsonCreator constructor(

        @field:NotNull
        val streetAddress: String,

        val postalCode: String?,

        @field:Valid @field:NotNull
        val addressDetails: AddressDetails
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AddressDetails(

        @field:Valid @field:NotNull
        val country: CountryDetails,

        @field:Valid @field:NotNull
        val region: RegionDetails,

        @field:Valid @field:NotNull
        val locality: LocalityDetails
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CountryDetails(

        var scheme: String?,

        @field:NotNull
        val id: String,

        var description: String?,

        var uri: String?
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RegionDetails(

        var scheme: String?,

        @field:NotNull
        val id: String,

        var description: String?,

        var uri: String?
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class LocalityDetails(

        @field:NotNull
        var scheme: String,

        @field:NotNull
        val id: String,

        @field:NotNull
        var description: String,

        var uri: String?
)