package com.procurement.access.application.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.domain.fail.Fail
import com.procurement.access.lib.functional.Result

interface Transform {

    /**
     * Parsing
     */
    fun tryParse(value: String): Result<JsonNode, Fail.Incident.Transform.Parsing>

    /**
     * Mapping
     */
    fun <R> tryMapping(value: JsonNode, target: Class<R>): Result<R, Fail.Incident.Transform.Mapping>
    fun <R> tryMapping(value: JsonNode, typeRef: TypeReference<R>): Result<R, Fail.Incident.Transform.Mapping>

    /**
     * Deserialization
     */
    fun <R> tryDeserialization(value: String, target: Class<R>): Result<R, Fail.Incident.Transform.Deserialization>
    fun <R> tryDeserialization(
        value: String,
        typeRef: TypeReference<R>
    ): Result<R, Fail.Incident.Transform.Deserialization>

    /**
     * Serialization
     */
    fun <R> trySerialization(value: R): Result<String, Fail.Incident.Transform.Serialization>

    /**
     * ???
     */
    fun tryToJson(value: JsonNode): Result<String, Fail.Incident.Transform.Serialization>
}

inline fun <reified T> String.tryDeserialization(transform: Transform) =
    transform.tryDeserialization(this, T::class.java)
