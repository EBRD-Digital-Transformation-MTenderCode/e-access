package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.EnumElementProvider

enum class LocationOfPersonsType(@JsonValue override val key: String) : EnumElementProvider.Key {
    REQUIREMENT_RESPONSE("requirementResponse");

    override fun toString(): String = key

    companion object : EnumElementProvider<LocationOfPersonsType>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = SubmissionMethodRationale.orThrow(name)
    }
}
