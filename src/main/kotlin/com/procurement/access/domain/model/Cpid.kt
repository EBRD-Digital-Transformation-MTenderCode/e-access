package com.procurement.access.domain.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.model.country.CountryId
import com.procurement.access.domain.util.extension.toMilliseconds
import com.procurement.access.lib.functional.Result
import java.time.LocalDateTime

class Cpid private constructor(@JsonValue val value: String) {

    override fun equals(other: Any?): Boolean {
        return if (this !== other)
            other is Cpid
                && this.value == other.value
        else
            true
    }

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value

    companion object {
        private val regex = "^[a-z]{4}-[a-z0-9]{6}-[A-Z]{2}-[0-9]{13}\$".toRegex()

        val pattern: String
            get() = regex.pattern

        @JvmStatic
        @JsonCreator
        fun tryCreateOrNull(value: String): Cpid? = if (value.matches(regex)) Cpid(value = value) else null

        fun tryCreate(value: String): Result<Cpid, String> =
            if (value.matches(regex)) Result.success(Cpid(value = value))
            else Result.failure(pattern)

        fun generate(prefix: String, country: CountryId, timestamp: LocalDateTime): Cpid =
            Cpid("${prefix.toLowerCase()}-${country.toUpperCase()}-${timestamp.toMilliseconds()}")
    }
}
