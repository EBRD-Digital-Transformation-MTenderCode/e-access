package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.exception.EnumException

enum class Scheme(@JsonValue val value: String) {
    CPV("CPV"),
    CPVS("CPVS"),
    GSIN("GSIN"),
    UNSPSC("UNSPSC"),
    CPC("CPC"),
    OKDP("OKDP"),
    OKPD("OKPD");

    override fun toString(): String {
        return this.value
    }

    companion object {
        private val elements: Map<String, Scheme> = values().associateBy { it.value.toUpperCase() }

        fun fromString(value: String): Scheme = elements[value.toUpperCase()]
            ?: throw EnumException(
                enumType = Scheme::class.java.canonicalName,
                value = value,
                values = values().joinToString { it.value }
            )
    }
}
