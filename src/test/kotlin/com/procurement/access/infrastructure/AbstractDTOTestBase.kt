package com.procurement.access.infrastructure

import com.procurement.access.json.JsonValidator
import com.procurement.access.json.loadJson
import com.procurement.access.json.toJson
import com.procurement.access.json.toObject
import org.junit.jupiter.api.Assertions.assertNotNull

abstract class AbstractDTOTestBase<T>(private val target: Class<T>) {
    fun testBindingAndMapping(pathToJsonFile: String) {
        println("Path to JSON file: '$pathToJsonFile'.")
        val expected = loadJson(pathToJsonFile)

        val obj = expected.toObject(target)
        assertNotNull(obj)

        val actual = obj.toJson()

        JsonValidator.equalsJsons(expected, actual)
    }
}
