package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.util.Result
import com.procurement.access.exception.EnumException

enum class LotStatus(@JsonValue val value: String) {
    PLANNING("planning"),
    PLANNED("planned"),
    ACTIVE("active"),
    CANCELLED("cancelled"),
    UNSUCCESSFUL("unsuccessful"),
    COMPLETE("complete");

    override fun toString(): String {
        return this.value
    }

    companion object {
        private val elements: Map<String, LotStatus> = values().associateBy { it.value.toUpperCase() }

        fun fromString(value: String): LotStatus = elements[value.toUpperCase()]
            ?: throw EnumException(
                enumType = LotStatus::class.java.canonicalName,
                value = value,
                values = values().joinToString { it.value }
            )

        fun tryCreate(value: String): Result<LotStatus, String> = elements[value.toUpperCase()]
            ?.let {
                Result.success(it)
            }
            ?: Result.failure(
                "Unknown value for enumType ${LotStatus::class.java.canonicalName}: " +
                    "$value, Allowed values are ${values().joinToString { it.value }}"
            )
    }
}
