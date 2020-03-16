package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.EnumElementProvider

enum class TenderDocumentType(@JsonValue override val key: String) : EnumElementProvider.Key {

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

    override fun toString(): String = key

    companion object : EnumElementProvider<TenderDocumentType>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = TenderDocumentType.orThrow(name)
    }
}
