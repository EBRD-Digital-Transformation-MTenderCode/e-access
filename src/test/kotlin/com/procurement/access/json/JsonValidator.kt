package com.procurement.access.json

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.fail

object JsonValidator {
    class JsonValues(private val valueByPaths: Map<String, String>) {
        fun assert(path: String, expectedValue: String) {
            val actualValue = getActualValue(path)
            equalsValues(path = path, expected = "\"$expectedValue\"", actual = actualValue)
        }

        fun assert(path: String, expectedValue: Boolean) {
            val actualValue = getActualValue(path)
            equalsValues(path = path, expected = expectedValue.toString(), actual = actualValue)
        }

        fun assert(path: String, expectedValue: Number) {
            val actualValue = getActualValue(path)
            equalsValues(path = path, expected = expectedValue.toString(), actual = actualValue)
        }

        private fun getActualValue(path: String): String {
            val actualValue = valueByPaths[path]
            return actualValue ?: Assertions.fail<String>("The path $path to check value not found.")
        }

        private fun equalsValues(path: String, expected: String, actual: String) {
            Assertions.assertEquals(
                expected,
                actual,
                "Invalid value by path $path (expected value: $expected, actual value: $actual)."
            )
        }
    }

    fun equalsJsons(expectedJson: String, actualJson: String, additionalChecks: (JsonValues.() -> Unit)? = null) {
        val expectedData: Map<String, String> = JsonPathParser.parse(expectedJson)
        val actualData: Map<String, String> = JsonPathParser.parse(actualJson)

        if (expectedData.size != actualData.size) {
            val intersect = expectedData.keys.intersect(actualData.keys)

            val message = buildString {
                appendln("Error comparing JSONs. Different number of attributes (expected json: ${expectedData.size} attributes, actual json: ${actualData.size} attributes)")
                for ((key, value) in expectedData) {
                    if (!intersect.contains(key))
                        appendln("EXPECTED JSON => path: $key, value: $value")
                }
                for ((key, value) in actualData) {
                    if (!intersect.contains(key))
                        appendln("ACTUAL JSON => path: $key, value: $value")
                }
            }
            fail<Unit>(message)
        }

        actualData.forEach { (path, value) ->
            val expectedValue = expectedData[path]
            assertNotNull(expectedValue, "Path $path not found in actual json.")
            assertEquals(
                expectedValue,
                value,
                "Actual value [$value] not equals expected value [$expectedValue] by path: $path."
            )
        }

        if (additionalChecks != null) additionalChecks(JsonValues(actualData))
    }

    fun checkValueByPath(json: String, checks: JsonValues.() -> Unit) {
        val actualData: Map<String, String> = JsonPathParser.parse(json)
        checks(JsonValues(actualData))
    }
}

