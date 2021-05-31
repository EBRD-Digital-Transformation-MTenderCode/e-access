package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.EnumElementProvider

enum class OperationType(@JsonValue override val key: String) : EnumElementProvider.Key {
    AMEND_FE("amendFE"),
    APPLY_CONFIRMATIONS("applyConfirmations"),
    APPLY_QUALIFICATION_PROTOCOL("applyQualificationProtocol"),
    AWARD_CONSIDERATION("awardConsideration"),
    COMPLETE_QUALIFICATION("completeQualification"),
    CREATE_AWARD("createAward"),
    CREATE_CN("createCN"),
    CREATE_CN_ON_PIN("createCNonPIN"),
    CREATE_CN_ON_PN("createCNonPN"),
    CREATE_CONFIRMATION_RESPONSE_BY_BUYER("createConfirmationResponseByBuyer"),
    CREATE_CONFIRMATION_RESPONSE_BY_INVITED_CANDIDATE("createConfirmationResponseByInvitedCandidate"),
    CREATE_FE("createFE"),
    CREATE_NEGOTIATION_CN_ON_PN("createNegotiationCnOnPn"),
    CREATE_PCR("createPcr"),
    CREATE_PIN("createPIN"),
    CREATE_PIN_ON_PN("createPINonPN"),
    CREATE_PN("createPN"),
    CREATE_RFQ("createRfq"),
    CREATE_SUBMISSION("createSubmission"),
    DECLARE_NON_CONFLICT_OF_INTEREST("declareNonConflictOfInterest"),
    DIVIDE_LOT("divideLot"),
    ISSUING_FRAMEWORK_CONTRACT("issuingFrameworkContract"),
    NEXT_STEP_AFTER_BUYERS_CONFIRMATION("nextStepAfterBuyersConfirmation"),
    NEXT_STEP_AFTER_INVITED_CANDIDATES_CONFIRMATION("nextStepAfterInvitedCandidatesConfirmation"),
    OUTSOURCING_PN("outsourcingPN"),
    QUALIFICATION("qualification"),
    QUALIFICATION_CONSIDERATION("qualificationConsideration"),
    QUALIFICATION_DECLARE_NON_CONFLICT_OF_INTEREST("qualificationDeclareNonConflictOfInterest"),
    QUALIFICATION_PROTOCOL("qualificationProtocol"),
    RELATION_AP("relationAP"),
    START_SECONDSTAGE("startSecondStage"),
    SUBMISSION_PERIOD_END("submissionPeriodEnd"),
    SUBMIT_BID("submitBid"),
    TENDER_PERIOD_END("tenderPeriodEnd"),
    UPDATE_AP("updateAP"),
    UPDATE_AWARD("updateAward"),
    UPDATE_CN("updateCN"),
    UPDATE_PN("updatePN"),
    WITHDRAW_BID("withdrawBid"),
    WITHDRAW_QUALIFICATION_PROTOCOL("withdrawQualificationProtocol")
    ;

    override fun toString(): String = key

    companion object : EnumElementProvider<OperationType>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = OperationType.orThrow(name)
    }
}
