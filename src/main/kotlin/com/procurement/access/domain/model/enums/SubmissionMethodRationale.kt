package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.EnumElementProvider

enum class SubmissionMethodRationale(@JsonValue override val key: String) : EnumElementProvider.Key {
    TOOLS_DEVICES_FILE_FORMATS_UNAVAILABLE("TOOLS_DEVICES_FILE_FORMATS_UNAVAILABLE"),
    IPR_ISSUES("IPR_ISSUES"),
    REQUIRES_SPECIALISED_EQUIPMENT("REQUIRES_SPECIALISED_EQUIPMENT"),
    PHYSICAL_MODEL("PHYSICAL_MODEL"),
    SENSITIVE_INFORMATION("SENSITIVE_INFORMATION");

    override fun toString(): String = key

    companion object : EnumElementProvider<SubmissionMethodRationale>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = SubmissionMethodRationale.orThrow(name)
    }
}
