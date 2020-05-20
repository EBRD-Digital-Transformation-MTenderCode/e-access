package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.EnumElementProvider

enum class LotStatusDetails(@JsonValue override val key: String) : EnumElementProvider.Key {
    UNSUCCESSFUL("unsuccessful"),
    AWARDED("awarded"),
    CANCELLED("cancelled"),
    EMPTY("empty");

    override fun toString(): String = key

    companion object : EnumElementProvider<LotStatusDetails>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = LotStatusDetails.orThrow(name)
    }
}
