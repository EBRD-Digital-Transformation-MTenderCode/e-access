package com.procurement.access.domain.model.requirement

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.EnumElementProvider

enum class EligibleEvidenceType(@JsonValue override val key: String) : EnumElementProvider.Key {

    DOCUMENT("document"),
    REFERENCE("reference");

    override fun toString(): String = key

    companion object : EnumElementProvider<EligibleEvidenceType>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = EligibleEvidenceType.orThrow(name)
    }
}