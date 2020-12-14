package com.procurement.access.application.service.cn.update

import com.procurement.access.domain.model.EntityBase
import com.procurement.access.domain.model.enums.BusinessFunctionDocumentType
import com.procurement.access.domain.model.enums.BusinessFunctionType
import com.procurement.access.domain.model.enums.DocumentType
import com.procurement.access.domain.model.enums.ProcurementMethodModalities
import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.domain.model.lot.RelatedLot
import com.procurement.access.domain.model.lot.RelatedLots
import com.procurement.access.domain.model.money.Money
import java.math.BigDecimal
import java.time.LocalDateTime

data class UpdateOpenCnData(
    val planning: Planning?,
    val tender: Tender
) {
    data class Planning(
        val rationale: String?,
        val budget: Budget?
    ) {

        data class Budget(
            val description: String
        )
    }

    data class Tender(
        val title: String,
        val description: String,
        val procurementMethodRationale: String?,
        val procurementMethodAdditionalInfo: String?,
        val tenderPeriod: TenderPeriod,
        val enquiryPeriod: EnquiryPeriod?,
        val procurementMethodModalities: List<ProcurementMethodModalities>?,
        val electronicAuctions: ElectronicAuctions?,
        val procuringEntity: ProcuringEntity?,
        val lots: List<Lot>,
        val items: List<Item>,
        val documents: List<Document>
    ) {

        data class TenderPeriod(
            val startDate: LocalDateTime,
            val endDate: LocalDateTime
        )

        data class EnquiryPeriod(
            val startDate: LocalDateTime,
            val endDate: LocalDateTime
        )

        data class ElectronicAuctions(
            val details: List<Detail>
        ) {

            data class Detail(
                override val id: String,
                override val relatedLot: LotId,
                val electronicAuctionModalities: List<ElectronicAuctionModality>
            ) : EntityBase<String>(), RelatedLot<LotId> {

                data class ElectronicAuctionModality(
                    val eligibleMinimumDifference: Money
                )
            }
        }

        data class ProcuringEntity(
            override val id: String,
            val persons: List<Person>
        ) : EntityBase<String>() {

            data class Person(
                val title: String,
                val name: String,
                val identifier: Identifier,
                val businessFunctions: List<BusinessFunction>
            ) {

                data class Identifier(
                    val scheme: String,
                    val id: String,
                    val uri: String?
                )

                data class BusinessFunction(
                    override val id: String,
                    val type: BusinessFunctionType,
                    val jobTitle: String,
                    val period: Period,
                    val documents: List<Document>
                ) : EntityBase<String>() {

                    data class Period(
                        val startDate: LocalDateTime
                    )

                    data class Document(
                        override val id: String,
                        val documentType: BusinessFunctionDocumentType,
                        val title: String,
                        val description: String?
                    ) : EntityBase<String>()
                }
            }
        }

        data class Lot(
            override val id: LotId,
            val internalId: String?,
            val title: String,
            val description: String,
            val value: Money,
            val contractPeriod: ContractPeriod,
            val placeOfPerformance: PlaceOfPerformance
        ) : EntityBase<LotId>() {

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

        data class Item(
            override val id: String,
            val internalId: String?,
            val description: String,
            override val relatedLot: LotId,
            val quantity: BigDecimal,
            val additionalClassifications: List<AdditionalClassification>,
            val unit: Unit
        ) : EntityBase<String>(), RelatedLot<LotId> {

            data class AdditionalClassification(
                val id: String,
                val scheme: String,
                val description: String
            )

            data class Unit(
                val id: String,
                val name: String
            )
        }

        data class Document(
            val documentType: DocumentType,
            override val id: String,
            val title: String?,
            val description: String?,
            override val relatedLots: List<LotId>
        ) : EntityBase<String>(), RelatedLots<LotId>
    }
}
