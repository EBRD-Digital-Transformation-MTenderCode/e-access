package com.procurement.access.domain.model.requirement

import com.procurement.access.domain.model.enums.RequirementDataType
import com.procurement.access.domain.model.enums.RequirementStatus
import java.math.BigDecimal
import java.time.LocalDateTime

class Requirement(
    val id: String,
    val title: String,
    val description: String?,
    val period: Period?,
    val dataType: RequirementDataType,
    val value: RequirementValue,
    val eligibleEvidences: List<EligibleEvidence>?,
    val status: RequirementStatus?,
    val datePublished: LocalDateTime?
) {

    data class Period(
        val startDate: LocalDateTime,
        val endDate: LocalDateTime
    )
}

sealed class RequirementValue

sealed class ExpectedValue : RequirementValue() {

    companion object {
        fun of(value: Boolean): ExpectedValue = AsBoolean(value)
        fun of(value: String): ExpectedValue = AsString(value)
        fun of(value: BigDecimal): ExpectedValue = AsNumber(value)
        fun of(value: Long): ExpectedValue = AsInteger(value)
    }

    class AsBoolean(val value: Boolean) : ExpectedValue() {

        override fun equals(other: Any?): Boolean = if (this === other)
            true
        else
            other is AsBoolean
                && this.value == other.value

        override fun hashCode(): Int = value.hashCode()

        override fun toString(): String = value.toString()
    }

    class AsString(val value: String) : ExpectedValue() {

        override fun equals(other: Any?): Boolean = if (this === other)
            true
        else
            other is AsString
                && this.value == other.value

        override fun hashCode(): Int = value.hashCode()

        override fun toString(): String = value
    }

    class AsNumber private constructor(val value: BigDecimal) : ExpectedValue() {

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
                    "The 'expected value' is an invalid scale '$scale', the maximum scale: '$AVAILABLE_SCALE'."
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

    class AsInteger(val value: Long) : ExpectedValue() {

        override fun equals(other: Any?): Boolean = if (this === other)
            true
        else
            other is AsInteger
                && this.value == other.value

        override fun hashCode(): Int = value.hashCode()

        override fun toString(): String = value.toString()
    }
}

sealed class MinValue : RequirementValue() {

    companion object {
        fun of(value: BigDecimal): MinValue = AsNumber(value)
        fun of(value: Long): MinValue = AsInteger(value)
    }

    class AsNumber private constructor(val value: BigDecimal) : MinValue() {

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
                    "The 'min value' is an invalid scale '$scale', the maximum scale: '$AVAILABLE_SCALE'."
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

    class AsInteger(val value: Long) : MinValue() {

        override fun equals(other: Any?): Boolean = if (this === other)
            true
        else
            other is AsInteger
                && this.value == other.value

        override fun hashCode(): Int = value.hashCode()

        override fun toString(): String = value.toString()
    }
}

sealed class MaxValue : RequirementValue() {

    companion object {
        fun of(value: BigDecimal): MaxValue = AsNumber(value)
        fun of(value: Long): MaxValue = AsInteger(value)
    }

    class AsNumber private constructor(val value: BigDecimal) : MaxValue() {

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
                    "The 'max value' is an invalid scale '$scale', the maximum scale: '$AVAILABLE_SCALE'."
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

    class AsInteger(val value: Long) : MaxValue() {

        override fun equals(other: Any?): Boolean = if (this === other)
            true
        else
            other is AsInteger
                && this.value == other.value

        override fun hashCode(): Int = value.hashCode()

        override fun toString(): String = value.toString()
    }
}

sealed class RangeValue : RequirementValue() {

    companion object {
        fun of(minValue: BigDecimal, maxValue: BigDecimal): RangeValue = AsNumber(
            minValue = minValue,
            maxValue = maxValue
        )

        fun of(minValue: Long, maxValue: Long): RangeValue = AsInteger(minValue = minValue, maxValue = maxValue)
    }

    data class AsNumber(val minValue: BigDecimal, val maxValue: BigDecimal) : RangeValue()
    data class AsInteger(val minValue: Long, val maxValue: Long) : RangeValue()
}

object NoneValue : RequirementValue()

fun Requirement.compareScale(allowedScale: Int): Int {
    return when (dataType) {
        RequirementDataType.NUMBER -> {
            when (value) {
                is ExpectedValue.AsNumber ->
                    if (value.value.scale() > allowedScale) -1
                    else 1
                is MinValue.AsNumber ->
                    if (value.value.scale() > allowedScale) -1
                    else 1
                is MaxValue.AsNumber ->
                    if (value.value.scale() > allowedScale) -1
                    else 1
                is RangeValue.AsNumber -> {
                    if (value.minValue.scale() > allowedScale || value.maxValue.scale() > allowedScale) -1
                    else 1
                }
                is MinValue.AsInteger,
                is RangeValue.AsInteger,
                is MaxValue.AsInteger,
                is ExpectedValue.AsBoolean,
                is ExpectedValue.AsString,
                is ExpectedValue.AsInteger,
                NoneValue -> 0
            }
        }
        RequirementDataType.BOOLEAN,
        RequirementDataType.STRING,
        RequirementDataType.INTEGER -> 0
    }
}

fun Requirement.hasInvalidScale(allowedScale: Int) = compareScale(allowedScale) == -1
