package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.EnumElementProvider

enum class CriteriaSource(@JsonValue override val key: String) : EnumElementProvider.Key {
    TENDERER("tenderer"),
    BUYER("buyer"),
    PROCURING_ENTITY("procuringEntity");

    override fun toString(): String = key

    companion object : EnumElementProvider<CriteriaSource>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = CriteriaSource.orThrow(name)
    }
}
