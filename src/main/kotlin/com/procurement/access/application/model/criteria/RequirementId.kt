package com.procurement.access.application.model.criteria

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.model.UUID_PATTERN
import com.procurement.access.domain.model.isUUID
import java.io.Serializable
import java.util.*

sealed class RequirementId(private val value: String) : Serializable {

    companion object {

        @JvmStatic
        @JsonCreator
        fun parse(text: String): RequirementId? = Permanent.tryCreateOrNull(text)
    }

    override fun equals(other: Any?): Boolean {
        return if (this !== other)
            other is RequirementId
                && this.value == other.value
        else
            true
    }

    override fun hashCode(): Int = value.hashCode()

    @JsonValue
    override fun toString(): String = value

    class Temporal private constructor(value: String) : RequirementId(value), Serializable {
        companion object {
            fun create(text: String): RequirementId = Temporal(text)
        }
    }

    class Permanent private constructor(value: String) : RequirementId(value), Serializable {
        companion object {
            val pattern: String
                get() = UUID_PATTERN

            fun validation(text: String): Boolean = text.isUUID()

            fun tryCreateOrNull(text: String): RequirementId? = if (validation(text)) Permanent(text) else null

            fun generate(): RequirementId = Permanent(UUID.randomUUID().toString())
        }
    }
}