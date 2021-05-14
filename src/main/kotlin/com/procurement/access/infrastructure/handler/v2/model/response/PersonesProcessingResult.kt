package com.procurement.access.infrastructure.handler.v2.model.response


import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.enums.BusinessFunctionDocumentType
import com.procurement.access.domain.model.enums.BusinessFunctionType
import com.procurement.access.domain.model.enums.MainGeneralActivity
import com.procurement.access.domain.model.enums.MainSectoralActivity
import com.procurement.access.domain.model.enums.PartyRole
import com.procurement.access.domain.model.enums.TypeOfBuyer
import com.procurement.access.domain.model.persone.PersonId
import java.time.LocalDateTime

data class PersonesProcessingResult(
    @param:JsonProperty("parties") @field:JsonProperty("parties") val parties: List<Party>
) {
    data class Party(
        @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
        @param:JsonProperty("name") @field:JsonProperty("name") val name: String,
        @param:JsonProperty("identifier") @field:JsonProperty("identifier") val identifier: Identifier,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @param:JsonProperty("additionalIdentifiers") @field:JsonProperty("additionalIdentifiers") val additionalIdentifiers: List<AdditionalIdentifier>?,

        @param:JsonProperty("address") @field:JsonProperty("address") val address: Address,
        @param:JsonProperty("contactPoint") @field:JsonProperty("contactPoint") val contactPoint: ContactPoint,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @param:JsonProperty("persones") @field:JsonProperty("persones") val persones: List<Persone>?,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @param:JsonProperty("details") @field:JsonProperty("details") val details: Details?,

        @param:JsonProperty("roles") @field:JsonProperty("roles") val roles: List<PartyRole>
    ) {
        data class Identifier(
            @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
            @param:JsonProperty("legalName") @field:JsonProperty("legalName") val legalName: String,
            @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: String,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @param:JsonProperty("uri") @field:JsonProperty("uri") val uri: String?
        )

        data class AdditionalIdentifier(
            @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
            @param:JsonProperty("legalName") @field:JsonProperty("legalName") val legalName: String,
            @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: String,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @param:JsonProperty("uri") @field:JsonProperty("uri") val uri: String?
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
                    @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
                    @param:JsonProperty("description") @field:JsonProperty("description") val description: String,
                    @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: String,
                    @param:JsonProperty("uri") @field:JsonProperty("uri") val uri: String
                )

                data class Region(
                    @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
                    @param:JsonProperty("description") @field:JsonProperty("description") val description: String,
                    @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: String,
                    @param:JsonProperty("uri") @field:JsonProperty("uri") val uri: String
                )

                data class Locality(
                    @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
                    @param:JsonProperty("description") @field:JsonProperty("description") val description: String,
                    @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: String,

                    @JsonInclude(JsonInclude.Include.NON_NULL)
                    @param:JsonProperty("uri") @field:JsonProperty("uri") val uri: String?
                )
            }
        }

        data class ContactPoint(
            @param:JsonProperty("name") @field:JsonProperty("name") val name: String,
            @param:JsonProperty("email") @field:JsonProperty("email") val email: String,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @param:JsonProperty("telephone") @field:JsonProperty("telephone") val telephone: String?,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @param:JsonProperty("faxNumber") @field:JsonProperty("faxNumber") val faxNumber: String?,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @param:JsonProperty("url") @field:JsonProperty("url") val url: String?
        )

        data class Persone(
            @param:JsonProperty("id") @field:JsonProperty("id") val id: PersonId,
            @param:JsonProperty("title") @field:JsonProperty("title") val title: String,
            @param:JsonProperty("name") @field:JsonProperty("name") val name: String,
            @param:JsonProperty("identifier") @field:JsonProperty("identifier") val identifier: Identifier,
            @param:JsonProperty("businessFunctions") @field:JsonProperty("businessFunctions") val businessFunctions: List<BusinessFunction>
        ) {
            data class Identifier(
                @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: String,
                @param:JsonProperty("id") @field:JsonProperty("id") val id: String,

                @JsonInclude(JsonInclude.Include.NON_NULL)
                @param:JsonProperty("uri") @field:JsonProperty("uri") val uri: String?
            )

            data class BusinessFunction(
                @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
                @param:JsonProperty("type") @field:JsonProperty("type") val type: BusinessFunctionType,
                @param:JsonProperty("jobTitle") @field:JsonProperty("jobTitle") val jobTitle: String,
                @param:JsonProperty("period") @field:JsonProperty("period") val period: Period,

                @JsonInclude(JsonInclude.Include.NON_EMPTY)
                @param:JsonProperty("documents") @field:JsonProperty("documents") val documents: List<Document>?
            ) {
                data class Period(
                    @param:JsonProperty("startDate") @field:JsonProperty("startDate") val startDate: LocalDateTime
                )

                data class Document(
                    @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
                    @param:JsonProperty("documentType") @field:JsonProperty("documentType") val documentType: BusinessFunctionDocumentType,
                    @param:JsonProperty("title") @field:JsonProperty("title") val title: String,

                    @JsonInclude(JsonInclude.Include.NON_NULL)
                    @param:JsonProperty("description") @field:JsonProperty("description") val description: String?
                )
            }
        }

        data class Details(
            @JsonInclude(JsonInclude.Include.NON_NULL)
            @param:JsonProperty("typeOfBuyer") @field:JsonProperty("typeOfBuyer") val typeOfBuyer: TypeOfBuyer?,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @param:JsonProperty("mainGeneralActivity") @field:JsonProperty("mainGeneralActivity") val mainGeneralActivity: MainGeneralActivity?,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @param:JsonProperty("mainSectoralActivity") @field:JsonProperty("mainSectoralActivity") val mainSectoralActivity: MainSectoralActivity?
        )
    }
}