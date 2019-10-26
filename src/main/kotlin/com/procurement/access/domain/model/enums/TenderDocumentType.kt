package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.exception.EnumException

enum class TenderDocumentType(@JsonValue val value: String) {

    EVALUATION_CRITERIA("evaluationCriteria"),
    ELIGIBILITY_CRITERIA("eligibilityCriteria"),
    BILL_OF_QUANTITY("billOfQuantity"),
    ILLUSTRATION("illustration"),
    TENDER_NOTICE("tenderNotice"),
    BIDDING_DOCUMENTS("biddingDocuments"),
    PROCUREMENT_PLAN("procurementPlan"),
    TECHNICAL_SPECIFICATIONS("technicalSpecifications"),
    CONTRACT_DRAFT("contractDraft"),
    CLARIFICATIONS("clarifications"),
    RISK_PROVISIONS("riskProvisions"),
    COMPLAINTS("complaints"),
    CONFLICT_OF_INTEREST("conflictOfInterest"),
    CANCELLATION_DETAILS("cancellationDetails"),
    SHORTLISTED_FIRMS("shortlistedFirms"),
    EVALUATION_REPORTS("evaluationReports"),
    CONTRACT_ARRANGEMENTS("contractArrangements"),
    CONTRACT_GUARANTEES("contractGuarantees");

    override fun toString(): String {
        return this.value
    }

    companion object {
        private val elements: Map<String, TenderDocumentType> = values().associateBy { it.value.toUpperCase() }

        fun fromString(value: String): TenderDocumentType = elements[value.toUpperCase()]
            ?: throw EnumException(
                enumType = TenderDocumentType::class.java.canonicalName,
                value = value,
                values = values().joinToString { it.value }
            )
    }
}
