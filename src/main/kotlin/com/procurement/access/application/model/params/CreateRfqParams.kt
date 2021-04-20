package com.procurement.access.application.model.params

import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.owner.Owner
import java.math.BigDecimal
import java.time.LocalDateTime

data class CreateRfqParams(
    val date: LocalDateTime,
    val owner: Owner,
    val tender: Tender,
    val cpid: Cpid,
    val relatedCpid: Cpid,
    val relatedOcid: Ocid.SingleStage,
    val additionalCpid: Cpid,
    val additionalOcid: Ocid.SingleStage
) {
    data class Tender(
        val title: String,
        val description: String,
        val lots: List<Lot>,
        val items: List<Item>,
        val electronicAuctions: ElectronicAuctions?,
        val procurementMethodModalities: List<String>?
    ) {
        data class Lot(
            val id: String,
            val internalId: String?,
            val title: String,
            val description: String?,
            val value: Value,
            val contractPeriod: ContractPeriod,
            val placeOfPerformance: PlaceOfPerformance
        ) {
            data class Value(
                val currency: String
            )

            data class ContractPeriod(
                val startDate: LocalDateTime,
                val endDate: LocalDateTime
            )

            data class PlaceOfPerformance(
                val description: String?,

                val address: Address
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
                            val id: String,
                            val description: String,
                            val scheme: String,
                            val uri: String
                        )

                        data class Region(
                            val id: String,
                            val description: String,
                            val scheme: String,
                            val uri: String
                        )

                        data class Locality(
                            val id: String,
                            val description: String,
                            val scheme: String,
                            val uri: String?
                        )
                    }
                }
            }
        }

        data class Item(
            val id: String,
            val internalId: String?,
            val description: String,
            val classification: Classification,
            val quantity: BigDecimal,
            val unit: Unit,
            val relatedLot: String
        ) {
            data class Classification(
                val id: String,
                val scheme: String,
                val description: String
            )

            data class Unit(
                val id: String,
                val name: String
            )
        }

        data class ElectronicAuctions(
            val details: List<Detail>
        ) {
            data class Detail(
                val id: String,
                val relatedLot: String
            )
        }
    }
}