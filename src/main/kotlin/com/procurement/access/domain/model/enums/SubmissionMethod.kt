package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.EnumElementProvider

enum class SubmissionMethod(@JsonValue override val key: String) : EnumElementProvider.Key {
    ELECTRONIC_SUBMISSION("electronicSubmission"),
    ELECTRONIC_AUCTION("electronicAuction"),
    WRITTEN("written"),
    IN_PERSON("inPerson");

    override fun toString(): String = key

    companion object : EnumElementProvider<SubmissionMethod>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = SubmissionMethod.orThrow(name)
    }
}
