package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import java.util.*

enum class ExtendedProcurementCategory(@JsonValue val value: String) {
    GOODS("goods"),
    WORKS("works"),
    SERVICES("services"),
    CONSULTING_SERVICES("consultingServices");

    override fun toString(): String {
        return this.value
    }
}

enum class LegalBasis(@JsonValue val value: String) {
    DIRECTIVE_2014_23_EU("DIRECTIVE_2014_23_EU"),
    DIRECTIVE_2014_24_EU("DIRECTIVE_2014_24_EU"),
    DIRECTIVE_2014_25_EU("DIRECTIVE_2014_25_EU"),
    DIRECTIVE_2009_81_EC("DIRECTIVE_2009_81_EC"),
    REGULATION_966_2012("REGULATION_966_2012"),
    NATIONAL_PROCUREMENT_LAW("NATIONAL_PROCUREMENT_LAW"),
    NULL("NULL");

    override fun toString(): String {
        return this.value
    }
}



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
}

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
}

enum class Operation(val value: String) {
    CREATE_CN("createCN"),
    CREATE_PN("createPN"),
    CREATE_PIN("createPIN"),
    UPDATE_CN("updateCN"),
    UPDATE_PN("updatePN"),
    CREATE_CN_ON_PN("createCNonPN"),
    CREATE_CN_ON_PIN("createCNonPIN"),
    CREATE_PIN_ON_PN("createPINonPN"),
    CREATE_NEGOTIATION_CN_ON_PN("createNegotiationCnOnPn");

    companion object {
        private val CONSTANTS = HashMap<String, Operation>()

        init {
            for (c in Operation.values()) {
                CONSTANTS[c.value] = c
            }
        }

        @JsonCreator
        fun fromValue(value: String): Operation {
            return CONSTANTS[value] ?: throw IllegalArgumentException(value)
        }
    }
}