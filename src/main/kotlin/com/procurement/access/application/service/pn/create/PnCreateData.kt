package com.procurement.access.application.service.pn.create

import com.procurement.access.domain.model.CPVCode
import com.procurement.access.domain.model.enums.DocumentType
import com.procurement.access.domain.model.enums.LegalBasis
import com.procurement.access.domain.model.enums.MainProcurementCategory
import com.procurement.access.domain.model.enums.Scheme
import com.procurement.access.domain.model.money.Money
import java.math.BigDecimal
import java.time.LocalDateTime

class PnCreateData(
    val planning: Planning,
    val tender: Tender
) {

    data class Planning(
        val rationale: String?,
        val budget: Budget
    ) {

        data class Budget(
            val description: String?,
            val amount: Money,
            val isEuropeanUnionFunded: Boolean,
            val budgetBreakdowns: List<BudgetBreakdown>
        ) {

            data class BudgetBreakdown(
                val id: String,
                val description: String?,
                val amount: Money,
                val period: Period,
                val sourceParty: SourceParty,
                val europeanUnionFunding: EuropeanUnionFunding?
            ) {

                data class Period(
                    val startDate: LocalDateTime,
                    val endDate: LocalDateTime
                )

                data class SourceParty(
                    val name: String,
                    val id: String
                )

                data class EuropeanUnionFunding(
                    val projectIdentifier: String,
                    val projectName: String,
                    val uri: String?
                )
            }
        }
    }

    data class Tender(
        val title: String,
        val description: String,
        val classification: Classification,
        val mainProcurementCategory: MainProcurementCategory,
        val legalBasis: LegalBasis,
        val procurementMethodDetails: String,
        val procurementMethodRationale: String?,
        val procurementMethodAdditionalInfo: String?,
        val eligibilityCriteria: String,
        val tenderPeriod: TenderPeriod,
        val procuringEntity: ProcuringEntity,
        val lots: List<Lot>,
        val items: List<Item>,
        val submissionMethodRationale: List<String>,
        val submissionMethodDetails: String,
        val documents: List<Document>
    ) {
        data class Classification(
            val scheme: Scheme,
            val id: CPVCode,
            val description: String
        )

        data class TenderPeriod(
            val startDate: LocalDateTime
        )

        data class ProcuringEntity(
            val name: String,
            val identifier: Identifier,
            val additionalIdentifiers: List<AdditionalIdentifier>,
            val address: Address,
            val contactPoint: ContactPoint
        ) {

            data class Identifier(
                val scheme: String,
                val id: String,
                val legalName: String,
                val uri: String?
            )

            data class AdditionalIdentifier(
                val scheme: String,
                val id: String,
                val legalName: String,
                val uri: String?
            )

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

            data class ContactPoint(
                val name: String,
                val email: String,
                val telephone: String,
                val faxNumber: String?,
                val url: String?
            )
        }

        data class Lot(
            val id: String,
            val internalId: String?,
            val title: String,
            val description: String,
            val value: Money,
            val contractPeriod: ContractPeriod,
            val placeOfPerformance: PlaceOfPerformance
        ) {

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
            val id: String,
            val internalId: String?,
            val classification: Classification,
            val additionalClassifications: List<AdditionalClassification>,
            val quantity: BigDecimal,
            val unit: Unit,
            val description: String,
            val relatedLot: String
        ) {
            data class Classification(
                val scheme: Scheme,
                val id: CPVCode,
                val description: String
            )

            data class AdditionalClassification(
                val scheme: Scheme,
                val id: CPVCode,
                val description: String
            )

            data class Unit(
                val id: String,
                val name: String
            )
        }

        data class Document(
            val id: String,
            val documentType: DocumentType,
            val title: String,
            val description: String?,
            val relatedLots: List<String>
        )
    }
}
