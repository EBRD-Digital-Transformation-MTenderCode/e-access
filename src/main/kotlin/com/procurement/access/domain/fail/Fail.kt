package com.procurement.access.domain.fail

import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.application.service.Logger
import com.procurement.access.domain.EnumElementProvider
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.ValidationResult

sealed class Fail {

    abstract val code: String
    abstract val description: String
    abstract fun logging(logger: Logger)

    abstract class Error(val prefix: String) : Fail() {
        val message: String
            get() = "ERROR CODE: '$code', DESCRIPTION: '$description'."

        override fun logging(logger: Logger) {
            logger.error(message = message)
        }

        companion object {
            fun <T, E : Error> E.toResult(): Result<T, E> = Result.failure(this)

            fun <E : Error> E.toValidationResult(): ValidationResult<E> = ValidationResult.error(this)
        }
    }

    sealed class Incident(val level: Level, number: String, override val description: String) : Fail() {
        override val code: String = "INC-$number"

        val message: String
            get() = "INCIDENT CODE: '$code', DESCRIPTION: '$description'."

        override fun logging(logger: Logger) {
            when (level) {
                Level.ERROR -> logger.error(message)
                Level.WARNING -> logger.warn(message)
                Level.INFO -> logger.info(message)
            }
        }

        class Database(val exception: Exception) : Incident(
            level = Level.ERROR,
            number = "01",
            description = "Database incident."
        ) {
            override fun logging(logger: Logger) {
                logger.error(message = message, exception = exception)
            }
        }

        sealed class Transform(val number: String, override val description: String) :
            Incident(level = Level.ERROR, number = number, description = description) {

            abstract val exception: Exception?

            override fun logging(logger: Logger) {
                logger.error(message = message, exception = exception)
            }

            class ParseFromDatabaseIncident(val jsonData: String, override val exception: Exception) : Transform(
                number = "2.1",
                description = "Could not parse data stored in database."
            ) {
                override fun logging(logger: Logger) {
                    logger.error(message = message, mdc = mapOf("jsonData" to jsonData), exception = exception)
                }
            }

            class Parsing(className: String, override val exception: Exception) :
                Transform(number = "2.2", description = "Error parsing to $className.")

            class Mapping(description: String, override val exception: Exception? = null) :
                Transform(number = "2.4", description = description)

            class Deserialization(description: String, override val exception: Exception) :
                Transform(number = "2.5", description = description)

            class Serialization(description: String, override val exception: Exception) :
                Transform(number = "2.6", description = description)
        }

        class Parsing(val className: String, val exception: Exception) : Incident(
            level = Level.ERROR,
            number = "02",
            description = "Error parsing to $className."
        ) {
            override fun logging(logger: Logger) {
                logger.error(message = message, exception = exception)
            }
        }

        class DatabaseIncident(val exception: Exception? = null, val mdc: Map<String, String> = emptyMap()) : Incident(
            level = Level.ERROR,
            number = "03",
            description = "Internal Server Error."
        ) {
            override fun logging(logger: Logger) {
                logger.error(message = message, mdc = mdc, exception = exception)
            }
        }

        class Transforming(val exception: Exception) :
            Incident(
                level = Level.ERROR,
                number = "04",
                description = "Error transforming."
            ) {
            override fun logging(logger: Logger) {
                logger.error(message = message, exception = exception)
            }
        }

        class ParsingIncident : Incident(
            level = Level.ERROR,
            number = "05",
            description = "Internal Server Error."
        ) {
            override fun logging(logger: Logger) {
                logger.error(message = message)
            }
        }

        enum class Level(@JsonValue override val key: String) : EnumElementProvider.Key {
            ERROR("error"),
            WARNING("warning"),
            INFO("info");

            companion object : EnumElementProvider<Level>(info = info())
        }
    }
}
