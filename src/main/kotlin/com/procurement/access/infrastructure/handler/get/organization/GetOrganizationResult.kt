package com.procurement.access.infrastructure.handler.get.organization

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.document.DocumentId
import com.procurement.access.domain.model.enums.BusinessFunctionDocumentType
import com.procurement.access.domain.model.enums.BusinessFunctionType
import com.procurement.access.domain.model.persone.PersonId
import java.time.LocalDateTime

data class GetOrganizationResult(
    @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
    @field:JsonProperty("name") @param:JsonProperty("name") val name: String,
    @field:JsonProperty("identifier") @param:JsonProperty("identifier") val identifier: Identifier,

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @field:JsonProperty("additionalIdentifiers") @param:JsonProperty("additionalIdentifiers") val additionalIdentifiers: List<AdditionalIdentifier>?,

    @field:JsonProperty("address") @param:JsonProperty("address") val address: Address,
    @field:JsonProperty("contactPoint") @param:JsonProperty("contactPoint") val contactPoint: ContactPoint,

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @field:JsonProperty("persones") @param:JsonProperty("persones") val persons: List<Person>?
) {
    data class Identifier(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
        @field:JsonProperty("legalName") @param:JsonProperty("legalName") val legalName: String,
        @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String?
    )

    data class AdditionalIdentifier(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
        @field:JsonProperty("legalName") @param:JsonProperty("legalName") val legalName: String,
        @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String?
    )

    data class Address(
        @field:JsonProperty("streetAddress") @param:JsonProperty("streetAddress") val streetAddress: String,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("postalCode") @param:JsonProperty("postalCode") val postalCode: String?,

        @field:JsonProperty("addressDetails") @param:JsonProperty("addressDetails") val addressDetails: AddressDetails
    ) {
        data class AddressDetails(
            @field:JsonProperty("country") @param:JsonProperty("country") val country: Country,
            @field:JsonProperty("region") @param:JsonProperty("region") val region: Region,
            @field:JsonProperty("locality") @param:JsonProperty("locality") val locality: Locality
        ) {
            data class Country(
                @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                @field:JsonProperty("description") @param:JsonProperty("description") val description: String,
                @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String,
                @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String
            )

            data class Region(
                @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                @field:JsonProperty("description") @param:JsonProperty("description") val description: String,
                @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String,
                @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String
            )

            data class Locality(
                @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                @field:JsonProperty("description") @param:JsonProperty("description") val description: String,
                @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String,

                @JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String?
            )
        }
    }

    data class ContactPoint(
        @field:JsonProperty("name") @param:JsonProperty("name") val name: String,
        @field:JsonProperty("email") @param:JsonProperty("email") val email: String,
        @field:JsonProperty("telephone") @param:JsonProperty("telephone") val telephone: String,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("faxNumber") @param:JsonProperty("faxNumber") val faxNumber: String?,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("url") @param:JsonProperty("url") val url: String?
    )

    data class Person(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: PersonId,
        @field:JsonProperty("title") @param:JsonProperty("title") val title: String,
        @field:JsonProperty("name") @param:JsonProperty("name") val name: String,
        @field:JsonProperty("identifier") @param:JsonProperty("identifier") val identifier: Identifier,
        @field:JsonProperty("businessFunctions") @param:JsonProperty("businessFunctions") val businessFunctions: List<BusinessFunction>
    ) {
        data class Identifier(
            @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String,
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String?
        )

        data class BusinessFunction(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
            @field:JsonProperty("type") @param:JsonProperty("type") val type: BusinessFunctionType,
            @field:JsonProperty("jobTitle") @param:JsonProperty("jobTitle") val jobTitle: String,
            @field:JsonProperty("period") @param:JsonProperty("period") val period: Period,

            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            @field:JsonProperty("documents") @param:JsonProperty("documents") val documents: List<Document>?
        ) {
            data class Period(
                @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime
            )

            data class Document(
                @field:JsonProperty("id") @param:JsonProperty("id") val id: DocumentId,
                @field:JsonProperty("documentType") @param:JsonProperty("documentType") val documentType: BusinessFunctionDocumentType,
                @field:JsonProperty("title") @param:JsonProperty("title") val title: String,

                @JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("description") @param:JsonProperty("description") val description: String?
            )
        }
    }
}
