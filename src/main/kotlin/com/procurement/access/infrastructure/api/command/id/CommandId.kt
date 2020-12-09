package com.procurement.access.infrastructure.api.command.id

import java.util.*

class CommandId private constructor(val underlying: String) {

    companion object {
        @JvmStatic
        val NaN = CommandId(UUID(0, 0).toString())

        operator fun invoke(text: String) = CommandId(text)
    }

    override fun equals(other: Any?): Boolean {
        return if (this !== other)
            other is CommandId
                && this.underlying == other.underlying
        else
            true
    }

    override fun hashCode(): Int = underlying.hashCode()

    override fun toString(): String = underlying
}
