package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.exception.EnumException

enum class ExtendedProcurementCategory(@JsonValue val value: String) {
    GOODS("goods"),
    WORKS("works"),
    SERVICES("services"),
    CONSULTING_SERVICES("consultingServices");

    override fun toString(): String {
        return this.value
    }

    companion object {
        private val elements: Map<String, ExtendedProcurementCategory> = values().associateBy { it.value.toUpperCase() }

        fun fromString(value: String): ExtendedProcurementCategory = elements[value.toUpperCase()]
            ?: throw EnumException(
                enumType = ExtendedProcurementCategory::class.java.canonicalName,
                value = value,
                values = values().joinToString { it.value }
            )
    }
}
