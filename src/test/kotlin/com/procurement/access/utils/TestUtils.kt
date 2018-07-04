//package com.procurement.access.utils
//
//import com.fasterxml.jackson.core.type.TypeReference
//import com.fasterxml.jackson.databind.DeserializationFeature
//import com.fasterxml.jackson.databind.ObjectMapper
//import com.fasterxml.jackson.databind.node.JsonNodeFactory
//import org.junit.jupiter.api.Assertions
//
//private object JsonMapper {
//    val mapper: ObjectMapper = ObjectMapper()
//
//    init {
//        mapper.configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, true)
//        mapper.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)
//        mapper.nodeFactory = JsonNodeFactory.withExactBigDecimals(true)
//    }
//}
//
//fun <T> compare(target: Class<T>, file: String) {
//    val resource = getJsonFromFile(file)
//    val obj = toObject(target, resource)
//    val actual = toJson(obj)
//    Assertions.assertEquals(resource, actual)
//}
//
//fun getJsonFromFile(fileName: String): String {
//    return toCompact(JsonMapper.javaClass.getResource(fileName).readText())
//}
//
//private fun toCompact(source: String): String {
//    val type = object : TypeReference<Map<String, Any>>() {}
//    val map: Map<String, Any> = JsonMapper.mapper.readValue(source, type)
//    return JsonMapper.mapper.writeValueAsString(map)
//}
