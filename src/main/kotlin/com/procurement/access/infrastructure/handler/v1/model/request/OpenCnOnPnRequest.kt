package com.procurement.access.infrastructure.handler.v1.model.request

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.access.domain.model.CPVCode
import com.procurement.access.domain.model.enums.AdditionalProcurementCategories
import com.procurement.access.domain.model.enums.AwardCriteria
import com.procurement.access.domain.model.enums.AwardCriteriaDetails
import com.procurement.access.domain.model.enums.BusinessFunctionDocumentType
import com.procurement.access.domain.model.enums.BusinessFunctionType
import com.procurement.access.domain.model.enums.MainProcurementCategory
import com.procurement.access.domain.model.enums.ProcurementMethodModalities
import com.procurement.access.domain.model.enums.Scheme
import com.procurement.access.infrastructure.bind.amount.positive.AmountPositiveDeserializer
import com.procurement.access.infrastructure.bind.amount.positive.AmountPositiveSerializer
import com.procurement.access.infrastructure.bind.quantity.QuantityDeserializer
import com.procurement.access.infrastructure.bind.quantity.QuantitySerializer
import com.procurement.access.infrastructure.handler.v1.model.request.criterion.CriterionRequest
import com.procurement.access.infrastructure.handler.v1.model.request.criterion.ReferenceCriterionRequest
import com.procurement.access.infrastructure.handler.v1.model.request.document.DocumentRequest

