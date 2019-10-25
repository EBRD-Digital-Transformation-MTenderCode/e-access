package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.exception.EnumException

enum class AwardCriteria(@JsonValue val value: String) {
    PRICE_ONLY("priceOnly"),
    COST_ONLY("costOnly"),
    QUALITY_ONLY("qualityOnly"),
    RATED_CRITERIA("ratedCriteria");

    override fun toString(): String {
        return this.value
    }

    companion object {
        private val elements: Map<String, AwardCriteria> = values().associateBy { it.value.toUpperCase() }

        fun fromString(value: String): AwardCriteria =
            elements[value.toUpperCase()] ?: throw EnumException(
                enumType = AwardCriteria::class.java.canonicalName,
                value = value,
                values = values().joinToString { it.value })
    }
}