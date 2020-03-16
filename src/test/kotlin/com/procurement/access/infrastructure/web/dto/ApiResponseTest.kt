package com.procurement.access.infrastructure.web.dto

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.json.JsonValidator
import com.procurement.access.json.loadJson
import com.procurement.access.json.toJson
import org.junit.jupiter.api.Test
import java.util.*

internal class ApiResponseTest : AbstractDTOTestBase<ApiSuccessResponse>(ApiSuccessResponse::class.java) {

    companion object {
        private const val JSON_RESPONSE_WITH_NO_RESULT = "json/dto/api/api_response_no_result.json"
        private const val JSON_RESPONSE_WITH_RESULT_LIST = "json/dto/api/api_response_result_list.json"
        private const val JSON_RESPONSE_WITH_RESULT_OBJECT = "json/dto/api/api_response_result_object.json"
    }

    @Test
    fun nullResultTest() {
        val expectedJson = loadJson(JSON_RESPONSE_WITH_NO_RESULT)
        val apiResponse = getApiResponse(
            result = null
        )

        val actualJson = apiResponse.toJson()
        JsonValidator.equalsJsons(expectedJson, actualJson)
    }

    @Test
    fun emptyResultTest() {
        val expectedJson = loadJson(JSON_RESPONSE_WITH_NO_RESULT)
        val apiResponse2 = getApiResponse(
            result = emptyList<String>()
        )
        val actualJson = apiResponse2.toJson()

        JsonValidator.equalsJsons(expectedJson, actualJson)
    }

    @Test
    fun listResultTest() {
        val expectedJson = loadJson(JSON_RESPONSE_WITH_RESULT_LIST)
        val apiResponse2 = getApiResponse(
            result = listOf(
                "7b1584b8-5eb0-43d8-ad72-f7c074cc6bac",
                "42211541-4d8c-4d43-a1cd-7242a898e0b4"
            )
        )
        val actualJson = apiResponse2.toJson()

        JsonValidator.equalsJsons(expectedJson, actualJson)
    }

    @Test
    fun objectResultTest() {
        val expectedJson = loadJson(JSON_RESPONSE_WITH_RESULT_OBJECT)
        val apiResponse2 = getApiResponse(
            result = object {
                val first = "first"
            }
        )
        val actualJson = apiResponse2.toJson()

        JsonValidator.equalsJsons(expectedJson, actualJson)
    }

    @Test
    fun fullData() {
        testBindingAndMapping(JSON_RESPONSE_WITH_RESULT_LIST)
    }

    private fun getApiResponse(result: Any?) = ApiSuccessResponse(
        version = getApiVersion(),
        id = getId(),
        result = result
    )

    private fun getApiVersion() = ApiVersion(2, 0, 0)
    private fun getId() = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6")
}