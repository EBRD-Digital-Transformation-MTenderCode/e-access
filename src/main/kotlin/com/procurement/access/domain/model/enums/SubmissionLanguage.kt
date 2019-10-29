package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.exception.EnumException

enum class SubmissionLanguage(@JsonValue val value: String) {
    BG("bg"),
    ES("es"),
    CS("cs"),
    DA("da"),
    DE("de"),
    ET("et"),
    EL("el"),
    EN("en"),
    FR("fr"),
    GA("ga"),
    HR("hr"),
    IT("it"),
    LV("lv"),
    LT("lt"),
    HU("hu"),
    MT("mt"),
    NL("nl"),
    PL("pl"),
    PT("pt"),
    RO("ro"),
    SK("sk"),
    SL("sl"),
    FI("fi"),
    SV("sv");

    override fun toString(): String {
        return this.value
    }

    companion object {
        private val elements: Map<String, SubmissionLanguage> = values().associateBy { it.value.toUpperCase() }

        fun fromString(value: String): SubmissionLanguage = elements[value.toUpperCase()]
            ?: throw EnumException(
                enumType = SubmissionLanguage::class.java.canonicalName,
                value = value,
                values = values().joinToString { it.value }
            )
    }
}
