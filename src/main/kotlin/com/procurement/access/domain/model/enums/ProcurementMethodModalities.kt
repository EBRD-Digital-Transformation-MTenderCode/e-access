package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.exception.EnumException

enum class ProcurementMethodModalities(@JsonValue val value: String) {
    ELECTRONIC_AUCTION("electronicAuction");

    override fun toString(): String {
        return this.value
    }

    companion object {
        private val elements: Map<String, ProcurementMethodModalities> = values().associateBy { it.value.toUpperCase() }

        fun fromString(value: String): ProcurementMethodModalities = elements[value.toUpperCase()]
            ?: throw EnumException(
                enumType = ProcurementMethodModalities::class.java.canonicalName,
                value = value,
                values = values().joinToString { it.value }
            )
    }
}
