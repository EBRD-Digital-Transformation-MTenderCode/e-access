package com.procurement.access.infrastructure.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode
import com.procurement.access.application.service.Transform
import com.procurement.access.domain.fail.Fail
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.Result.Companion.failure
import com.procurement.access.lib.functional.Result.Companion.success
import com.procurement.access.lib.functional.asFailure
import java.io.IOException

class JacksonJsonTransform(private val mapper: ObjectMapper) : Transform {

    /**
     * Parsing
     */
    override fun tryParse(value: String): Result<JsonNode, Fail.Incident.Transform.Parsing> = try {
        success(mapper.readTree(value))
    } catch (expected: IOException) {
        failure(Fail.Incident.Transform.Parsing(className = JsonNode::class.java.canonicalName, exception = expected))
    }

    /**
     * Mapping
     */
    override fun <R> tryMapping(value: JsonNode, target: Class<R>): Result<R, Fail.Incident.Transform.Mapping> =
        try {
            if (value is NullNode)
                Fail.Incident.Transform.Mapping(description = "Object to map must not be null.").asFailure()
            else
                success(mapper.treeToValue(value, target))
        } catch (expected: Exception) {
            Fail.Incident.Transform.Mapping(description = "Error of mapping.", exception = expected).asFailure()
        }

    override fun <R> tryMapping(
        value: JsonNode,
        typeRef: TypeReference<R>
    ): Result<R, Fail.Incident.Transform.Mapping> = try {
        val parser = mapper.treeAsTokens(value)
        success(mapper.readValue(parser, typeRef))
    } catch (expected: Exception) {
        Fail.Incident.Transform.Mapping(description = "Error of mapping.", exception = expected).asFailure()
    }

    /**
     * Deserialization
     */
    override fun <R> tryDeserialization(
        value: String,
        target: Class<R>
    ): Result<R, Fail.Incident.Transform.Deserialization> = try {
        success(mapper.readValue(value, target))
    } catch (expected: Exception) {
        Fail.Incident.Transform.Deserialization(description = "Error of deserialization.", exception = expected)
            .asFailure()
    }

    override fun <R> tryDeserialization(
        value: String,
        typeRef: TypeReference<R>
    ): Result<R, Fail.Incident.Transform.Deserialization> = try {
        success(mapper.readValue(value, typeRef))
    } catch (expected: Exception) {
        Fail.Incident.Transform.Deserialization(description = "Error of deserialization.", exception = expected)
            .asFailure()
    }

    /**
     * Serialization
     */
    override fun <R> trySerialization(value: R): Result<String, Fail.Incident.Transform.Serialization> = try {
        success(mapper.writeValueAsString(value))
    } catch (expected: Exception) {
        Fail.Incident.Transform.Serialization(description = "Error of serialization.", exception = expected)
            .asFailure()
    }

    /**
     * ???
     */
    override fun tryToJson(value: JsonNode): Result<String, Fail.Incident.Transform.Serialization> = try {
        success(mapper.writeValueAsString(value))
    } catch (expected: Exception) {
        Fail.Incident.Transform.Serialization(description = "Error of serialization.", exception = expected)
            .asFailure()
    }
}
