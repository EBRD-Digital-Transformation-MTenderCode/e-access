package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.exception.EnumException

enum class SubmissionMethod(@JsonValue val value: String) {
    ELECTRONIC_SUBMISSION("electronicSubmission"),
    ELECTRONIC_AUCTION("electronicAuction"),
    WRITTEN("written"),
    IN_PERSON("inPerson");

    override fun toString(): String {
        return this.value
    }

    companion object {
        private val elements: Map<String, SubmissionMethod> = values().associateBy { it.value.toUpperCase() }

        fun fromString(value: String): SubmissionMethod = elements[value.toUpperCase()]
            ?: throw EnumException(
                enumType = SubmissionMethod::class.java.canonicalName,
                value = value,
                values = values().joinToString { it.value })
    }
}
