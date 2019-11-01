package com.procurement.access.application.service.lot

import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.domain.model.money.Money
import java.time.LocalDateTime

data class GettedLot(
    val id: String,
    val internalId: String?,
    val title: String,
    val description: String,
    val status: LotStatus,
    val statusDetails: LotStatusDetails,
    val value: Money,
    val options: List<Option>,
    val variants: List<Variant>,
    val renewals: List<Renewal>,
    val recurrentProcurement: List<RecurrentProcurement>,
    val contractPeriod: ContractPeriod,
    val placeOfPerformance: PlaceOfPerformance
) {
    data class Option(
        val hasOptions: Boolean = false
    )

    data class Variant(
        val hasVariants: Boolean = false
    )

    data class Renewal(
        val hasRenewals: Boolean = false
    )

    data class RecurrentProcurement(
        val isRecurrent: Boolean
    )

    data class ContractPeriod(
        val startDate: LocalDateTime,
        val endDate: LocalDateTime
    )

    data class PlaceOfPerformance(
        val address: Address,
        val description: String?
    ) {

        data class Address(
            val streetAddress: String,
            val postalCode: String?,
            val addressDetails: AddressDetails
        ) {

            data class AddressDetails(
                val country: Country,
                val region: Region,
                val locality: Locality
            ) {

                data class Country(
                    val scheme: String,
                    val id: String,
                    val description: String,
                    val uri: String
                )

                data class Region(
                    val scheme: String,
                    val id: String,
                    val description: String,
                    val uri: String
                )

                data class Locality(
                    val scheme: String,
                    val id: String,
                    val description: String,
                    val uri: String?
                )
            }
        }
    }
}
