package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.EnumElementProvider

enum class TenderStatus(@JsonValue override val key: String) : EnumElementProvider.Key {
    PLANNING("planning"),
    PLANNED("planned"),
    ACTIVE("active"),
    CANCELLED("cancelled"),
    UNSUCCESSFUL("unsuccessful"),
    COMPLETE("complete");

    override fun toString(): String = key

    companion object : EnumElementProvider<TenderStatus>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = TenderStatus.orThrow(name)
    }
}
