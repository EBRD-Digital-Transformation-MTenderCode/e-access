package com.procurement.access.infrastructure.entity


import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.AwardCriteria
import com.procurement.access.domain.model.enums.AwardCriteriaDetails
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.domain.model.enums.MainProcurementCategory
import com.procurement.access.domain.model.enums.ProcurementMethod
import com.procurement.access.domain.model.enums.RelatedProcessType
import com.procurement.access.domain.model.enums.Scheme
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.domain.model.token.Token
import java.math.BigDecimal
import java.time.LocalDateTime

data class RfqEntity(
    @param:JsonProperty("ocid") @field:JsonProperty("ocid") val ocid: Ocid.SingleStage,
    @param:JsonProperty("token") @field:JsonProperty("token") val token: Token,
    @param:JsonProperty("tender") @field:JsonProperty("tender") val tender: Tender,
    @param:JsonProperty("relatedProcesses") @field:JsonProperty("relatedProcesses") val relatedProcesses: List<RelatedProcess>
) {
    data class Tender(
        @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
        @param:JsonProperty("title") @field:JsonProperty("title") val title: String,
        @param:JsonProperty("description") @field:JsonProperty("description") val description: String,
        @param:JsonProperty("status") @field:JsonProperty("status") val status: TenderStatus,
        @param:JsonProperty("statusDetails") @field:JsonProperty("statusDetails") val statusDetails: TenderStatusDetails,
        @param:JsonProperty("value") @field:JsonProperty("value") val value: Value,
        @param:JsonProperty("date") @field:JsonProperty("date") val date: LocalDateTime,
        @param:JsonProperty("lots") @field:JsonProperty("lots") val lots: List<Lot>,
        @param:JsonProperty("items") @field:JsonProperty("items") val items: List<Item>,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @param:JsonProperty("procurementMethodModalities") @field:JsonProperty("procurementMethodModalities") val procurementMethodModalities: List<String>?,

        @param:JsonProperty("awardCriteria") @field:JsonProperty("awardCriteria") val awardCriteria: AwardCriteria,
        @param:JsonProperty("awardCriteriaDetails") @field:JsonProperty("awardCriteriaDetails") val awardCriteriaDetails: AwardCriteriaDetails,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @param:JsonProperty("classification") @field:JsonProperty("classification") val classification: Classification?,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @param:JsonProperty("procurementMethod") @field:JsonProperty("procurementMethod") val procurementMethod: ProcurementMethod?,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @param:JsonProperty("procurementMethodDetails") @field:JsonProperty("procurementMethodDetails") val procurementMethodDetails: String?,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @param:JsonProperty("mainProcurementCategory") @field:JsonProperty("mainProcurementCategory") val mainProcurementCategory: MainProcurementCategory?,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @param:JsonProperty("additionalProcurementCategories") @field:JsonProperty("additionalProcurementCategories") val additionalProcurementCategories: List<MainProcurementCategory> = emptyList()

    ) {

        data class Classification(
            @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
            @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: Scheme,
            @param:JsonProperty("description") @field:JsonProperty("description") val description: String
        )

        data class Value(
            @field:JsonProperty("currency") @param:JsonProperty("currency") val currency: String
        )

        data class Lot(
            @param:JsonProperty("id") @field:JsonProperty("id") val id: LotId,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @param:JsonProperty("internalId") @field:JsonProperty("internalId") val internalId: String?,

            @param:JsonProperty("title") @field:JsonProperty("title") val title: String,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @param:JsonProperty("description") @field:JsonProperty("description") val description: String?,

            @param:JsonProperty("value") @field:JsonProperty("value") val value: Value,
            @param:JsonProperty("contractPeriod") @field:JsonProperty("contractPeriod") val contractPeriod: ContractPeriod,
            @param:JsonProperty("placeOfPerformance") @field:JsonProperty("placeOfPerformance") val placeOfPerformance: PlaceOfPerformance,
            @param:JsonProperty("status") @field:JsonProperty("status") val status: LotStatus,
            @param:JsonProperty("statusDetails") @field:JsonProperty("statusDetails") val statusDetails: LotStatusDetails
        ) {
            data class Value(
                @param:JsonProperty("currency") @field:JsonProperty("currency") val currency: String
            )

            data class ContractPeriod(
                @param:JsonProperty("startDate") @field:JsonProperty("startDate") val startDate: LocalDateTime,
                @param:JsonProperty("endDate") @field:JsonProperty("endDate") val endDate: LocalDateTime
            )

            data class PlaceOfPerformance(
                @JsonInclude(JsonInclude.Include.NON_NULL)
                @param:JsonProperty("description") @field:JsonProperty("description") val description: String?,

                @param:JsonProperty("address") @field:JsonProperty("address") val address: Address
            ) {
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
                            @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
                            @param:JsonProperty("description") @field:JsonProperty("description") val description: String,
                            @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: String,
                            @param:JsonProperty("uri") @field:JsonProperty("uri") val uri: String
                        )

                        data class Region(
                            @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
                            @param:JsonProperty("description") @field:JsonProperty("description") val description: String,
                            @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: String,
                            @param:JsonProperty("uri") @field:JsonProperty("uri") val uri: String
                        )

                        data class Locality(
                            @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
                            @param:JsonProperty("description") @field:JsonProperty("description") val description: String,
                            @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: String,

                            @JsonInclude(JsonInclude.Include.NON_NULL)
                            @param:JsonProperty("uri") @field:JsonProperty("uri") val uri: String?
                        )
                    }
                }
            }
        }

        data class Item(
            @param:JsonProperty("id") @field:JsonProperty("id") val id: String,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @param:JsonProperty("internalId") @field:JsonProperty("internalId") val internalId: String?,

            @param:JsonProperty("description") @field:JsonProperty("description") val description: String,
            @param:JsonProperty("classification") @field:JsonProperty("classification") val classification: Classification,
            @param:JsonProperty("quantity") @field:JsonProperty("quantity") val quantity: BigDecimal,
            @param:JsonProperty("unit") @field:JsonProperty("unit") val unit: Unit,
            @param:JsonProperty("relatedLot") @field:JsonProperty("relatedLot") val relatedLot: LotId
        ) {
            data class Classification(
                @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
                @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: String,
                @param:JsonProperty("description") @field:JsonProperty("description") val description: String
            )

            data class Unit(
                @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
                @param:JsonProperty("name") @field:JsonProperty("name") val name: String
            )
        }
    }

    data class RelatedProcess(
        @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
        @param:JsonProperty("relationship") @field:JsonProperty("relationship") val relationship: List<RelatedProcessType>,
        @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: String,
        @param:JsonProperty("identifier") @field:JsonProperty("identifier") val identifier: String,
        @param:JsonProperty("uri") @field:JsonProperty("uri") val uri: String
    )
}