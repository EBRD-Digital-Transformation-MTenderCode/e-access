package com.procurement.access.model.dto.cn.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.procurement.access.databinding.QuantityDeserializer
import com.procurement.access.model.dto.databinding.BooleansDeserializer
import com.procurement.access.model.dto.ocds.*
import com.procurement.access.model.dto.ocds.Unit
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class CreateCn @JsonCreator constructor(

        @field:Valid @field:NotNull
        var planning: PlanningCreateCn,

        @field:Valid @field:NotNull
        var tender: TenderCreateCn
)


data class PlanningCreateCn @JsonCreator constructor(

        @field:Valid @field:NotNull
        val budget: BudgetCreateCn,

        val rationale: String?
)

data class BudgetCreateCn @JsonCreator constructor(

        val description: String?,

        @field:Valid @field:NotNull
        val amount: Value,

        @field:NotNull
        @field:JsonDeserialize(using = BooleansDeserializer::class)
        @get:JsonProperty("isEuropeanUnionFunded")
        val isEuropeanUnionFunded: Boolean,

        @field:Valid @field:NotEmpty
        val budgetBreakdown: List<BudgetBreakdown>
)

data class TenderCreateCn @JsonCreator constructor(

        @field:Valid @field:NotNull
        val classification: Classification,

        @field:NotNull
        val mainProcurementCategory: String,

        @field:NotEmpty
        val submissionMethod: List<String>?,

        val submissionMethodRationale: List<String>?,

        val submissionMethodDetails: String?,

        @field:NotNull
        val eligibilityCriteria: String,

        @field:NotNull
        val title: String,

        @field:NotNull
        val description: String,

        @field:NotNull
        val legalBasis: LegalBasis,

        val procurementMethodRationale: String?,

        val procurementMethodAdditionalInfo: String?,

        @field:Valid @field:NotNull
        val tenderPeriod: PeriodCreateCn,

        @field:Valid @field:NotNull
        val procuringEntity: OrganizationReference,

        @field:Valid @field:NotEmpty
        var lots: HashSet<LotCreateCn>,

        @field:Valid @field:NotEmpty
        val items: HashSet<ItemCreateCn>,

        @field:Valid
        var documents: List<Document>?
)

data class LotCreateCn @JsonCreator constructor(

        @field:NotNull
        var id: String,

        @field:NotNull
        val title: String,

        @field:NotNull
        val description: String,

        @field:Valid @field:NotNull
        val value: Value,

        @field:Valid @field:NotNull
        val contractPeriod: Period,

        @field:Valid @field:NotNull
        val placeOfPerformance: PlaceOfPerformance
)

data class ItemCreateCn @JsonCreator constructor(

        @field:NotNull
        var id: String,

        val description: String?,

        @field:Valid @field:NotNull
        val classification: Classification,

        @field:Valid
        val additionalClassifications: HashSet<Classification>?,

        @field:NotNull
        @field:JsonDeserialize(using = QuantityDeserializer::class)
        val quantity: BigDecimal,

        @field:Valid @field:NotNull
        val unit: Unit,

        @field:NotNull
        var relatedLot: String
)

data class PeriodCreateCn @JsonCreator constructor(

        @field:NotNull
        val endDate: LocalDateTime
)
