package com.procurement.access.application.model.params


import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.amount.Amount
import java.time.LocalDateTime

data class DivideLotParams(
     val cpid: Cpid,
     val ocid: Ocid,
     val tender: Tender
) {
    data class Tender(
         val lots: List<Lot>,
         val items: List<Item>
    ) {
        data class Lot(
             val id: String,
             val internalId: String?,
             val title: String?,
             val description: String?,
             val value: Value?,
             val contractPeriod: ContractPeriod?,
             val placeOfPerformance: PlaceOfPerformance?,
             val hasOptions: Boolean?,
             val options: List<Option>,
             val hasRecurrence: Boolean?,
             val recurrence: Recurrence?,
             val hasRenewal: Boolean?,
             val renewal: Renewal?
        ) {
            data class Value(
                 val amount: Amount,
                 val currency: String
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

            data class Option(
                val description: String?,
                val period: Period?
            ) {
                data class Period(
                    val durationInDays: Int?,
                    val startDate: LocalDateTime?,
                    val endDate: LocalDateTime?,
                    val maxExtentDate: LocalDateTime?
                )
            }

            data class Recurrence(
                val dates: List<Date>?,
                val description: String?
            ) {
                data class Date(
                    val startDate: LocalDateTime?
                )
            }

            data class Renewal(
                val description: String?,
                val minimumRenewals: Long?,
                val maximumRenewals: Long?,
                val period: Period?
            ) {
                data class Period(
                    val durationInDays: Int?,
                    val startDate: LocalDateTime?,
                    val endDate: LocalDateTime?,
                    val maxExtentDate: LocalDateTime?
                )
            }
        }

        data class Item(
             val id: String,
             val relatedLot: String
        )
    }
}