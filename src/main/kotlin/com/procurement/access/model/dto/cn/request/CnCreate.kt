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

data class CnCreate @JsonCreator constructor(

        @field:Valid @field:NotNull
        var planning: PlanningCnCreate,

        @field:Valid @field:NotNull
        var tender: TenderCnCreate
)


data class PlanningCnCreate @JsonCreator constructor(

        @field:Valid @field:NotNull
        val budget: BudgetCnCreate,

        val rationale: String?
)

data class BudgetCnCreate @JsonCreator constructor(

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

data class TenderCnCreate @JsonCreator constructor(

        @field:Valid @field:NotNull
        val classification: Classification,

        @field:NotNull
        val mainProcurementCategory: String,

        @field:NotNull
        val submissionMethodRationale: List<String>,

        @field:NotNull
        val submissionMethodDetails: String,

        @field:NotNull
        val procurementMethodDetails: String,

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
        val tenderPeriod: PeriodCnCreate,

        @field:Valid @field:NotNull
        val procuringEntity: OrganizationReference,

        @field:Valid @field:NotEmpty
        var lots: HashSet<LotCnCreate>,

        @field:Valid @field:NotEmpty
        val items: HashSet<ItemCnCreate>,

        @field:Valid
        var documents: List<Document>?
)

data class LotCnCreate @JsonCreator constructor(

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

data class ItemCnCreate @JsonCreator constructor(

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

data class PeriodCnCreate @JsonCreator constructor(

        @field:NotNull
        val endDate: LocalDateTime
)
