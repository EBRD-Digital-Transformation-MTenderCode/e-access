package com.procurement.access.domain.model.mainProcurementCategory

import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.exception.EnumException

enum class MainProcurementCategory(@JsonValue val value: String) {
    GOODS("goods"),
    WORKS("works"),
    SERVICES("services");

    override fun toString(): String {
        return this.value
    }

    companion object {
        private val elements: Map<String, MainProcurementCategory> = values().associateBy { it.value.toUpperCase() }

        fun fromString(value: String): MainProcurementCategory =
            elements[value.toUpperCase()] ?: throw EnumException(
                enumType = MainProcurementCategory::class.java.canonicalName,
                value = value,
                values = values().joinToString { it.value })
    }
}
