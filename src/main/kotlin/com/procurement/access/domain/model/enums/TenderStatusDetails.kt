package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.exception.EnumException

enum class TenderStatusDetails(@JsonValue val value: String) {
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
    EMPTY("empty");

    override fun toString(): String {
        return this.value
    }

    companion object {
        private val elements: Map<String, TenderStatusDetails> = values().associateBy { it.value.toUpperCase() }

        fun fromString(value: String): TenderStatusDetails = elements[value.toUpperCase()]
            ?: throw EnumException(
                enumType = TenderStatusDetails::class.java.canonicalName,
                value = value,
                values = values().joinToString { it.value }
            )
    }
}