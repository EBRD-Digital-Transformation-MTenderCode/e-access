package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.exception.EnumException

enum class SubmissionMethodRationale(@JsonValue val value: String) {
    TOOLS_DEVICES_FILE_FORMATS_UNAVAILABLE("TOOLS_DEVICES_FILE_FORMATS_UNAVAILABLE"),
    IPR_ISSUES("IPR_ISSUES"),
    REQUIRES_SPECIALISED_EQUIPMENT("REQUIRES_SPECIALISED_EQUIPMENT"),
    PHYSICAL_MODEL("PHYSICAL_MODEL"),
    SENSITIVE_INFORMATION("SENSITIVE_INFORMATION");

    override fun toString(): String {
        return this.value
    }

    companion object {
        private val elements: Map<String, SubmissionMethodRationale> = values().associateBy { it.value.toUpperCase() }

        fun fromString(value: String): SubmissionMethodRationale = elements[value.toUpperCase()]
            ?: throw EnumException(
                enumType = SubmissionMethodRationale::class.java.canonicalName,
                value = value,
                values = values().joinToString { it.value }
            )
    }
}
