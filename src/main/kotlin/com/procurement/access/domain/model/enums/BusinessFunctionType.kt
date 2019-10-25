package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.exception.EnumException

enum class BusinessFunctionType(@JsonValue val value: String) {
    AUTHORITY("authority");

    override fun toString(): String {
        return this.value
    }

    companion object {
        private val elements: Map<String, BusinessFunctionType> = values().associateBy { it.value.toUpperCase() }

        fun fromString(value: String): BusinessFunctionType = elements[value.toUpperCase()]
            ?: throw EnumException(
                enumType = BusinessFunctionType::class.java.canonicalName,
                value = value,
                values = values().joinToString { it.value }
            )
    }
}
