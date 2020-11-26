package com.procurement.access.infrastructure.api

import com.fasterxml.jackson.annotation.JsonValue

class ApiVersion private constructor(@JsonValue val underlying: String) : Comparable<ApiVersion> {

    companion object {
        const val pattern: String = "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\$"
        private val regex = pattern.toRegex()

        val NaN = ApiVersion(0, 0, 0)

        fun orNull(version: String): ApiVersion? = if (version.matches(regex)) ApiVersion(version) else null

        fun orThrow(version: String, builder: (String) -> Exception): ApiVersion =
            if (version.matches(regex)) ApiVersion(version) else throw builder(version)
    }

    constructor(major: Int, minor: Int, patch: Int) : this("$major.$minor.$patch")

    override fun equals(other: Any?): Boolean {
        return if (this !== other)
            other is ApiVersion
                && this.underlying == other.underlying
        else
            true
    }

    override fun hashCode(): Int = underlying.hashCode()

    override fun toString(): String = underlying

    override fun compareTo(other: ApiVersion): Int = underlying.compareTo(other.underlying)
}