import java.math.BigDecimal
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenCnOnPnRequest(
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @field:JsonProperty("criteria") @param:JsonProperty("criteria") val criteria: List<ReferenceCriterionRequest>?,

    @field:JsonProperty("tender") @param:JsonProperty("tender") val tender: Tender,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @field:JsonProperty("mainProcurementCategory") @param:JsonProperty("mainProcurementCategory") val mainProcurementCategory: MainProcurementCategory?,

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @field:JsonProperty("items") @param:JsonProperty("items") val items: List<ItemReferenceRequest> = emptyList()
) {

    data class Tender(
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("classification") @param:JsonProperty("classification") val classification: Classification? = null, //NULL if Items in PNEntity is not EMPTY

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("procurementMethodRationale") @param:JsonProperty("procurementMethodRationale") val procurementMethodRationale: String? = null,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("procurementMethodAdditionalInfo") @param:JsonProperty("procurementMethodAdditionalInfo") val procurementMethodAdditionalInfo: String? = null,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @field:JsonProperty("additionalProcurementCategories") @param:JsonProperty("additionalProcurementCategories") val additionalProcurementCategories: List<AdditionalProcurementCategories>?,

        @field:JsonProperty("awardCriteria") @param:JsonProperty("awardCriteria") val awardCriteria: AwardCriteria,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("awardCriteriaDetails") @param:JsonProperty("awardCriteriaDetails") val awardCriteriaDetails: AwardCriteriaDetails?,

        @field:JsonProperty("tenderPeriod") @param:JsonProperty("tenderPeriod") val tenderPeriod: TenderPeriod,
        @field:JsonProperty("enquiryPeriod") @param:JsonProperty("enquiryPeriod") val enquiryPeriod: EnquiryPeriod,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @field:JsonProperty("procurementMethodModalities") @param:JsonProperty("procurementMethodModalities") val procurementMethodModalities: Set<ProcurementMethodModalities>?,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("electronicAuctions") @param:JsonProperty("electronicAuctions") val electronicAuctions: ElectronicAuctions?,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @field:JsonProperty("criteria") @param:JsonProperty("criteria") val criteria: List<CriterionRequest>?,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @field:JsonProperty("conversions") @param:JsonProperty("conversions") val conversions: List<ConversionRequest>?,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("procuringEntity") @param:JsonProperty("procuringEntity") val procuringEntity: ProcuringEntity?,

        @field:JsonProperty("lots") @param:JsonProperty("lots") val lots: List<Lot>,
        @field:JsonProperty("items") @param:JsonProperty("items") val items: List<Item>,
        @field:JsonProperty("documents") @param:JsonProperty("documents") val documents: List<DocumentRequest>
    ) {

        data class Classification(
            @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: Scheme,
            @field:JsonProperty("description") @param:JsonProperty("description") val description: String,
            @field:JsonProperty("id") @param:JsonProperty("id") val id: CPVCode
        )

        data class TenderPeriod(
            @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime,

            @field:JsonProperty("endDate") @param:JsonProperty("endDate") val endDate: LocalDateTime
        )

        data class EnquiryPeriod(
            @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime,

            @field:JsonProperty("endDate") @param:JsonProperty("endDate") val endDate: LocalDateTime
        )

        data class ElectronicAuctions(
            @field:JsonProperty("details") @param:JsonProperty("details") val details: List<Detail>
        ) {

            data class Detail(
                @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                @field:JsonProperty("relatedLot") @param:JsonProperty("relatedLot") val relatedLot: String,
                @field:JsonProperty("electronicAuctionModalities") @param:JsonProperty("electronicAuctionModalities") val electronicAuctionModalities: List<Modalities>
            ) {

                data class Modalities(
                    @field:JsonProperty("eligibleMinimumDifference") @param:JsonProperty("eligibleMinimumDifference") val eligibleMinimumDifference: EligibleMinimumDifference
                ) {

                    data class EligibleMinimumDifference(
                        @JsonDeserialize(using = AmountPositiveDeserializer::class)
                        @JsonSerialize(using = AmountPositiveSerializer::class)
                        @field:JsonProperty("amount") @param:JsonProperty("amount") val amount: BigDecimal,

                        @field:JsonProperty("currency") @param:JsonProperty("currency") val currency: String
                    )
                }
            }
        }

        data class ProcuringEntity(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,

            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            @field:JsonProperty("persones") @param:JsonProperty("persones") val persones: List<Persone>?
        ) {
            data class Persone(
                @field:JsonProperty("title") @param:JsonProperty("title") val title: String,
                @field:JsonProperty("name") @param:JsonProperty("name") val name: String,
                @field:JsonProperty("identifier") @param:JsonProperty("identifier") val identifier: Identifier,
                @field:JsonProperty("businessFunctions") @param:JsonProperty("businessFunctions") val businessFunctions: List<BusinessFunction>
            ) {
                data class Identifier(
                    @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String,
                    @field:JsonProperty("id") @param:JsonProperty("id") val id: String,

                    @JsonInclude(JsonInclude.Include.NON_NULL)
                    @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String?
                ) {
                    override fun equals(other: Any?): Boolean = if (this === other)
                        true
                    else
                        other is Identifier
                            && this.scheme == other.scheme
                            && this.id == other.id

                    override fun hashCode(): Int {
                        var result = scheme.hashCode()
                        result = 31 * result + id.hashCode()
                        return result
                    }
                }

                data class BusinessFunction(
                    @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                    @field:JsonProperty("type") @param:JsonProperty("type") val type: BusinessFunctionType,
                    @field:JsonProperty("jobTitle") @param:JsonProperty("jobTitle") val jobTitle: String,
                    @field:JsonProperty("period") @param:JsonProperty("period") val period: Period,

                    @JsonInclude(JsonInclude.Include.NON_EMPTY)
                    @field:JsonProperty("documents") @param:JsonProperty("documents") val documents: List<Document>?
                ) {
                    data class Document(
                        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                        @field:JsonProperty("documentType") @param:JsonProperty("documentType") val documentType: BusinessFunctionDocumentType,
                        @field:JsonProperty("title") @param:JsonProperty("title") val title: String,

                        @JsonInclude(JsonInclude.Include.NON_NULL)
                        @field:JsonProperty("description") @param:JsonProperty("description") val description: String?
                    )

                    data class Period(
                        @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime
                    )
                }
            }
        }

        data class Lot(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @field:JsonProperty("internalId") @param:JsonProperty("internalId") val internalId: String?,

            @field:JsonProperty("title") @param:JsonProperty("title") val title: String,
            @field:JsonProperty("description") @param:JsonProperty("description") val description: String,
            @field:JsonProperty("value") @param:JsonProperty("value") val value: Value,
            @field:JsonProperty("contractPeriod") @param:JsonProperty("contractPeriod") val contractPeriod: ContractPeriod,
            @field:JsonProperty("placeOfPerformance") @param:JsonProperty("placeOfPerformance") val placeOfPerformance: PlaceOfPerformance,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @field:JsonProperty("hasOptions") @param:JsonProperty("hasOptions") val hasOptions: Boolean?,

            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            @field:JsonProperty("options") @param:JsonProperty("options") val options: List<Option>?,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @field:JsonProperty("hasRecurrence") @param:JsonProperty("hasRecurrence") val hasRecurrence: Boolean?,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @field:JsonProperty("recurrence") @param:JsonProperty("recurrence") val recurrence: Recurrence?,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @field:JsonProperty("hasRenewal") @param:JsonProperty("hasRenewal") val hasRenewal: Boolean?,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @field:JsonProperty("renewal") @param:JsonProperty("renewal") val renewal: Renewal?
        ) {

            data class Value(
                @JsonDeserialize(using = AmountPositiveDeserializer::class)
                @JsonSerialize(using = AmountPositiveSerializer::class)
                @field:JsonProperty("amount") @param:JsonProperty("amount") val amount: BigDecimal,

                @field:JsonProperty("currency") @param:JsonProperty("currency") val currency: String
            )

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
                            @JsonInclude(JsonInclude.Include.NON_NULL)
                            @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String?,
                            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,

                            @JsonInclude(JsonInclude.Include.NON_NULL)
                            @field:JsonProperty("description") @param:JsonProperty("description") val description: String?,

                            @JsonInclude(JsonInclude.Include.NON_NULL)
                            @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String?
                        )

                        data class Region(
                            @JsonInclude(JsonInclude.Include.NON_NULL)
                            @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String?,
                            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,

                            @JsonInclude(JsonInclude.Include.NON_NULL)
                            @field:JsonProperty("description") @param:JsonProperty("description") val description: String?,

                            @JsonInclude(JsonInclude.Include.NON_NULL)
                            @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String?
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

            data class Option(
                @JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("description") @param:JsonProperty("description") val description: String?,

                @JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("period") @param:JsonProperty("period") val period: Period?
            ) {

                @JsonIgnore
                fun isEmpty(): Boolean = description == null && period == null
            }

            data class Recurrence(
                @JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("description") @param:JsonProperty("description") val description: String?,

                @JsonInclude(JsonInclude.Include.NON_EMPTY)
                @field:JsonProperty("dates") @param:JsonProperty("dates") val dates: List<Date>?
            ) {

                @JsonIgnore
                fun isEmpty(): Boolean = description == null && (dates == null || dates.isEmpty())

                data class Date(
                    @JsonInclude(JsonInclude.Include.NON_NULL)
                    @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime?
                )
            }

            data class Renewal(
                @JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("description") @param:JsonProperty("description") val description: String?,

                @JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("minimumRenewals") @param:JsonProperty("minimumRenewals") val minimumRenewals: Int?,

                @JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("maximumRenewals") @param:JsonProperty("maximumRenewals") val maximumRenewals: Int?,

                @JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("period") @param:JsonProperty("period") val period: Period?
            ) {

                @JsonIgnore
                fun isEmpty(): Boolean =
                    description == null && minimumRenewals == null && maximumRenewals == null && period == null
            }

            data class Period(
                @JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime?,

                @JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("endDate") @param:JsonProperty("endDate") val endDate: LocalDateTime?,

                @JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("durationInDays") @param:JsonProperty("durationInDays") val durationInDays: Int?,

                @JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("maxExtentDate") @param:JsonProperty("maxExtentDate") val maxExtentDate: LocalDateTime?
            ) {

                @JsonIgnore
                fun isEmpty(): Boolean =
                    startDate == null && endDate == null && durationInDays == null && maxExtentDate == null
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
                @field:JsonProperty("description") @param:JsonProperty("description") val description: String,
                @field:JsonProperty("id") @param:JsonProperty("id") val id: String
            )

            data class AdditionalClassification(
                @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: Scheme,
                @field:JsonProperty("description") @param:JsonProperty("description") val description: String,
                @field:JsonProperty("id") @param:JsonProperty("id") val id: String
            )

            data class Unit(
                @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                @field:JsonProperty("name") @param:JsonProperty("name") val name: String
            )
        }
    }
}
