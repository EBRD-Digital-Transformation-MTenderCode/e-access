package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.exception.EnumException

enum class CriteriaRelatesToEnum(@JsonValue val value: String) {
    TENDERER("tenderer"),
    ITEM("item"),
    LOT("lot");

    override fun toString(): String {
        return this.value
    }

    companion object {
        private val elements: Map<String, CriteriaRelatesToEnum> = values().associateBy { it.value.toUpperCase() }

        fun fromString(value: String): CriteriaRelatesToEnum = elements[value.toUpperCase()]
            ?: throw EnumException(
                enumType = CriteriaRelatesToEnum::class.java.canonicalName,
                value = value,
                values = values().joinToString { it.value }
            )
    }
}
