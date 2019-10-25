package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.exception.EnumException

enum class DocumentType(@JsonValue val value: String) {

    EVALUATION_CRITERIA("evaluationCriteria"),
    ELIGIBILITY_CRITERIA("eligibilityCriteria"),
    BILL_OF_QUANTITY("billOfQuantity"),
    ILLUSTRATION("illustration"),
    MARKET_STUDIES("marketStudies"),
    TENDER_NOTICE("tenderNotice"),
    BIDDING_DOCUMENTS("biddingDocuments"),
    PROCUREMENT_PLAN("procurementPlan"),
    TECHNICAL_SPECIFICATIONS("technicalSpecifications"),
    CONTRACT_DRAFT("contractDraft"),
    HEARING_NOTICE("hearingNotice"),
    CLARIFICATIONS("clarifications"),
    ENVIRONMENTAL_IMPACT("environmentalImpact"),
    ASSET_AND_LIABILITY_ASSESSMENT("assetAndLiabilityAssessment"),
    RISK_PROVISIONS("riskProvisions"),
    COMPLAINTS("complaints"),
    NEEDS_ASSESSMENT("needsAssessment"),
    FEASIBILITY_STUDY("feasibilityStudy"),
    PROJECT_PLAN("projectPlan"),
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
        private val elements: Map<String, DocumentType> = values().associateBy { it.value.toUpperCase() }

        fun fromString(value: String): DocumentType = elements[value.toUpperCase()]
            ?: throw EnumException(
                enumType = DocumentType::class.java.canonicalName,
                value = value,
                values = values().joinToString { it.value }
            )
    }
}
