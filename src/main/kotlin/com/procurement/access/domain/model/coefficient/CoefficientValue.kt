package com.procurement.access.domain.model.coefficient

import java.math.BigDecimal

sealed class CoefficientValue {

    companion object {
        fun of(value: Boolean): CoefficientValue = AsBoolean(value)
        fun of(value: String): CoefficientValue = AsString(value)
        fun of(value: BigDecimal): CoefficientValue = AsNumber(value)
        fun of(value: Long): CoefficientValue = AsInteger(value)
    }

    class AsBoolean(val value: Boolean) : CoefficientValue() {

        override fun equals(other: Any?): Boolean = if (this === other)
            true
        else
            other is AsBoolean
                && this.value == other.value

        override fun hashCode(): Int = value.hashCode()

        override fun toString(): String = value.toString()
    }

    class AsString(val value: String) : CoefficientValue() {

        override fun equals(other: Any?): Boolean = if (this === other)
            true
        else
            other is AsString
                && this.value == other.value

        override fun hashCode(): Int = value.hashCode()

        override fun toString(): String = value
    }

    class AsNumber private constructor(val value: BigDecimal) : CoefficientValue() {

        companion object {
            const val AVAILABLE_SCALE = 3
            private const val STRING_FORMAT = "%.${AVAILABLE_SCALE}f"

            operator fun invoke(value: BigDecimal): AsNumber {
                checkScale(value)
                return AsNumber(value)
            }

            private fun checkScale(value: BigDecimal) {
                val scale = value.scale()
                require(scale <= AVAILABLE_SCALE) {
                    "The 'coefficient value' is an invalid scale '$scale', the maximum scale: '${AVAILABLE_SCALE}'."
                }
            }
        }

        override fun equals(other: Any?): Boolean = if (this === other)
            true
        else
            other is AsNumber
                && this.value == other.value

        override fun hashCode(): Int = value.hashCode()

        override fun toString(): String = STRING_FORMAT.format(value)
    }

    class AsInteger(val value: Long) : CoefficientValue() {

        override fun equals(other: Any?): Boolean = if (this === other)
            true
        else
            other is AsInteger
                && this.value == other.value

        override fun hashCode(): Int = value.hashCode()

        override fun toString(): String = value.toString()
    }
}
