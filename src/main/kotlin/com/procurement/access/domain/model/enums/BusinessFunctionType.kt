package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.EnumElementProvider

enum class BusinessFunctionType(@JsonValue override val key: String) : EnumElementProvider.Key {
    @Deprecated("Use 'chairman' instead of it")
    AUTHORITY("authority"),

    CHAIRMAN("chairman"),
    PROCURMENT_OFFICER("procurementOfficer"),
    CONTACT_POINT("contactPoint"),
    TECHNICAL_EVALUATOR("technicalEvaluator"),
    TECHNICAL_OPENER("technicalOpener"),
    PRICE_OPENER("priceOpener"),
    PRICE_EVALUATOR("priceEvaluator");

    override fun toString(): String = key

    companion object : EnumElementProvider<BusinessFunctionType>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = BusinessFunctionType.orThrow(name)
    }
}
