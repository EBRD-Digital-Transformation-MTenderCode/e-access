package com.procurement.access.service.validation.strategy

import com.fasterxml.jackson.databind.JsonNode
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.enums.MainProcurementCategory
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.dto.CheckItemsRequest
import com.procurement.access.infrastructure.dto.CheckItemsResponse
import com.procurement.access.infrastructure.entity.PNEntity
import com.procurement.access.infrastructure.generator.CommandMessageGenerator
import com.procurement.access.infrastructure.generator.ContextGenerator
import com.procurement.access.infrastructure.generator.TenderProcessEntityGenerator
import com.procurement.access.json.JSON
import com.procurement.access.json.JsonValidator
import com.procurement.access.json.getArray
import com.procurement.access.json.getObject
import com.procurement.access.json.getString
import com.procurement.access.json.loadJson
import com.procurement.access.json.setAttribute
import com.procurement.access.json.toJson
import com.procurement.access.json.toNode
import com.procurement.access.json.toObject
import com.procurement.access.model.dto.bpe.CommandMessage
import com.procurement.access.model.dto.bpe.CommandType
import com.procurement.access.utils.toObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class CheckItemsStrategyTest {
    companion object {
        private val REQUEST_ITEM_COMMON_PARTY_CPV_CODE = "12345"
        private val PN_TENDER_CPV_CODE = "12300458"
        private val INVALID_PN_TENDER_CPV_CODE = "17500458"
        private val RESPONSE_TENDER_CPV_CODE = "${REQUEST_ITEM_COMMON_PARTY_CPV_CODE}000"
    }

    private lateinit var tenderProcessDao: TenderProcessDao
    private lateinit var strategy: CheckItemsStrategy

    @BeforeEach
    fun init() {
        tenderProcessDao = mock()
        strategy = CheckItemsStrategy(tenderProcessDao)
    }

    @Nested
    inner class ValidationRules {

        /**
         * VR-3.14.1
         */
        @Nested
        inner class VR_3_14_1 {

            @Nested
            inner class A {

                @Nested
                inner class NoItems {
                    private val hasItems = false

                    @ParameterizedTest
                    @CsvSource(
                        value = [
                            "createCNonPN",
                            "createPINonPN",
                            "createNegotiationCnOnPn"
                        ]
                    )
                    fun success(operationName: String) {
                        val dataRequest = request()
                        val cm = commandMessage(operationName = operationName, data = dataRequest)

                        val dataEntity = tenderProcessEntityData(hasItems = hasItems)
                        mockGetByCpIdAndStage(
                            cpid = ContextGenerator.CPID,
                            stage = ContextGenerator.PREV_STAGE,
                            data = dataEntity
                        )

                        val actual = strategy.check(cm).toJson()
                        val expectedItems = dataRequest.toObject<CheckItemsRequest>().items
                        val expected = response(
                            mdmValidation = true,
                            itemsAdd = true,
                            id = RESPONSE_TENDER_CPV_CODE,
                            mainProcurementCategory = MainProcurementCategory.SERVICES,
                            items = expectedItems.map { CheckItemsResponse.Item(id = it.id, relatedLot = it.relatedLot) }
                        )

                        JsonValidator.equalsJsons(expectedJson = expected, actualJson = actual)
                    }

                    @Nested
                    inner class VR_3_14_2 {

                        @ParameterizedTest
                        @CsvSource(
                            value = [
                                "createCNonPN",
                                "createPINonPN",
                                "createNegotiationCnOnPn"
                            ]
                        )
                        fun requestWithoutItems(operationName: String) {
                            val dataRequest = "{\"items\":[]}".toNode()
                            val cm = commandMessage(operationName = operationName, data = dataRequest)

                            val dataEntity = tenderProcessEntityData(hasItems = hasItems)
                            mockGetByCpIdAndStage(
                                cpid = ContextGenerator.CPID,
                                stage = ContextGenerator.PREV_STAGE,
                                data = dataEntity
                            )

                            val exception = assertThrows<ErrorException> {
                                strategy.check(cm)
                            }
                            assertEquals(ErrorType.EMPTY_ITEMS, exception.error)
                        }

                        @ParameterizedTest
                        @CsvSource(
                            value = [
                                "createCNonPN",
                                "createPINonPN",
                                "createNegotiationCnOnPn"
                            ]
                        )
                        fun requestWithNotConsistentCPVCode(operationName: String) {
                            val dataRequest = requestWithNotConsistentCPVCodes()
                            val cm = commandMessage(operationName = operationName, data = dataRequest)

                            val dataEntity = tenderProcessEntityData(hasItems = hasItems)
                            mockGetByCpIdAndStage(
                                cpid = ContextGenerator.CPID,
                                stage = ContextGenerator.PREV_STAGE,
                                data = dataEntity
                            )

                            val exception = assertThrows<ErrorException> {
                                strategy.check(cm)
                            }
                            assertEquals(ErrorType.ITEMS_CPV_CODES_NOT_CONSISTENT, exception.error)
                        }
                    }

                    @ParameterizedTest
                    @CsvSource(
                        value = [
                            "createCNonPN",
                            "createPINonPN",
                            "createNegotiationCnOnPn"
                        ]
                    )
                    fun vr_3_14_3(operationName: String) {
                        val dataRequest = request()
                        val cm = commandMessage(operationName = operationName, data = dataRequest)

                        val dataEntity = tenderProcessEntityData(hasItems = hasItems, id = INVALID_PN_TENDER_CPV_CODE)
                        mockGetByCpIdAndStage(
                            cpid = ContextGenerator.CPID,
                            stage = ContextGenerator.PREV_STAGE,
                            data = dataEntity
                        )

                        val exception = assertThrows<ErrorException> {
                            strategy.check(cm)
                        }
                        assertEquals(ErrorType.CALCULATED_CPV_CODE_NO_MATCH_TENDER_CPV_CODE, exception.error)
                    }
                }

                @Nested
                inner class Items {
                    private val hasItems = true

                    @ParameterizedTest
                    @CsvSource(
                        value = [
                            "createCNonPN",
                            "createPINonPN",
                            "createNegotiationCnOnPn"
                        ]
                    )
                    fun success(operationName: String) {
                        val dataRequest = request()
                        val cm = commandMessage(operationName = operationName, data = dataRequest)

                        val dataEntity = tenderProcessEntityData(hasItems = hasItems)
                        mockGetByCpIdAndStage(
                            cpid = ContextGenerator.CPID,
                            stage = ContextGenerator.PREV_STAGE,
                            data = dataEntity
                        )

                        val actual = strategy.check(cm).toJson()
                        val expectedItems = toObject(clazz = PNEntity::class.java, json = dataEntity).tender.items
                        val expected = response(
                            mdmValidation = false,
                            itemsAdd = true,
                            mainProcurementCategory = MainProcurementCategory.SERVICES,
                            items = expectedItems.map { CheckItemsResponse.Item(id = it.id, relatedLot = it.relatedLot) }
                        )
                        JsonValidator.equalsJsons(expectedJson = expected, actualJson = actual)
                    }
                }
            }

            @Nested
            inner class B {

                @ParameterizedTest
                @CsvSource(
                    value = [
                        "createPN",
                        "createPIN",
                        "createCN"
                    ]
                )
                fun success(operationName: String) {
                    val dataRequest = request()
                    val cm = commandMessage(operationName = operationName, data = dataRequest)

                    val actual = strategy.check(cm).toJson()
                    val expected = response(mdmValidation = true, itemsAdd = true, id = RESPONSE_TENDER_CPV_CODE)
                    JsonValidator.equalsJsons(expectedJson = expected, actualJson = actual)
                }
            }

            @Nested
            inner class C {

                @Nested
                inner class NoItems {
                    private val hasItems = false

                    @ParameterizedTest
                    @CsvSource(
                        value = [
                            "updatePN"
                        ]
                    )
                    fun success(operationName: String) {
                        val dataRequest = request()
                        val cm = commandMessage(operationName = operationName, data = dataRequest)

                        val dataEntity = tenderProcessEntityData(hasItems = hasItems)
                        val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = dataEntity)
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(ContextGenerator.CPID),
                                eq(ContextGenerator.STAGE)
                            )
                        ).thenReturn(tenderProcessEntity)

                        val actual = strategy.check(cm).toJson()
                        val expected = response(mdmValidation = true, itemsAdd = true, id = RESPONSE_TENDER_CPV_CODE)

                        JsonValidator.equalsJsons(expectedJson = expected, actualJson = actual)
                    }

                    @Nested
                    inner class VR_3_14_2 {
                        @ParameterizedTest
                        @CsvSource(
                            value = [
                                "updatePN"
                            ]
                        )
                        fun requestWithoutItems(operationName: String) {
                            val dataRequest = "{\"items\":[]}".toNode()
                            val cm = commandMessage(operationName = operationName, data = dataRequest)

                            val dataEntity = tenderProcessEntityData(hasItems = hasItems)
                            mockGetByCpIdAndStage(
                                cpid = ContextGenerator.CPID,
                                stage = ContextGenerator.STAGE,
                                data = dataEntity
                            )

                            val exception = assertThrows<ErrorException> {
                                strategy.check(cm)
                            }
                            assertEquals(ErrorType.EMPTY_ITEMS, exception.error)
                        }

                        @ParameterizedTest
                        @CsvSource(
                            value = [
                                "updatePN"
                            ]
                        )
                        fun requestWithNotConsistentCPVCode(operationName: String) {
                            val dataRequest = requestWithNotConsistentCPVCodes()
                            val cm = commandMessage(operationName = operationName, data = dataRequest)

                            val dataEntity = tenderProcessEntityData(hasItems = hasItems)
                            mockGetByCpIdAndStage(
                                cpid = ContextGenerator.CPID,
                                stage = ContextGenerator.STAGE,
                                data = dataEntity
                            )

                            val exception = assertThrows<ErrorException> {
                                strategy.check(cm)
                            }
                            assertEquals(ErrorType.ITEMS_CPV_CODES_NOT_CONSISTENT, exception.error)
                        }
                    }
                }

                @Nested
                inner class Items {
                    private val hasItems = true

                    @ParameterizedTest
                    @CsvSource(
                        value = [
                            "updatePN"
                        ]
                    )
                    fun success(operationName: String) {
                        val dataRequest = request()
                        val cm = commandMessage(operationName = operationName, data = dataRequest)

                        val dataEntity = tenderProcessEntityData(hasItems = hasItems)
                        mockGetByCpIdAndStage(
                            cpid = ContextGenerator.CPID,
                            stage = ContextGenerator.STAGE,
                            data = dataEntity
                        )

                        val actual = strategy.check(cm).toJson()
                        val expected = response(mdmValidation = true, itemsAdd = false)

                        JsonValidator.equalsJsons(expectedJson = expected, actualJson = actual)
                    }
                }
            }

            @ParameterizedTest
            @CsvSource(
                value = [
                    "updateCN",
                    "createCNonPIN"
                ]
            )
            fun undefinedOperation(operationName: String) {
                val dataRequest = request()
                val cm = commandMessage(operationName = operationName, data = dataRequest)

                val actual = strategy.check(cm).toJson()
                val expected = response(mdmValidation = false, itemsAdd = false)

                JsonValidator.equalsJsons(expectedJson = expected, actualJson = actual)
            }
        }

        private fun mockGetByCpIdAndStage(cpid: String, stage: String, data: JSON) {
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = data)
            whenever(tenderProcessDao.getByCpIdAndStage(eq(cpid), eq(stage)))
                .thenReturn(tenderProcessEntity)
        }
    }

    private fun commandMessage(operationName: String, data: JsonNode): CommandMessage {
        val context = ContextGenerator.generate(
            operationType = operationName
        )
        return CommandMessageGenerator.generate(
            command = CommandType.CHECK_ITEMS,
            context = context,
            data = data
        )
    }

    private fun request(vararg ids: String): JsonNode {
        val items = ids.map { id ->
            CheckItemsRequest.Item(
                classification = CheckItemsRequest.Item.Classification(
                    id = id
                ),
                id = "id-${id}",
                relatedLot = "relatedLot-${id}"
            )
        }

        return CheckItemsRequest(items = items)
            .toJson()
            .toNode()
    }

    private fun request(): JsonNode {
        return request(
            "${REQUEST_ITEM_COMMON_PARTY_CPV_CODE}123-1",
            "${REQUEST_ITEM_COMMON_PARTY_CPV_CODE}456-1",
            "${REQUEST_ITEM_COMMON_PARTY_CPV_CODE}789-1",
            "${REQUEST_ITEM_COMMON_PARTY_CPV_CODE}789-1",
            "${REQUEST_ITEM_COMMON_PARTY_CPV_CODE}012-1"
        )
    }

    private fun requestWithNotConsistentCPVCodes(): JsonNode {
        return request().apply {
            getArray("items")
                .getObject(0)
                .getObject("classification") {
                    val id = getString("id").textValue().let {
                        it.substring(0, 3).reversed() + it.substring(3)
                    }

                    setAttribute("id", id)
                }
        }
    }

    private fun tenderProcessEntityData(hasItems: Boolean, id: String = PN_TENDER_CPV_CODE): JSON {
        val json = if (hasItems)
            loadJson("json/service/check/items/entity/pn/entity_pn_with_items.json")
        else
            loadJson("json/service/check/items/entity/pn/entity_pn_without_items.json")

        return if (!hasItems) {
            json.toNode()
                .apply {
                    getObject("tender", "classification")
                        .setAttribute("id", id)
                }
                .toJson()
        } else
            json
    }

    private fun response(
        mdmValidation: Boolean,
        itemsAdd: Boolean,
        id: String? = null,
        mainProcurementCategory: MainProcurementCategory? = null,
        items: List<CheckItemsResponse.Item>? = emptyList()
    ): JSON {
        return CheckItemsResponse(
            mdmValidation = mdmValidation,
            itemsAdd = itemsAdd,
            tender = id?.let {
                CheckItemsResponse.Tender(
                    classification = CheckItemsResponse.Tender.Classification(
                        id = id
                    )
                )
            },
            mainProcurementCategory = mainProcurementCategory,
            items = items
        ).toJson()
    }
}
