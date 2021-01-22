package com.procurement.access.infrastructure.handler.v2.model.response


import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.amount.Amount
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.domain.model.lot.LotId
import java.math.BigDecimal
import java.time.LocalDateTime

data class DivideLotResult(
    @param:JsonProperty("tender") @field:JsonProperty("tender") val tender: Tender
) {
    data class Tender(
        @param:JsonProperty("lots") @field:JsonProperty("lots") val lots: List<Lot>,
        @param:JsonProperty("items") @field:JsonProperty("items") val items: List<Item>
    ) {
        data class Lot(
            @param:JsonProperty("id") @field:JsonProperty("id") val id: LotId,

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
            @param:JsonProperty("placeOfPerformance") @field:JsonProperty("placeOfPerformance") val placeOfPerformance: PlaceOfPerformance?,

            @param:JsonProperty("status") @field:JsonProperty("status") val status: LotStatus,
            @param:JsonProperty("statusDetails") @field:JsonProperty("statusDetails") val statusDetails: LotStatusDetails
        ) {
            data class Value(
                @param:JsonProperty("amount") @field:JsonProperty("amount") val amount: Amount,
                @param:JsonProperty("currency") @field:JsonProperty("currency") val currency: String
            )

            data class ContractPeriod(
                @param:JsonProperty("startDate") @field:JsonProperty("startDate") val startDate: LocalDateTime,
                @param:JsonProperty("endDate") @field:JsonProperty("endDate") val endDate: LocalDateTime
            )

            data class PlaceOfPerformance(
                @JsonInclude(JsonInclude.Include.NON_NULL)
                @param:JsonProperty("description") @field:JsonProperty("description") val description: String?,

                @param:JsonProperty("address") @field:JsonProperty("address") val address: Address
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
            }
        }

        data class Item(
            @param:JsonProperty("id") @field:JsonProperty("id") val id: String,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @param:JsonProperty("internalId") @field:JsonProperty("internalId") val internalId: String?,

            @param:JsonProperty("classification") @field:JsonProperty("classification") val classification: Classification,

            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            @param:JsonProperty("additionalClassifications") @field:JsonProperty("additionalClassifications") val additionalClassifications: List<AdditionalClassification>?,

            @param:JsonProperty("quantity") @field:JsonProperty("quantity") val quantity: BigDecimal,
            @param:JsonProperty("unit") @field:JsonProperty("unit") val unit: Unit,
            @param:JsonProperty("description") @field:JsonProperty("description") val description: String,
            @param:JsonProperty("relatedLot") @field:JsonProperty("relatedLot") val relatedLot: LotId
        ) {
            data class Classification(
                @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
                @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: String,
                @param:JsonProperty("description") @field:JsonProperty("description") val description: String
            )

            data class AdditionalClassification(
                @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
                @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: String,
                @param:JsonProperty("description") @field:JsonProperty("description") val description: String
            )

            data class Unit(
                @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
                @param:JsonProperty("name") @field:JsonProperty("name") val name: String
            )
        }
    }
}