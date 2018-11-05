package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Address @JsonCreator constructor(

        val streetAddress: String,

        val postalCode: String?,

        val addressDetails: AddressDetails
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AddressDetails(

        val country: CountryDetails,

        val region: RegionDetails,

        val locality: LocalityDetails
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CountryDetails(

        var scheme: String?,

        val id: String,

        var description: String?,

        var uri: String?
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RegionDetails(

        var scheme: String?,

        val id: String,

        var description: String?,

        var uri: String?
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class LocalityDetails(

        var scheme: String,

        val id: String,

        var description: String,

        var uri: String?
)