package com.procurement.access.infrastructure.handler.v1.model.request

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.access.domain.model.CPVCode
import com.procurement.access.domain.model.enums.DocumentType
import com.procurement.access.domain.model.enums.LegalBasis
import com.procurement.access.domain.model.enums.MainProcurementCategory
import com.procurement.access.domain.model.enums.Scheme
import com.procurement.access.domain.model.enums.TypeOfBuyer
import com.procurement.access.domain.model.money.Money
import com.procurement.access.infrastructure.bind.money.MoneyDeserializer
import com.procurement.access.infrastructure.bind.money.MoneySerializer
import com.procurement.access.infrastructure.bind.quantity.QuantityDeserializer
import com.procurement.access.infrastructure.bind.quantity.QuantitySerializer


import java.math.BigDecimal
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
class PnCreateRequest(
    @field:JsonProperty("planning") @param:JsonProperty("planning") val planning: Planning,
    @field:JsonProperty("tender") @param:JsonProperty("tender") val tender: Tender,
    @param:JsonProperty("buyer") @field:JsonProperty("buyer") val buyer: Buyer
) {

    data class Planning(
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("rationale") @param:JsonProperty("rationale") val rationale: String?,
        @field:JsonProperty("budget") @param:JsonProperty("budget") val budget: Budget
    ) {

        data class Budget(
            @JsonInclude(JsonInclude.Include.NON_NULL)
            @field:JsonProperty("description") @param:JsonProperty("description") val description: String?,

            @JsonDeserialize(using = MoneyDeserializer::class)
            @JsonSerialize(using = MoneySerializer::class)
            @field:JsonProperty("amount") @param:JsonProperty("amount") val amount: Money,

            @get:JsonProperty("isEuropeanUnionFunded") @param:JsonProperty("isEuropeanUnionFunded") val isEuropeanUnionFunded: Boolean,
            @field:JsonProperty("budgetBreakdown") @param:JsonProperty("budgetBreakdown") val budgetBreakdowns: List<BudgetBreakdown>
        ) {

            data class BudgetBreakdown(
                @field:JsonProperty("id") @param:JsonProperty("id") val id: String,

                @JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("description") @param:JsonProperty("description") val description: String?,

                @JsonDeserialize(using = MoneyDeserializer::class)
                @JsonSerialize(using = MoneySerializer::class)
                @field:JsonProperty("amount") @param:JsonProperty("amount") val amount: Money,

                @field:JsonProperty("period") @param:JsonProperty("period") val period: Period,
                @field:JsonProperty("sourceParty") @param:JsonProperty("sourceParty") val sourceParty: SourceParty,

                @JsonInclude(JsonInclude.Include.NON_NULL)
                @get:JsonProperty("europeanUnionFunding") @param:JsonProperty("europeanUnionFunding") val europeanUnionFunding: EuropeanUnionFunding?
            ) {

                data class Period(
                    @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime,

                    @field:JsonProperty("endDate") @param:JsonProperty("endDate") val endDate: LocalDateTime
                )

                data class SourceParty(
                    @field:JsonProperty("name") @param:JsonProperty("name") val name: String,
                    @field:JsonProperty("id") @param:JsonProperty("id") val id: String
                )

                data class EuropeanUnionFunding(
                    @field:JsonProperty("projectIdentifier") @param:JsonProperty("projectIdentifier") val projectIdentifier: String,
                    @field:JsonProperty("projectName") @param:JsonProperty("projectName") val projectName: String,

                    @JsonInclude(JsonInclude.Include.NON_NULL)
                    @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String?
                )
            }
        }
    }

    data class Tender(
        @field:JsonProperty("title") @param:JsonProperty("title") val title: String,
        @field:JsonProperty("description") @param:JsonProperty("description") val description: String,
        @field:JsonProperty("classification") @param:JsonProperty("classification") val classification: Classification,
        @field:JsonProperty("mainProcurementCategory") @param:JsonProperty("mainProcurementCategory") val mainProcurementCategory: MainProcurementCategory,
        @field:JsonProperty("legalBasis") @param:JsonProperty("legalBasis") val legalBasis: LegalBasis,
        @field:JsonProperty("procurementMethodDetails") @param:JsonProperty("procurementMethodDetails") val procurementMethodDetails: String,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("procurementMethodRationale") @param:JsonProperty("procurementMethodRationale") val procurementMethodRationale: String?,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("procurementMethodAdditionalInfo") @param:JsonProperty("procurementMethodAdditionalInfo") val procurementMethodAdditionalInfo: String?,
        @field:JsonProperty("eligibilityCriteria") @param:JsonProperty("eligibilityCriteria") val eligibilityCriteria: String,
        @field:JsonProperty("tenderPeriod") @param:JsonProperty("tenderPeriod") val tenderPeriod: TenderPeriod,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("procuringEntity") @param:JsonProperty("procuringEntity") val procuringEntity: ProcuringEntity?,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @field:JsonProperty("lots") @param:JsonProperty("lots") val lots: List<Lot>?,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @field:JsonProperty("items") @param:JsonProperty("items") val items: List<Item>?,

        @field:JsonProperty("submissionMethodRationale") @param:JsonProperty("submissionMethodRationale") val submissionMethodRationale: List<String>,
        @field:JsonProperty("submissionMethodDetails") @param:JsonProperty("submissionMethodDetails") val submissionMethodDetails: String,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @field:JsonProperty("documents") @param:JsonProperty("documents") val documents: List<Document>?
    ) {

        data class Classification(
            @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: Scheme,
            @field:JsonProperty("id") @param:JsonProperty("id") val id: CPVCode,
            @field:JsonProperty("description") @param:JsonProperty("description") val description: String
        )

        data class TenderPeriod(
            @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime
        )

        data class ProcuringEntity(
            @field:JsonProperty("name") @param:JsonProperty("name") val name: String,
            @field:JsonProperty("identifier") @param:JsonProperty("identifier") val identifier: Identifier,

            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            @field:JsonProperty("additionalIdentifiers") @param:JsonProperty("additionalIdentifiers") val additionalIdentifiers: List<AdditionalIdentifier>?,
            @field:JsonProperty("address") @param:JsonProperty("address") val address: Address,
            @field:JsonProperty("contactPoint") @param:JsonProperty("contactPoint") val contactPoint: ContactPoint
        ) {

            data class Identifier(
                @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String,
                @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                @field:JsonProperty("legalName") @param:JsonProperty("legalName") val legalName: String,

                @JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String?
            )

            data class AdditionalIdentifier(
                @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String,
                @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                @field:JsonProperty("legalName") @param:JsonProperty("legalName") val legalName: String,

                @JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String?
            )

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

            data class ContactPoint(
                @field:JsonProperty("name") @param:JsonProperty("name") val name: String,
                @field:JsonProperty("email") @param:JsonProperty("email") val email: String,
                @field:JsonProperty("telephone") @param:JsonProperty("telephone") val telephone: String,

                @JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("faxNumber") @param:JsonProperty("faxNumber") val faxNumber: String?,

                @JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("url") @param:JsonProperty("url") val url: String?
            )
        }

        data class Lot(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
            @JsonInclude(JsonInclude.Include.NON_NULL)
            @field:JsonProperty("internalId") @param:JsonProperty("internalId") val internalId: String?,
            @field:JsonProperty("title") @param:JsonProperty("title") val title: String,
            @field:JsonProperty("description") @param:JsonProperty("description") val description: String,

            @JsonDeserialize(using = MoneyDeserializer::class)
            @JsonSerialize(using = MoneySerializer::class)
            @field:JsonProperty("value") @param:JsonProperty("value") val value: Money,

            @field:JsonProperty("contractPeriod") @param:JsonProperty("contractPeriod") val contractPeriod: ContractPeriod,
            @field:JsonProperty("placeOfPerformance") @param:JsonProperty("placeOfPerformance") val placeOfPerformance: PlaceOfPerformance
        ) {
            data class ContractPeriod(
                @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime,

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

        data class Item(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
            @JsonInclude(JsonInclude.Include.NON_NULL)
            @field:JsonProperty("internalId") @param:JsonProperty("internalId") val internalId: String?,
            @field:JsonProperty("classification") @param:JsonProperty("classification") val classification: Classification,

            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            @field:JsonProperty("additionalClassifications") @param:JsonProperty("additionalClassifications") val additionalClassifications: List<AdditionalClassification>?,

            @JsonDeserialize(using = QuantityDeserializer::class)
            @JsonSerialize(using = QuantitySerializer::class)
            @field:JsonProperty("quantity") @param:JsonProperty("quantity") val quantity: BigDecimal,
            @field:JsonProperty("unit") @param:JsonProperty("unit") val unit: Unit,
            @field:JsonProperty("description") @param:JsonProperty("description") val description: String,
            @field:JsonProperty("relatedLot") @param:JsonProperty("relatedLot") val relatedLot: String
        ) {
            data class Classification(
                @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: Scheme,
                @field:JsonProperty("id") @param:JsonProperty("id") val id: CPVCode,
                @field:JsonProperty("description") @param:JsonProperty("description") val description: String
            )

            data class AdditionalClassification(
                @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: Scheme,
                @field:JsonProperty("id") @param:JsonProperty("id") val id: CPVCode,
                @field:JsonProperty("description") @param:JsonProperty("description") val description: String
            )

            data class Unit(
                @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                @field:JsonProperty("name") @param:JsonProperty("name") val name: String
            )
        }

        data class Document(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
            @field:JsonProperty("documentType") @param:JsonProperty("documentType") val documentType: DocumentType,
            @field:JsonProperty("title") @param:JsonProperty("title") val title: String,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @field:JsonProperty("description") @param:JsonProperty("description") val description: String?,

            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            @field:JsonProperty("relatedLots") @param:JsonProperty("relatedLots") val relatedLots: List<String>?
        )
    }

    data class Buyer(
        @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
        @param:JsonProperty("name") @field:JsonProperty("name") val name: String,
        @param:JsonProperty("identifier") @field:JsonProperty("identifier") val identifier: Identifier,
        @param:JsonProperty("address") @field:JsonProperty("address") val address: Address,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @param:JsonProperty("additionalIdentifiers") @field:JsonProperty("additionalIdentifiers") val additionalIdentifiers: List<AdditionalIdentifier>?,

        @param:JsonProperty("contactPoint") @field:JsonProperty("contactPoint") val contactPoint: ContactPoint,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @param:JsonProperty("details") @field:JsonProperty("details") val details: Details?
    ) {
        data class Identifier(
            @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
            @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: String,
            @param:JsonProperty("legalName") @field:JsonProperty("legalName") val legalName: String,
            @param:JsonProperty("uri") @field:JsonProperty("uri") val uri: String
        )

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
                    @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: String,
                    @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
                    @param:JsonProperty("description") @field:JsonProperty("description") val description: String,
                    @param:JsonProperty("uri") @field:JsonProperty("uri") val uri: String
                )

                data class Region(
                    @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: String,
                    @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
                    @param:JsonProperty("description") @field:JsonProperty("description") val description: String,
                    @param:JsonProperty("uri") @field:JsonProperty("uri") val uri: String
                )

                data class Locality(
                    @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: String,
                    @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
                    @param:JsonProperty("description") @field:JsonProperty("description") val description: String,

                    @JsonInclude(JsonInclude.Include.NON_NULL)
                    @param:JsonProperty("uri") @field:JsonProperty("uri") val uri: String?
                )
            }
        }

        data class AdditionalIdentifier(
            @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
            @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: String,
            @param:JsonProperty("legalName") @field:JsonProperty("legalName") val legalName: String,
            @param:JsonProperty("uri") @field:JsonProperty("uri") val uri: String
        )

        data class ContactPoint(
            @param:JsonProperty("name") @field:JsonProperty("name") val name: String,
            @param:JsonProperty("email") @field:JsonProperty("email") val email: String,
            @param:JsonProperty("telephone") @field:JsonProperty("telephone") val telephone: String,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @param:JsonProperty("faxNumber") @field:JsonProperty("faxNumber") val faxNumber: String?,

            @param:JsonProperty("url") @field:JsonProperty("url") val url: String
        )

        data class Details(
            @JsonInclude(JsonInclude.Include.NON_NULL)
            @param:JsonProperty("typeOfBuyer") @field:JsonProperty("typeOfBuyer") val typeOfBuyer: TypeOfBuyer?,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @param:JsonProperty("mainGeneralActivity") @field:JsonProperty("mainGeneralActivity") val mainGeneralActivity: String?,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @param:JsonProperty("mainSectoralActivity") @field:JsonProperty("mainSectoralActivity") val mainSectoralActivity: String?
        )
    }
}
