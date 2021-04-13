package com.procurement.access.domain.model.process

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid

class RelatedProcessIdentifier private constructor(@JsonValue val value: String) {

    companion object {
        fun of(cpid: Cpid) = RelatedProcessIdentifier(cpid.value)
        fun of(ocid: Ocid) = RelatedProcessIdentifier(ocid.value)

        @JvmStatic
        @JsonCreator
        fun create(value: String) = RelatedProcessIdentifier(value)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RelatedProcessIdentifier

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }


}