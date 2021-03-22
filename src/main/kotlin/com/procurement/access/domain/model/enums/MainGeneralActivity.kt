package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.EnumElementProvider

enum class MainGeneralActivity(@JsonValue override val key: String) : EnumElementProvider.Key {

    DEFENCE("DEFENCE"),
    ECONOMIC_AND_FINANCIAL_AFFAIRS("ECONOMIC_AND_FINANCIAL_AFFAIRS"),
    EDUCATION("EDUCATION"),
    ENVIRONMENT("ENVIRONMENT"),
    GENERAL_PUBLIC_SERVICES("GENERAL_PUBLIC_SERVICES"),
    HEALTH("HEALTH"),
    HOUSING_AND_COMMUNITY_AMENITIES("HOUSING_AND_COMMUNITY_AMENITIES"),
    PUBLIC_ORDER_AND_SAFETY("PUBLIC_ORDER_AND_SAFETY"),
    RECREATION_CULTURE_AND_RELIGION("RECREATION_CULTURE_AND_RELIGION"),
    SOCIAL_PROTECTION("SOCIAL_PROTECTION");

    override fun toString(): String = key

    companion object : EnumElementProvider<MainGeneralActivity>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = orThrow(name)
    }
}
