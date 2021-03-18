package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.procurement.access.infrastructure.entity.process.RelatedProcess
import com.procurement.access.model.dto.databinding.BooleansDeserializer

@JsonInclude(JsonInclude.Include.NON_NULL)
@Deprecated("Use 'CNEntity' instead of this")
data class TenderProcess @JsonCreator constructor(

    val ocid: String?,

    var token: String?,

    @field:JsonDeserialize(using = BooleansDeserializer::class)
    @get:JsonProperty("isLotsChanged")
    var isLotsChanged: Boolean? = null,

    var amendment: Amendment? = null,

    val planning: Planning,

    val tender: Tender,

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @field:JsonProperty("relatedProcesses") @param:JsonProperty("relatedProcesses") val relatedProcesses: List<RelatedProcess>?,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @param:JsonProperty("buyer") @field:JsonProperty("buyer") val buyer: Buyer? = null
) {

    data class Buyer(
        @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
        @param:JsonProperty("name") @field:JsonProperty("name") val name: String,
        @param:JsonProperty("identifier") @field:JsonProperty("identifier") val identifier: Identifier,
        @param:JsonProperty("address") @field:JsonProperty("address") val address: Address,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @param:JsonProperty("additionalIdentifiers") @field:JsonProperty("additionalIdentifiers") val additionalIdentifiers: List<AdditionalIdentifier>?,

        @param:JsonProperty("contactPoint") @field:JsonProperty("contactPoint") val contactPoint: ContactPoint,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @param:JsonProperty("details") @field:JsonProperty("details") val details: Details?
    ) {
        data class Identifier(
            @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
            @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: String,
            @param:JsonProperty("legalName") @field:JsonProperty("legalName") val legalName: String,
            @param:JsonProperty("uri") @field:JsonProperty("uri") val uri: String
        )

        data class Address(
            @param:JsonProperty("streetAddress") @field:JsonProperty("streetAddress") val streetAddress: String,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @param:JsonProperty("postalCode") @field:JsonProperty("postalCode") val postalCode: String?,

            @param:JsonProperty("addressDetails") @field:JsonProperty("addressDetails") val addressDetails: AddressDetails
        ) {
            data class AddressDetails(
                @param:JsonProperty("country") @field:JsonProperty("country") val country: Country,
                @param:JsonProperty("region") @field:JsonProperty("region") val region: Region,
                @param:JsonProperty("locality") @field:JsonProperty("locality") val locality: Locality
            ) {
                data class Country(
                    @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: String,
                    @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
                    @param:JsonProperty("description") @field:JsonProperty("description") val description: String,
                    @param:JsonProperty("uri") @field:JsonProperty("uri") val uri: String
                )

                data class Region(
                    @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: String,
                    @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
                    @param:JsonProperty("description") @field:JsonProperty("description") val description: String,
                    @param:JsonProperty("uri") @field:JsonProperty("uri") val uri: String
                )

                data class Locality(
                    @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: String,
                    @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
                    @param:JsonProperty("description") @field:JsonProperty("description") val description: String,

                    @JsonInclude(JsonInclude.Include.NON_NULL)
                    @param:JsonProperty("uri") @field:JsonProperty("uri") val uri: String?
                )
            }
        }

        data class AdditionalIdentifier(
            @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
            @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: String,
            @param:JsonProperty("legalName") @field:JsonProperty("legalName") val legalName: String,
            @param:JsonProperty("uri") @field:JsonProperty("uri") val uri: String
        )

        data class ContactPoint(
            @param:JsonProperty("name") @field:JsonProperty("name") val name: String,
            @param:JsonProperty("email") @field:JsonProperty("email") val email: String,
            @param:JsonProperty("telephone") @field:JsonProperty("telephone") val telephone: String,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @param:JsonProperty("faxNumber") @field:JsonProperty("faxNumber") val faxNumber: String?,

            @param:JsonProperty("url") @field:JsonProperty("url") val url: String
        )

        data class Details(
            @JsonInclude(JsonInclude.Include.NON_NULL)
            @param:JsonProperty("typeOfBuyer") @field:JsonProperty("typeOfBuyer") val typeOfBuyer: TypeOfBuyer?,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @param:JsonProperty("mainGeneralActivity") @field:JsonProperty("mainGeneralActivity") val mainGeneralActivity: String?,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @param:JsonProperty("mainSectoralActivity") @field:JsonProperty("mainSectoralActivity") val mainSectoralActivity: String?
        )
    }
}

data class Amendment @JsonCreator constructor(

    val relatedLots: Set<String>
)