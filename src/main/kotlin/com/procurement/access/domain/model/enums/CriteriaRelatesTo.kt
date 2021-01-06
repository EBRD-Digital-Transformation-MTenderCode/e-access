package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.EnumElementProvider

enum class CriteriaRelatesTo(@JsonValue override val key: String) : EnumElementProvider.Key {
    AWARD("award"),
    ITEM("item"),
    LOT("lot"),
    QUALIFICATION("qualification"),
    TENDER("tender"),
    TENDERER("tenderer");

    override fun toString(): String = key

    companion object : EnumElementProvider<CriteriaRelatesTo>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = CriteriaRelatesTo.orThrow(name)
    }
}
