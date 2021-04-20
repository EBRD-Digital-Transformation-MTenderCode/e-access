package com.procurement.access.infrastructure.handler.v1.model.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.infrastructure.entity.RfqEntity
import com.procurement.access.model.dto.databinding.MoneyDeserializer
import com.procurement.access.model.dto.ocds.Address
import com.procurement.access.model.dto.ocds.AddressDetails
import com.procurement.access.model.dto.ocds.ContractPeriod
import com.procurement.access.model.dto.ocds.CountryDetails
import com.procurement.access.model.dto.ocds.LocalityDetails
import com.procurement.access.model.dto.ocds.Lot
import com.procurement.access.model.dto.ocds.Option
import com.procurement.access.model.dto.ocds.PlaceOfPerformance
import com.procurement.access.model.dto.ocds.RecurrentProcurement
import com.procurement.access.model.dto.ocds.RegionDetails
import com.procurement.access.model.dto.ocds.Renewal
import com.procurement.access.model.dto.ocds.Variant
import java.math.BigDecimal

data class UpdateLotByBidRq @JsonCreator constructor(
    val lotId: String,
    val lotAwarded: Boolean
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UpdateLotByBidRs @JsonCreator constructor(
    val lot: Lot
) {
    companion object {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class Lot @JsonCreator constructor(

        val id: String,
        val internalId: String?,
        val title: String?,
        val description: String?,
        val status: LotStatus?,
        val statusDetails: LotStatusDetails?,
        val value: Value,
        val options: List<Option>?,
        val recurrentProcurement: List<RecurrentProcurement>?,
        val renewals: List<Renewal>?,
        val variants: List<Variant>?,
        val contractPeriod: ContractPeriod?,
        val placeOfPerformance: PlaceOfPerformance?
    ) {

        @JsonInclude(JsonInclude.Include.NON_NULL)
        data class Value @JsonCreator constructor(

            @field:JsonDeserialize(using = MoneyDeserializer::class)
            val amount: BigDecimal?,

            val currency: String
        )

    }
}

fun UpdateLotByBidRs.Companion.fromDomain(lot: Lot): UpdateLotByBidRs =
    UpdateLotByBidRs(
        lot = UpdateLotByBidRs.Lot(
            id = lot.id,
            internalId = lot.internalId,
            title = lot.title,
            description = lot.description,
            status = lot.status,
            statusDetails = lot.statusDetails,
            value = lot.value.let { value ->
                UpdateLotByBidRs.Lot.Value(
                    amount = null,
                    currency = value.currency
                )
            },
            options = lot.options,
            recurrentProcurement = lot.recurrentProcurement,
            renewals = lot.renewals,
            variants = lot.variants,
            contractPeriod = lot.contractPeriod,
            placeOfPerformance = lot.placeOfPerformance
        )
    )

fun UpdateLotByBidRs.Companion.fromDomain(lot: RfqEntity.Tender.Lot): UpdateLotByBidRs =
    UpdateLotByBidRs(
        lot = UpdateLotByBidRs.Lot(
            id = lot.id.toString(),
            internalId = lot.internalId,
            title = lot.title,
            description = lot.description,
            status = lot.status,
            statusDetails = lot.statusDetails,
            value = lot.value.let { value ->
                UpdateLotByBidRs.Lot.Value(
                    amount = null,
                    currency = value.currency
                )
            },
            options = emptyList(),
            recurrentProcurement = emptyList(),
            renewals = emptyList(),
            variants = emptyList(),
            contractPeriod = lot.contractPeriod.let { period ->
                ContractPeriod(
                    startDate = period.startDate,
                    endDate = period.endDate
                )
            },
            placeOfPerformance = lot.placeOfPerformance.let { placeOfPerformance ->
                PlaceOfPerformance(
                    description = placeOfPerformance.description,
                    address = placeOfPerformance.address
                        .let { address ->
                            Address(
                                streetAddress = address.streetAddress,
                                postalCode = address.postalCode,
                                addressDetails = address.addressDetails
                                    .let { addressDetails ->
                                        AddressDetails(
                                            country = addressDetails.country
                                                .let { country ->
                                                    CountryDetails(
                                                        scheme = country.scheme,
                                                        id = country.id,
                                                        description = country.description,
                                                        uri = country.uri
                                                    )
                                                },
                                            region = addressDetails.region
                                                .let { region ->
                                                    RegionDetails(
                                                        scheme = region.scheme,
                                                        id = region.id,
                                                        description = region.description,
                                                        uri = region.uri
                                                    )
                                                },
                                            locality = addressDetails.locality
                                                .let { locality ->
                                                    LocalityDetails(
                                                        scheme = locality.scheme,
                                                        id = locality.id,
                                                        description = locality.description,
                                                        uri = locality.uri
                                                    )
                                                }
                                        )
                                    }
                            )
                        }
                )
            }
        )
    )
