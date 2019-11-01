package com.procurement.access.infrastructure.dto.lot

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.domain.model.money.Money
import com.procurement.access.infrastructure.bind.money.MoneyDeserializer
import com.procurement.access.infrastructure.bind.money.MoneySerializer
import com.procurement.access.model.dto.databinding.JsonDateTimeDeserializer
import com.procurement.access.model.dto.databinding.JsonDateTimeSerializer
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class GetLotResponse(
    @field:JsonProperty("lot") @param:JsonProperty("lot") val lot: Lot
) {

    data class Lot(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("internalId") @param:JsonProperty("internalId") val internalId: String?,

        @field:JsonProperty("title") @param:JsonProperty("title") val title: String,
        @field:JsonProperty("description") @param:JsonProperty("description") val description: String,
        @field:JsonProperty("status") @param:JsonProperty("status") val status: LotStatus,
        @field:JsonProperty("statusDetails") @param:JsonProperty("statusDetails") val statusDetails: LotStatusDetails,

        @JsonDeserialize(using = MoneyDeserializer::class)
        @JsonSerialize(using = MoneySerializer::class)
        @field:JsonProperty("value") @param:JsonProperty("value") val value: Money,

        @field:JsonProperty("options") @param:JsonProperty("options") val options: List<Option>,
        @field:JsonProperty("variants") @param:JsonProperty("variants") val variants: List<Variant>,
        @field:JsonProperty("renewals") @param:JsonProperty("renewals") val renewals: List<Renewal>,
        @field:JsonProperty("recurrentProcurement") @param:JsonProperty("recurrentProcurement") val recurrentProcurement: List<RecurrentProcurement>,
        @field:JsonProperty("contractPeriod") @param:JsonProperty("contractPeriod") val contractPeriod: ContractPeriod,
        @field:JsonProperty("placeOfPerformance") @param:JsonProperty("placeOfPerformance") val placeOfPerformance: PlaceOfPerformance
    ) {
        data class Option(
            @field:JsonProperty("hasOptions") @param:JsonProperty("hasOptions") val hasOptions: Boolean = false
        )

        data class Variant(
            @field:JsonProperty("hasVariants") @param:JsonProperty("hasVariants") val hasVariants: Boolean = false
        )

        data class Renewal(
            @field:JsonProperty("hasRenewals") @param:JsonProperty("hasRenewals") val hasRenewals: Boolean = false
        )

        data class RecurrentProcurement(
            @get:JsonProperty("isRecurrent") @param:JsonProperty("isRecurrent") val isRecurrent: Boolean
        )

        data class ContractPeriod(
            @JsonDeserialize(using = JsonDateTimeDeserializer::class)
            @JsonSerialize(using = JsonDateTimeSerializer::class)
            @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime,

            @JsonDeserialize(using = JsonDateTimeDeserializer::class)
            @JsonSerialize(using = JsonDateTimeSerializer::class)
            @field:JsonProperty("endDate") @param:JsonProperty("endDate") val endDate: LocalDateTime
        )

        data class PlaceOfPerformance(
            @field:JsonProperty("address") @param:JsonProperty("address") val address: Address,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @field:JsonProperty("description") @param:JsonProperty("description") val description: String?
        ) {

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
                        @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String,
                        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                        @field:JsonProperty("description") @param:JsonProperty("description") val description: String,
                        @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String
                    )

                    data class Region(
                        @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String,
                        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                        @field:JsonProperty("description") @param:JsonProperty("description") val description: String,
                        @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String
                    )

                    data class Locality(
                        @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String,
                        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                        @field:JsonProperty("description") @param:JsonProperty("description") val description: String,

                        @JsonInclude(JsonInclude.Include.NON_NULL)
                        @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String?
                    )
                }
            }
        }
    }
}
