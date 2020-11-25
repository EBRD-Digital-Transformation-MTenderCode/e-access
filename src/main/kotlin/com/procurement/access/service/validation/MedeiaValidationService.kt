package com.procurement.access.service.validation

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.utils.toJson
import com.worldturner.medeia.api.UrlSchemaSource
import com.worldturner.medeia.api.ValidationFailedException
import com.worldturner.medeia.api.jackson.MedeiaJacksonApi

interface JsonValidationService {
    fun <T: Any> validateCriteria(json: JsonNode, clazz: Class<T>): T
}

class MedeiaValidationService(
    private val objectMapper: ObjectMapper
) : JsonValidationService {

    private val api = MedeiaJacksonApi()
    private val criteriaSchema = UrlSchemaSource(javaClass.getResource("/json/criteria/check/check_criteria_schema.json"))

    private val criteriaValidator = api.loadSchema(criteriaSchema)

    override fun <T: Any> validateCriteria(json: JsonNode, clazz: Class<T>): T =
        try {
            val unvalidatedParser = objectMapper.factory.createParser(toJson(json))
            val decoratedParser = api.decorateJsonParser(criteriaValidator, unvalidatedParser)

            /* ¯╰( ´・ω・)つ──☆ ✿✿✿ */ api.parseAll(decoratedParser) /* ✿✿✿  */

            val validatedParser = objectMapper.factory.createParser(toJson(json))
            objectMapper.readValue(validatedParser, clazz)
        } catch (exception: Exception) {
            errorHandling(exception)
        }

    private fun errorHandling(exception: Exception): Nothing {
        when (exception) {
            is ValidationFailedException -> processingJsonSchemaException(exception = exception)
            is JsonMappingException      -> {
                val cause = exception.cause ?: exception
                if (cause is ValidationFailedException)
                    processingJsonSchemaException(exception = cause)

                throw ErrorException(
                    error = ErrorType.INVALID_JSON,
                    message = "Cannot validate json via json schema. ${(exception.message)}"
                )
            }
            else                         ->
                throw ErrorException(
                    error = ErrorType.INVALID_JSON,
                    message = "Cannot validate json via json schema. ${(exception.message)}"
                )
        }
    }

    private fun processingJsonSchemaException(exception: ValidationFailedException): Nothing {
        val errorDetails = StringBuilder()
        var delails = exception.failures[0].details
        if (exception.failures[0].property != null) errorDetails.append(exception.failures[0].property + " -> ")
        while (!delails.isEmpty()) {
            val fail = delails.toMutableList()[0]

            if (fail.property != null) errorDetails.append(fail.property + " -> ")
            if (delails.toMutableList().get(0).details.isEmpty()) errorDetails.append(fail.message)

            delails = delails.toMutableList().get(0).details
        }
        throw ErrorException(
            error = ErrorType.INVALID_JSON,
            message = errorDetails.toString()
        )
    }
}
