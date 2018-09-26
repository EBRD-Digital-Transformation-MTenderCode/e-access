package com.procurement.access.model.dto.pn

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.procurement.access.databinding.QuantityDeserializer
import com.procurement.access.model.dto.databinding.BooleansDeserializer
import com.procurement.access.model.dto.ocds.*
import com.procurement.access.model.dto.ocds.Unit
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class PnCreate @JsonCreator constructor(

        var planning: PlanningPnCreate,

        var tender: TenderPnCreate
)


data class PlanningPnCreate @JsonCreator constructor(

        val budget: BudgetPnCreate,

        val rationale: String?
)

data class BudgetPnCreate @JsonCreator constructor(

        val description: String?,

        val amount: Value,

        @field:JsonDeserialize(using = BooleansDeserializer::class)
        val isEuropeanUnionFunded: Boolean?,

        val budgetBreakdown: List<BudgetBreakdown>
)

data class TenderPnCreate @JsonCreator constructor(

        val title: String,

        val description: String,

        val classification: Classification,

        val mainProcurementCategory: MainProcurementCategory,

        val procurementMethodDetails: String,

        val procurementMethodRationale: String?,

        val procurementMethodAdditionalInfo: String?,

        val submissionMethodRationale: List<String>,

        val submissionMethodDetails: String,

        val eligibilityCriteria: String,

        val legalBasis: LegalBasis,

        val tenderPeriod: TenderPeriodPnCreate,

        val procuringEntity: OrganizationReference,

        val lots: List<LotPnCreate>?,

        val items: List<ItemPnCreate>?,

        val documents: List<Document>?
)

data class LotPnCreate @JsonCreator constructor(

        var id: String,

        val title: String?,

        val description: String?,

        val value: Value,

        val contractPeriod: ContractPeriod,

        val placeOfPerformance: PlaceOfPerformance?
)

data class ItemPnCreate @JsonCreator constructor(

        var id: String,

        val description: String?,

        val classification: Classification,

        val additionalClassifications: HashSet<Classification>?,

        @field:JsonDeserialize(using = QuantityDeserializer::class)
        val quantity: BigDecimal,

        val unit: Unit,

        var relatedLot: String
)

data class TenderPeriodPnCreate @JsonCreator constructor(

        val startDate: LocalDateTime
)
