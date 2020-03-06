package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.EnumElementProvider

enum class ExtendedProcurementCategory(@JsonValue override val key: String) : EnumElementProvider.Key {
    GOODS("goods"),
    WORKS("works"),
    SERVICES("services"),
    CONSULTING_SERVICES("consultingServices");

    override fun toString(): String = key

    companion object : EnumElementProvider<ExtendedProcurementCategory>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = ExtendedProcurementCategory.orThrow(name)
    }
}
