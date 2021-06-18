package com.procurement.access.service.validation.strategy

import com.fasterxml.jackson.databind.JsonNode
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.enums.MainProcurementCategory
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.api.v1.CommandMessage
import com.procurement.access.infrastructure.api.v1.CommandTypeV1
import com.procurement.access.infrastructure.entity.PNEntity
import com.procurement.access.infrastructure.generator.CommandMessageGenerator
import com.procurement.access.infrastructure.generator.ContextGenerator
import com.procurement.access.infrastructure.generator.TenderProcessEntityGenerator
import com.procurement.access.infrastructure.handler.v1.model.request.CheckItemsRequest
import com.procurement.access.infrastructure.handler.v1.model.response.CheckItemsResponse
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
import com.procurement.access.utils.toObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class CheckItemsStrategyTest {
    companion object {
        private const val ITEM_ID_1 = "item-1"
        private const val UNKNOWN_ITEM_ID_1 = "unknown"
        private const val RELATED_LOT_1 = "lot-1"

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
                        mockGetByCpIdAndOcid(
                            cpid = ContextGenerator.CPID,
                            ocid = ContextGenerator.OCID,
                            data = dataEntity
                        )

                        val actual = strategy.check(cm).toJson()
                        val expectedItems = dataRequest.toObject<CheckItemsRequest>().items
                        val expected = response(
                            mdmValidation = true,
                            itemsAdd = true,
                            id = RESPONSE_TENDER_CPV_CODE,
                            mainProcurementCategory = MainProcurementCategory.SERVICES,
                            items = expectedItems.map {
                                CheckItemsResponse.Item(
                                    id = it.id,
                                    relatedLot = it.relatedLot
                                )
                            }
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
                            mockGetByCpIdAndOcid(
                                cpid = ContextGenerator.CPID,
                                ocid = ContextGenerator.OCID,
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

                            val dataEntity = tenderProcessEntityData(hasItems = hasItems, id ="non.consistent.id")
                            mockGetByCpIdAndOcid(
                                cpid = ContextGenerator.CPID,
                                ocid = ContextGenerator.OCID,
                                data = dataEntity
                            )

                            val exception = assertThrows<ErrorException> {
                                strategy.check(cm)
                            }
                            assertEquals(ErrorType.MISSING_HOMOGENEOUS_ITEMS, exception.error)
                        }

                    @ParameterizedTest
                    @CsvSource(
                        value = [
                            "createCNonPN",
                            "createPINonPN",
                            "createNegotiationCnOnPn"
                        ]
                    )
                    fun requestWithCPVCodesConsistentWithTenderClassification(operationName: String) {
                        val dataRequest = requestWithNotConsistentCPVCodes()
                        val cm = commandMessage(operationName = operationName, data = dataRequest)

                        val dataEntity = tenderProcessEntityData(hasItems = hasItems)
                        mockGetByCpIdAndOcid(
                            cpid = ContextGenerator.CPID,
                            ocid = ContextGenerator.OCID,
                            data = dataEntity
                        )

                        val actual = strategy.check(cm).toJson()
                        val expectedItems = dataRequest.toObject<CheckItemsRequest>().items
                        val expected = response(
                            mdmValidation = true,
                            itemsAdd = true,
                            id = RESPONSE_TENDER_CPV_CODE,
                            mainProcurementCategory = MainProcurementCategory.SERVICES,
                            items = expectedItems.map {
                                CheckItemsResponse.Item(
                                    id = it.id,
                                    relatedLot = it.relatedLot
                                )
                            }
                        )

                        JsonValidator.equalsJsons(expectedJson = expected, actualJson = actual)
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
                        mockGetByCpIdAndOcid(
                            cpid = ContextGenerator.CPID,
                            ocid = ContextGenerator.OCID,
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
                        mockGetByCpIdAndOcid(
                            cpid = ContextGenerator.CPID,
                            ocid = ContextGenerator.OCID,
                            data = dataEntity
                        )

                        val actual = strategy.check(cm).toJson()
                        val expectedItems = toObject(clazz = PNEntity::class.java, json = dataEntity).tender.items
                        val expected = response(
                            mdmValidation = false,
                            itemsAdd = true,
                            mainProcurementCategory = MainProcurementCategory.SERVICES,
                            items = expectedItems.map {
                                CheckItemsResponse.Item(
                                    id = it.id,
                                    relatedLot = it.relatedLot
                                )
                            }
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
                            tenderProcessDao.getByCpidAndOcid(
                                eq(ContextGenerator.CPID),
                                eq(ContextGenerator.OCID)
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
                            mockGetByCpIdAndOcid(
                                cpid = ContextGenerator.CPID,
                                ocid = ContextGenerator.OCID,
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
                            mockGetByCpIdAndOcid(
                                cpid = ContextGenerator.CPID,
                                ocid = ContextGenerator.OCID,
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
                        mockGetByCpIdAndOcid(
                            cpid = ContextGenerator.CPID,
                            ocid = ContextGenerator.OCID,
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

        @Nested
        inner class UpdateCN {
            @ParameterizedTest
            @CsvSource(
                value = [
                    "updateCN"
                ]
            )
            fun success(operationName: String) {
                val dataRequest = CheckItemsRequest(
                    items = listOf(
                        CheckItemsRequest.Item(
                            classification = CheckItemsRequest.Item.Classification(
                                id = REQUEST_ITEM_COMMON_PARTY_CPV_CODE + "123"
                            ),
                            id = ITEM_ID_1,
                            relatedLot = RELATED_LOT_1
                        )
                    )
                )

                val cm = commandMessage(operationName = operationName, data = dataRequest.toJson().toNode())

                val dataEntity = loadJson("json/service/check/items/entity/pn/entity_cn.json")
                mockGetByCpIdAndOcid(
                    cpid = ContextGenerator.CPID,
                    ocid = ContextGenerator.OCID,
                    data = dataEntity
                )

                val actual = strategy.check(cm).toJson()
                val expected = response(
                    mdmValidation = true,
                    itemsAdd = true,
                    mainProcurementCategory = MainProcurementCategory.SERVICES,
                    items = dataRequest.items.map { CheckItemsResponse.Item(id = it.id, relatedLot = it.relatedLot) }
                )
                JsonValidator.equalsJsons(expectedJson = expected, actualJson = actual)
            }

            @ParameterizedTest
            @CsvSource(
                value = [
                    "updateCN"
                ]
            )
            fun failure(operationName: String) {
                val dataRequest = CheckItemsRequest(
                    items = listOf(
                        CheckItemsRequest.Item(
                            classification = CheckItemsRequest.Item.Classification(
                                id = REQUEST_ITEM_COMMON_PARTY_CPV_CODE + "123"
                            ),
                            id = UNKNOWN_ITEM_ID_1,
                            relatedLot = RELATED_LOT_1
                        )
                    )
                )

                val cm = commandMessage(operationName = operationName, data = dataRequest.toJson().toNode())

                val dataEntity = loadJson("json/service/check/items/entity/pn/entity_cn.json")
                mockGetByCpIdAndOcid(
                    cpid = ContextGenerator.CPID,
                    ocid = ContextGenerator.OCID,
                    data = dataEntity
                )

                val exception = assertThrows<ErrorException> {
                    strategy.check(cm)
                }

                assertEquals("Invalid items id. Incorrect Items list.", exception.message)
            }
        }

        private fun mockGetByCpIdAndOcid(cpid: String, ocid: String, data: JSON) {
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = data)
            whenever(tenderProcessDao.getByCpidAndOcid(eq(cpid), eq(ocid)))
                .thenReturn(tenderProcessEntity)
        }
    }

    private fun commandMessage(operationName: String, data: JsonNode): CommandMessage {
        val context = ContextGenerator.generate(
            operationType = operationName
        )
        return CommandMessageGenerator.generate(
            command = CommandTypeV1.CHECK_ITEMS,
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

    private fun cnData(): JSON = loadJson("json/service/check/items/entity/pn/entity_pn_with_items.json")

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
