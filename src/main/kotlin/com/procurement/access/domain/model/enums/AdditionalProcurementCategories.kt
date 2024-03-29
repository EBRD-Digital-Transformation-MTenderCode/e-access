package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.EnumElementProvider

enum class AdditionalProcurementCategories(@JsonValue override val key: String) : EnumElementProvider.Key {
    GOODS("goods"),
    WORKS("works"),
    SERVICES("services");

    override fun toString(): String = key

    companion object : EnumElementProvider<AdditionalProcurementCategories>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = MainProcurementCategory.orThrow(name)
    }
}
