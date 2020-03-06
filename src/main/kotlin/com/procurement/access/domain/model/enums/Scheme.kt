package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.EnumElementProvider

enum class Scheme(@JsonValue override val key: String) : EnumElementProvider.Key {
    CPV("CPV"),
    CPVS("CPVS"),
    GSIN("GSIN"),
    UNSPSC("UNSPSC"),
    CPC("CPC"),
    OKDP("OKDP"),
    OKPD("OKPD");

    override fun toString(): String = key

    companion object : EnumElementProvider<Scheme>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = Scheme.orThrow(name)
    }
}
