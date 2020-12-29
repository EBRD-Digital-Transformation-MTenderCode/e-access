package com.procurement.access.infrastructure.handler.v2.model.request


import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class ValidateLotsDataRequest(
    @param:JsonProperty("cpid") @field:JsonProperty("cpid") val cpid: String,
    @param:JsonProperty("ocid") @field:JsonProperty("ocid") val ocid: String,
    @param:JsonProperty("tender") @field:JsonProperty("tender") val tender: Tender
) {
    data class Tender(
        @param:JsonProperty("lots") @field:JsonProperty("lots") val lots: List<Lot>,
        @param:JsonProperty("items") @field:JsonProperty("items") val items: List<Item>
    ) {
        data class Lot(
            @param:JsonProperty("id") @field:JsonProperty("id") val id: String,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @param:JsonProperty("internalId") @field:JsonProperty("internalId") val internalId: String?,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @param:JsonProperty("title") @field:JsonProperty("title") val title: String?,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @param:JsonProperty("description") @field:JsonProperty("description") val description: String?,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @param:JsonProperty("value") @field:JsonProperty("value") val value: Value?,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @param:JsonProperty("contractPeriod") @field:JsonProperty("contractPeriod") val contractPeriod: ContractPeriod?,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @param:JsonProperty("placeOfPerformance") @field:JsonProperty("placeOfPerformance") val placeOfPerformance: PlaceOfPerformance?
        ) {
            data class Value(
                @param:JsonProperty("amount") @field:JsonProperty("amount") val amount: BigDecimal,
                @param:JsonProperty("currency") @field:JsonProperty("currency") val currency: String
            )

            data class ContractPeriod(
                @param:JsonProperty("startDate") @field:JsonProperty("startDate") val startDate: String,
                @param:JsonProperty("endDate") @field:JsonProperty("endDate") val endDate: String
            )

            data class PlaceOfPerformance(
                @param:JsonProperty("address") @field:JsonProperty("address") val address: Address,

                @JsonInclude(JsonInclude.Include.NON_NULL)
                @param:JsonProperty("description") @field:JsonProperty("description") val description: String?
            ) {
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
                            @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: String
                        )

                        data class Region(
                            @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
                            @param:JsonProperty("description") @field:JsonProperty("description") val description: String,
                            @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: String
                        )

                        data class Locality(
                            @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
                            @param:JsonProperty("description") @field:JsonProperty("description") val description: String,
                            @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: String
                        )
                    }
                }
            }
        }

        data class Item(
            @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
            @param:JsonProperty("relatedLot") @field:JsonProperty("relatedLot") val relatedLot: String
        )
    }
}