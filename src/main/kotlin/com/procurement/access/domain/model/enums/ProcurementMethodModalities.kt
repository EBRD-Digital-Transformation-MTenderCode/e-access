package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.EnumElementProvider

enum class ProcurementMethodModalities(@JsonValue override val key: String) : EnumElementProvider.Key {
    ELECTRONIC_AUCTION("electronicAuction"),
    REQUIRES_ELECTRONIC_CATALOGUE("requiresElectronicCatalogue");

    override fun toString(): String = key

    companion object : EnumElementProvider<ProcurementMethodModalities>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = ProcurementMethodModalities.orThrow(name)
    }
}
