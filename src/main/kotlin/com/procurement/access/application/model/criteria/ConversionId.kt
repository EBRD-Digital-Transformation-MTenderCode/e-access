package com.procurement.access.application.model.criteria

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.model.UUID_PATTERN
import com.procurement.access.domain.model.isUUID
import java.io.Serializable
import java.util.*

sealed class ConversionId(private val value: String) : Serializable {

    companion object {

        @JvmStatic
        @JsonCreator
        fun parse(text: String): ConversionId? = Permanent.tryCreateOrNull(text)
    }

    override fun equals(other: Any?): Boolean {
        return if (this !== other)
            other is ConversionId
                && this.value == other.value
        else
            true
    }

    override fun hashCode(): Int = value.hashCode()

    @JsonValue
    override fun toString(): String = value

    class Temporal private constructor(value: String) : ConversionId(value), Serializable {
        companion object {
            fun create(text: String): ConversionId = Temporal(text)
        }
    }

    class Permanent private constructor(value: String) : ConversionId(value), Serializable {
        companion object {
            val pattern: String
                get() = UUID_PATTERN

            fun validation(text: String): Boolean = text.isUUID()

            fun tryCreateOrNull(text: String): ConversionId? = if (validation(text)) Permanent(text) else null

            fun generate(): ConversionId = Permanent(UUID.randomUUID().toString())
        }
    }
}