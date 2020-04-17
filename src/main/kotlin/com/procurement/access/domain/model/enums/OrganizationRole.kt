package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.EnumElementProvider

enum class OrganizationRole(@JsonValue override val key: String) : EnumElementProvider.Key {
    PROCURING_ENTITY("procuringEntity");

    override fun toString(): String = key

    companion object : EnumElementProvider<OrganizationRole>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = OrganizationRole.orThrow(name)
    }
}
