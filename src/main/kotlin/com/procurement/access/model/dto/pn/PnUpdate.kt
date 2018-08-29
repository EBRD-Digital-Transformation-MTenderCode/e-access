package com.procurement.access.model.dto.pn

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.procurement.access.databinding.QuantityDeserializer
import com.procurement.access.model.dto.ocds.*
import com.procurement.access.model.dto.ocds.Unit
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotNull

data class PnUpdate @JsonCreator constructor(

        @field:Valid @field:NotNull
        val planning: PlanningPnUpdate,

        @field:Valid @field:NotNull
        val tender: TenderPnUpdate
)


data class PlanningPnUpdate @JsonCreator constructor(

        @field:Valid @field:NotNull
        val budget: BudgetPnUpdate,

        val rationale: String?
)

data class BudgetPnUpdate @JsonCreator constructor(

        val description: String?

)

data class TenderPnUpdate @JsonCreator constructor(

        @field:NotNull
        val title: String,

        @field:NotNull
        val description: String,

        val procurementMethodRationale: String?,

        val procurementMethodAdditionalInfo: String?,

        val classification: Classification?,

        @field:Valid @field:NotNull
        val tenderPeriod: PeriodPnUpdate,

        @field:Valid
        val lots: List<LotPnUpdate>?,

        @field:Valid
        val items: List<ItemPnUpdate>?,

        @field:Valid
        val documents: List<Document>?
)

data class LotPnUpdate @JsonCreator constructor(

        @field:NotNull
        var id: String,

        @field:NotNull
        val title: String,

        @field:NotNull
        val description: String,

        @field:Valid @field:NotNull
        val value: Value,

        @field:Valid @field:NotNull
        val contractPeriod: ContractPeriod,

        @field:Valid @field:NotNull
        val placeOfPerformance: PlaceOfPerformance
)

data class ItemPnUpdate @JsonCreator constructor(

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

data class PeriodPnUpdate @JsonCreator constructor(

        @field:NotNull
        val startDate: LocalDateTime
)
