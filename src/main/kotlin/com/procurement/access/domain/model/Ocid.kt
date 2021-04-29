package com.procurement.access.domain.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.EnumElementProvider.Companion.keysAsStrings
import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.domain.util.extension.toMilliseconds
import com.procurement.access.lib.functional.Result
import java.io.Serializable
import java.time.LocalDateTime

/**
 * `private val` used for correct deserialization. With public it's fail
 *
 * Alternative way it's use annotation (above constructor or factory method of child class) with specifying mode.
 * Ex: @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
 */
sealed class Ocid(@JsonValue val value: String) : Serializable {

    override fun equals(other: Any?): Boolean {
        return if (this !== other)
            other is Ocid
                && this.value == other.value
        else
            true
    }

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value

    fun extractCpidOrNull() = Cpid.tryCreateOrNull(value.substring(0..27))

    class MultiStage private constructor(value: String) : Ocid(value = value) {

        companion object {
            private val regex = "^[a-z]{4}-[a-z0-9]{6}-[A-Z]{2}-[0-9]{13}\$".toRegex()

            val pattern: String
                get() = regex.pattern

            @JvmStatic
            @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
            fun tryCreateOrNull(value: String): MultiStage? =
                if (value.matches(regex)) MultiStage(value = value) else null

            fun generate(cpid: Cpid): MultiStage = MultiStage(cpid.value)
        }
    }

    class SingleStage private constructor(value: String, val stage: Stage) : Ocid(value = value) {

        companion object {
            private const val STAGE_POSITION = 4
            private val STAGES: String
                get() = Stage.allowedElements.keysAsStrings()
                    .joinToString(separator = "|", prefix = "(", postfix = ")") { it.toUpperCase() }

            private val regex = "^[a-z]{4}-[a-z0-9]{6}-[A-Z]{2}-[0-9]{13}-$STAGES-[0-9]{13}\$".toRegex()

            val pattern: String
                get() = regex.pattern

            @JvmStatic
            @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
            fun tryCreateOrNull(value: String): SingleStage? =
                if (value.matches(regex)) {
                    val stage = Stage.orNull(value.split("-")[STAGE_POSITION])!!
                    SingleStage(stage = stage, value = value)
                } else
                    null

            fun tryCreate(value: String): Result<SingleStage, String> =
                if (value.matches(regex)) {
                    val stage = Stage.orNull(value.split("-")[STAGE_POSITION])!!
                    Result.success(SingleStage(stage = stage, value = value))
                } else
                    Result.failure(pattern)

            fun generate(cpid: Cpid, stage: Stage, timestamp: LocalDateTime): SingleStage =
                SingleStage("$cpid-$stage-${timestamp.toMilliseconds()}", stage)
        }
    }
}
