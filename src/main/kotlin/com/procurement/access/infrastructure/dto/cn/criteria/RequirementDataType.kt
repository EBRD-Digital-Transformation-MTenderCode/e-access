package com.procurement.access.infrastructure.dto.cn.criteria

import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.exception.EnumException

enum class RequirementDataType(private val value: String) {

    NUMBER("number"),
    BOOLEAN("boolean"),
    STRING("string"),
    INTEGER("integer");

    @JsonValue
    fun value(): String {
        return this.value
    }

    override fun toString(): String {
        return this.value
    }

    companion object {
        private val elements: Map<String, RequirementDataType> =
            values().associateBy { it.value.toUpperCase() }

        fun fromString(value: String): RequirementDataType =
            elements[value.toUpperCase()] ?: throw EnumException(
                enumType = RequirementDataType::class.java.canonicalName,
                value = value,
                values = values().joinToString { it.value })
    }
}
