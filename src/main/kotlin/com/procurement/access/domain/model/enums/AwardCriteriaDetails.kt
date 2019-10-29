package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.exception.EnumException

enum class AwardCriteriaDetails(@JsonValue val value: String) {
    MANUAL("manual"),
    AUTOMATED("automated");

    override fun toString(): String {
        return this.value
    }

    companion object {
        private val elements: Map<String, AwardCriteriaDetails> = values().associateBy { it.value.toUpperCase() }

        fun fromString(value: String): AwardCriteriaDetails = elements[value.toUpperCase()] ?: throw EnumException(
            enumType = AwardCriteriaDetails::class.java.canonicalName,
            value = value,
            values = values().joinToString { it.value })
    }
}
