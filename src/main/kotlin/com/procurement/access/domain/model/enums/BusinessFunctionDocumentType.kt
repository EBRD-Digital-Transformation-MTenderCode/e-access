package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.EnumElementProvider

enum class BusinessFunctionDocumentType(@JsonValue override val key: String) : EnumElementProvider.Key {
    REGULATORY_DOCUMENT("regulatoryDocument");

    override fun toString(): String = key

    companion object : EnumElementProvider<BusinessFunctionDocumentType>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = BusinessFunctionDocumentType.orThrow(name)
    }
}
