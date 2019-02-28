package com.procurement.access.model.dto.pn

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.procurement.access.databinding.QuantityDeserializer
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.model.dto.ocds.Classification
import com.procurement.access.model.dto.ocds.ContractPeriod
import com.procurement.access.model.dto.ocds.Document
import com.procurement.access.model.dto.ocds.PlaceOfPerformance
import com.procurement.access.model.dto.ocds.Unit
import com.procurement.access.model.dto.ocds.Value
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

        val placeOfPerformance: PlaceOfPerformance?
)

data class ItemPnUpdate @JsonCreator constructor(

        var id: String,

        val description: String,

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

fun PnUpdate.validate(): PnUpdate {
    this.tender.items?.let {
        if (it.isEmpty()) throw ErrorException(ErrorType.EMPTY_ITEMS)
        val lots = this.tender.lots ?: throw ErrorException(ErrorType.EMPTY_LOTS)
        if (lots.isEmpty()) throw ErrorException(ErrorType.EMPTY_LOTS)
    }
    this.tender.lots?.let {
        if (it.isEmpty()) throw ErrorException(ErrorType.EMPTY_LOTS)
        val items = this.tender.items ?: throw ErrorException(ErrorType.EMPTY_ITEMS)
        if (items.isEmpty()) throw ErrorException(ErrorType.EMPTY_ITEMS)
    }
    if (this.tender.documents?.isEmpty() == true) throw ErrorException(ErrorType.EMPTY_DOCS)
    return this
}