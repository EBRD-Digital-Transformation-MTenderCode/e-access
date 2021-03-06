package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.EnumElementProvider

enum class RelatedProcessScheme(@JsonValue override val key: String) : EnumElementProvider.Key {

    OCID("ocid");

    override fun toString(): String = key

    companion object : EnumElementProvider<RelatedProcessScheme>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = orThrow(name)
    }
}
