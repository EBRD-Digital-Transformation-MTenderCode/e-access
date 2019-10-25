package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.exception.EnumException

enum class ConversionsRelatesTo(@JsonValue val value: String) {
    REQUIREMENT("requirement"),
    OBSERVATION("observation"),
    OPTION("option");

    override fun toString(): String {
        return this.value
    }

    companion object {
        private val elements: Map<String, ConversionsRelatesTo> = values().associateBy { it.value.toUpperCase() }

        fun fromString(value: String): ConversionsRelatesTo = elements[value.toUpperCase()]
            ?: throw EnumException(
                enumType = ConversionsRelatesTo::class.java.canonicalName,
                value = value,
                values = values().joinToString { it.value }
            )
    }
}
