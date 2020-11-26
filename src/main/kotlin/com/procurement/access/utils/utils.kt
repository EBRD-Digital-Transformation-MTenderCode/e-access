package com.procurement.access.utils

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.bind.jackson.configuration
import com.procurement.access.lib.functional.Result
import java.io.IOException

private object JsonMapper {

    val mapper: ObjectMapper = ObjectMapper().apply {
        configuration()
    }
}

/*Json utils*/

fun <Any> toJson(obj: Any): String {
    try {
        return JsonMapper.mapper.writeValueAsString(obj)
    } catch (e: JsonProcessingException) {
        throw RuntimeException(e)
    }
}

fun <T> toObject(clazz: Class<T>, json: String): T {
    try {
        return JsonMapper.mapper.readValue(json, clazz)
    } catch (e: IOException) {
        throw IllegalArgumentException(e)
    }
}

fun <T> toObject(clazz: Class<T>, json: JsonNode): T {
    try {
        return JsonMapper.mapper.treeToValue(json, clazz)
    } catch (e: IOException) {
        throw IllegalArgumentException(e)
    }
}

fun <T : Any> JsonNode.tryToObject(target: Class<T>): Result<T, Fail.Incident.Parsing> = try {
    Result.success(JsonMapper.mapper.treeToValue(this, target))
} catch (expected: Exception) {
    Result.failure(Fail.Incident.Parsing(className = target.canonicalName, exception = expected))
}

fun <T : Any> String.tryToObject(target: Class<T>): Result<T, Fail.Incident.Parsing> = try {
    Result.success(JsonMapper.mapper.readValue(this, target))
} catch (expected: Exception) {
    Result.failure(Fail.Incident.Parsing(className = target.canonicalName, exception = expected))
}

fun String.toNode(): Result<JsonNode, Fail.Incident.Transforming> = try {
    Result.success(JsonMapper.mapper.readTree(this))
} catch (exception: JsonProcessingException) {
    Result.failure(Fail.Incident.Transforming(exception = exception))
}

fun <R> trySerialization(value: R): Result<String, Fail.Incident.Transforming> = try {
    Result.success(JsonMapper.mapper.writeValueAsString(value))
} catch (expected: Exception) {
    Result.failure(Fail.Incident.Transforming(exception = expected))
}
