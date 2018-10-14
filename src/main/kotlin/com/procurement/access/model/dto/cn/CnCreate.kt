package com.procurement.access.model.dto.cn

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.procurement.access.databinding.QuantityDeserializer
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.model.dto.databinding.BooleansDeserializer
import com.procurement.access.model.dto.ocds.*
import com.procurement.access.model.dto.ocds.Unit
import java.math.BigDecimal
import java.util.*

data class CnCreate @JsonCreator constructor(

        val planning: PlanningCnCreate,

        val tender: TenderCnCreate
)

data class PlanningCnCreate @JsonCreator constructor(

        val budget: BudgetCnCreate,

        val rationale: String?
)

data class BudgetCnCreate @JsonCreator constructor(

        val description: String?,

        val amount: Value,

        @field:JsonDeserialize(using = BooleansDeserializer::class)
        val isEuropeanUnionFunded: Boolean,

        val budgetBreakdown: List<BudgetBreakdown>
)

data class TenderCnCreate @JsonCreator constructor(

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

        val tenderPeriod: Period,

        val enquiryPeriod: Period,

        val procuringEntity: OrganizationReference,

        val lots: List<LotCnCreate>,

        val items: List<ItemCnCreate>,

        val documents: List<Document>,

        val procurementMethodModalities: String?,

        val electronicAuctions: ElectronicAuctions?
)

data class LotCnCreate @JsonCreator constructor(

        var id: String,

        val title: String,

        val description: String,

        val value: Value,

        val contractPeriod: ContractPeriod,

        val placeOfPerformance: PlaceOfPerformance
)

data class ItemCnCreate @JsonCreator constructor(

        var id: String,

        val description: String?,

        val classification: Classification,

        val additionalClassifications: HashSet<Classification>?,

        @field:JsonDeserialize(using = QuantityDeserializer::class)
        val quantity: BigDecimal,

        val unit: Unit,

        var relatedLot: String
)

fun CnCreate.validate(): CnCreate {
    if (this.planning.budget.budgetBreakdown.isEmpty()) throw ErrorException(ErrorType.EMPTY_BREAKDOWN)
    if (this.tender.items.isEmpty()) throw ErrorException(ErrorType.EMPTY_ITEMS)
    if (this.tender.lots.isEmpty()) throw ErrorException(ErrorType.EMPTY_LOTS)
    if (this.tender.documents.isEmpty()) throw ErrorException(ErrorType.EMPTY_DOCS)
    this.tender.procurementMethodModalities?.let {
        this.tender.electronicAuctions ?: throw ErrorException(ErrorType.INVALID_AUCTION)
    }
    this.tender.electronicAuctions?.let {
        this.tender.procurementMethodModalities ?: throw ErrorException(ErrorType.INVALID_PMM)
    }
    return this
}