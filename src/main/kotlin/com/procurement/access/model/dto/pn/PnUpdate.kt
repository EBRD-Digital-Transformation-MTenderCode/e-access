package com.procurement.access.model.dto.pn

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.procurement.access.databinding.QuantityDeserializer
import com.procurement.access.model.dto.ocds.*
import com.procurement.access.model.dto.ocds.Unit
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class PnUpdate @JsonCreator constructor(

        val planning: PlanningPnUpdate,

        val tender: TenderPnUpdate
)


data class PlanningPnUpdate @JsonCreator constructor(

        val budget: BudgetPnUpdate,

        val rationale: String?
)

data class BudgetPnUpdate @JsonCreator constructor(

        val description: String?
)

data class TenderPnUpdate @JsonCreator constructor(

        val title: String,

        val description: String,

        val procurementMethodRationale: String?,

        val procurementMethodAdditionalInfo: String?,

        val classification: Classification?,

        val tenderPeriod: PeriodPnUpdate,

        val lots: List<LotPnUpdate>?,

        val items: List<ItemPnUpdate>?,

        val documents: List<Document>?
)

data class LotPnUpdate @JsonCreator constructor(

        var id: String,

        val title: String,

        val description: String,

        val value: Value,

        val contractPeriod: ContractPeriod,

        val placeOfPerformance: PlaceOfPerformance
)

data class ItemPnUpdate @JsonCreator constructor(

        var id: String,

        val description: String?,

        val classification: Classification,

        val additionalClassifications: HashSet<Classification>?,

        @field:JsonDeserialize(using = QuantityDeserializer::class)
        val quantity: BigDecimal,

        val unit: Unit,

        var relatedLot: String
)

data class PeriodPnUpdate @JsonCreator constructor(

        val startDate: LocalDateTime
)
