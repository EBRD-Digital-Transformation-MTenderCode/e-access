package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.EnumElementProvider

enum class RequirementDataType(@JsonValue override val key: String) : EnumElementProvider.Key {

    BOOLEAN("boolean"),
    STRING("string"),
    NUMBER("number"),
    INTEGER("integer");

    override fun toString(): String = key

    companion object : EnumElementProvider<RequirementDataType>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = RequirementDataType.orThrow(name)
    }
}
