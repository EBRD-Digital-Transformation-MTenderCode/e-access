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
             val placeOfPerformance: PlaceOfPerformance?
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
                             val uri: String
                        )
                    }
                }
            }
        }

        data class Item(
             val id: String,
             val relatedLot: String
        )
    }
}