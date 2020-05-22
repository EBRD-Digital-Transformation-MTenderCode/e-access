package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.EnumElementProvider

enum class TenderStatusDetails(@JsonValue override val key: String) : EnumElementProvider.Key {
    PLANNING("planning"),
    PLANNED("planned"),
    CLARIFICATION("clarification"),
    NEGOTIATION("negotiation"),
    TENDERING("tendering"),
    CANCELLATION("cancellation"),
    SUSPENDED("suspended"),
    AWARDING("awarding"),
    AUCTION("auction"),
    AWARDED_STANDSTILL("awardedStandStill"),
    AWARDED_SUSPENDED("awardedSuspended"),
    AWARDED_CONTRACT_PREPARATION("awardedContractPreparation"),
    COMPLETE("complete"),
    EMPTY("empty"),
    SUBMISSION("submission"),
    QUALIFICATION("qualification"),
    LACK_OF_SUBMISSIONS("lackOfSubmissions");


    override fun toString(): String = key

    companion object : EnumElementProvider<TenderStatusDetails>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = TenderStatusDetails.orThrow(name)
    }
}