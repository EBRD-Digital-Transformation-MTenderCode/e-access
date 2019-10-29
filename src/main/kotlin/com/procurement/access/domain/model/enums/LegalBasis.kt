package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.exception.EnumException

enum class LegalBasis(@JsonValue val value: String) {
    DIRECTIVE_2014_23_EU("DIRECTIVE_2014_23_EU"),
    DIRECTIVE_2014_24_EU("DIRECTIVE_2014_24_EU"),
    DIRECTIVE_2014_25_EU("DIRECTIVE_2014_25_EU"),
    DIRECTIVE_2009_81_EC("DIRECTIVE_2009_81_EC"),
    REGULATION_966_2012("REGULATION_966_2012"),
    NATIONAL_PROCUREMENT_LAW("NATIONAL_PROCUREMENT_LAW"),
    NULL("NULL");

    override fun toString(): String {
        return this.value
    }

    companion object {
        private val elements: Map<String, LegalBasis> = values().associateBy { it.value.toUpperCase() }

        fun fromString(value: String): LegalBasis = elements[value.toUpperCase()]
            ?: throw EnumException(
                enumType = LegalBasis::class.java.canonicalName,
                value = value,
                values = values().joinToString { it.value }
            )
    }
}
