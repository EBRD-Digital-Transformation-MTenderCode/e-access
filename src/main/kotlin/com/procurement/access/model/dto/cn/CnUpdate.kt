package com.procurement.access.model.dto.cn

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.model.dto.databinding.QuantityDeserializer
import com.procurement.access.model.dto.ocds.AwardCriteria
import com.procurement.access.model.dto.ocds.Classification
import com.procurement.access.model.dto.ocds.ContractPeriod
import com.procurement.access.model.dto.ocds.Document
import com.procurement.access.model.dto.ocds.ElectronicAuctions
import com.procurement.access.model.dto.ocds.Period
import com.procurement.access.model.dto.ocds.PlaceOfPerformance
import com.procurement.access.model.dto.ocds.ProcurementMethodModalities
import com.procurement.access.model.dto.ocds.Unit
import com.procurement.access.model.dto.ocds.Value
import com.procurement.access.model.dto.ocds.validate
import java.math.BigDecimal
import java.util.*

data class CnUpdate @JsonCreator constructor(

        val planning: PlanningCnUpdate,

        val tender: TenderCnUpdate
)

data class PlanningCnUpdate @JsonCreator constructor(

        val budget: BudgetCnUpdate,

        val rationale: String?
)

data class BudgetCnUpdate @JsonCreator constructor(

        val description: String?
)

data class TenderCnUpdate @JsonCreator constructor(

        val title: String,

        val description: String,

        val procurementMethodRationale: String?,

        val procurementMethodAdditionalInfo: String?,

        val classification: Classification?,

        val tenderPeriod: Period,

        val enquiryPeriod: Period,

        val lots: List<LotCnUpdate>,

        val items: List<ItemCnUpdate>,

        val documents: List<Document>,

        val procurementMethodModalities: Set<ProcurementMethodModalities>?,

        val electronicAuctions: ElectronicAuctions?,

        val awardCriteria: AwardCriteria?
)

data class LotCnUpdate @JsonCreator constructor(

        var id: String,

        val title: String,

        val description: String,

        val value: Value,

        val contractPeriod: ContractPeriod,

        val placeOfPerformance: PlaceOfPerformance
)

data class ItemCnUpdate @JsonCreator constructor(

        var id: String,

        val description: String,

        val classification: Classification,

        val additionalClassifications: HashSet<Classification>?,

        @field:JsonDeserialize(using = QuantityDeserializer::class)
        val quantity: BigDecimal,

        val unit: Unit,

        var relatedLot: String
)

fun CnUpdate.validate(): CnUpdate {
    if (this.tender.items.isEmpty()) throw ErrorException(ErrorType.EMPTY_ITEMS)
    if (this.tender.lots.isEmpty()) throw ErrorException(ErrorType.EMPTY_LOTS)
    if (this.tender.documents.isEmpty()) throw ErrorException(ErrorType.EMPTY_DOCS)
    this.tender.procurementMethodModalities?.let {
        this.tender.electronicAuctions ?: throw ErrorException(ErrorType.INVALID_AUCTION_IS_EMPTY)
        if (this.tender.procurementMethodModalities.isEmpty()) throw ErrorException(ErrorType.INVALID_PMM)
    }
    this.tender.electronicAuctions?.let {
        it.validate()
        this.tender.procurementMethodModalities ?: throw ErrorException(ErrorType.INVALID_PMM)
        if (this.tender.procurementMethodModalities.isEmpty()) throw ErrorException(ErrorType.INVALID_PMM)
    }
    return this
}