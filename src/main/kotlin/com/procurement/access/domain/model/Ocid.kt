package com.procurement.access.domain.model

import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.domain.util.Result
import com.procurement.access.utils.getStageFromOcid

class Ocid private constructor(private val value: String, val stage: Stage) {

    override fun equals(other: Any?): Boolean {
        return if (this !== other)
            other is Ocid
                && this.value == other.value
        else
            true
    }

    override fun hashCode(): Int = value.hashCode()

    @JsonValue
    override fun toString(): String = value

    companion object {
        private val STAGES: String
            get() = Stage.values()
                .joinToString(separator = "|", prefix = "(", postfix = ")") { it.key.toUpperCase() }

        private val regex = "^[a-z]{4}-[a-z0-9]{6}-[A-Z]{2}-[0-9]{13}-$STAGES-[0-9]{13}\$".toRegex()

        val pattern: String
            get() = regex.pattern

        fun tryCreate(value: String): Result<Ocid, String> =
            if (value.matches(regex)) {
                val stage = Stage.orNull(value.getStageFromOcid())
                Result.success(Ocid(value = value, stage = stage!!))
            } else
                Result.failure(Cpid.pattern)

        fun tryCreateOrNull(value: String): Ocid? =
            if (value.matches(regex)) {
                val stage = Stage.orNull(value.getStageFromOcid())
                Ocid(value = value, stage = stage!!)
            } else
                null
    }
}
