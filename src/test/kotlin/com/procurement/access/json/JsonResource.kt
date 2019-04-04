package com.procurement.access.json

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.procurement.access.model.dto.databinding.IntDeserializer
import com.procurement.access.model.dto.databinding.JsonDateTimeDeserializer
import com.procurement.access.model.dto.databinding.JsonDateTimeSerializer
import com.procurement.access.model.dto.databinding.StringsDeserializer
import java.io.IOException
import java.io.StringWriter
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime

typealias JSON = String

fun loadJson(fileName: String): JSON {
    return ClassPathResource.getFilePath(fileName).let { pathToFile ->
        val path = Paths.get(pathToFile)
        val buffer = Files.readAllBytes(path)
        String(buffer, Charset.defaultCharset())
    }
}

fun JSON.compact(): JSON {
    val factory = JsonFactory()
    val parser = factory.createParser(this)
    val out = StringWriter()
    factory.createGenerator(out).use { gen ->
        while (parser.nextToken() != null) {
            gen.copyCurrentEvent(parser)
        }
    }
    return out.buffer.toString()
}

inline fun <reified T> JsonNode.toObject(): T = try {
    JsonMapper.mapper.treeToValue(this, T::class.java)
} catch (e: IOException) {
    throw IllegalArgumentException(e)
}

inline fun <reified T> JSON.toObject(): T = this.toObject(T::class.java)

fun <T> JSON.toObject(target: Class<T>): T = try {
    JsonMapper.mapper.readValue(this, target)
} catch (exception: Exception) {
    throw RuntimeException(exception)
}

fun <T> T.toJson(): JSON = try {
    JsonMapper.mapper.writeValueAsString(this)
} catch (e: JsonProcessingException) {
    throw RuntimeException(e)
}

fun JSON.toNode(): JsonNode = try {
    JsonMapper.mapper.readTree(this)
} catch (e: JsonProcessingException) {
    throw RuntimeException(e)
}

private object ClassPathResource {
    fun getFilePath(fileName: String): String {
        return javaClass.classLoader.getResource(fileName)?.path
            ?: throw IllegalArgumentException("File by path: $fileName not found.")
    }
}

object JsonMapper {
    val mapper = ObjectMapper().apply {
        //TODO Refactoring here and utils.kt
        registerKotlinModule()
        registerModule(extendModule())

        configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, true)
        configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)
        configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        //TODO added ZERO to last !!!
//        nodeFactory = JsonNodeFactory.withExactBigDecimals(true)
    }

    private fun extendModule() =
        SimpleModule().apply {
            addSerializer(LocalDateTime::class.java, JsonDateTimeSerializer())
            addDeserializer(LocalDateTime::class.java, JsonDateTimeDeserializer())
            addDeserializer(String::class.java, StringsDeserializer())
            addDeserializer(Int::class.java, IntDeserializer())
        }
}