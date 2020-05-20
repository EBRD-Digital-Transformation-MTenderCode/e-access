package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.EnumElementProvider

enum class ConversionsRelatesTo(@JsonValue override val key: String) : EnumElementProvider.Key {
    REQUIREMENT("requirement"),
    OBSERVATION("observation"),
    OPTION("option");

    override fun toString(): String = key

    companion object : EnumElementProvider<ConversionsRelatesTo>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = ConversionsRelatesTo.orThrow(name)
    }
}
