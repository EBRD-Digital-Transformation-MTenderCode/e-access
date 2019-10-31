package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.exception.EnumException

enum class LotStatusDetails(@JsonValue val value: String) {
    UNSUCCESSFUL("unsuccessful"),
    AWARDED("awarded"),
    CANCELLED("cancelled"),
    EMPTY("empty");

    override fun toString(): String {
        return this.value
    }

    companion object {
        private val elements: Map<String, LotStatusDetails> = values().associateBy { it.value.toUpperCase() }

        fun fromString(value: String): LotStatusDetails = elements[value.toUpperCase()]
            ?: throw EnumException(
                enumType = LotStatusDetails::class.java.canonicalName,
                value = value,
                values = values().joinToString { it.value }
            )
    }
}