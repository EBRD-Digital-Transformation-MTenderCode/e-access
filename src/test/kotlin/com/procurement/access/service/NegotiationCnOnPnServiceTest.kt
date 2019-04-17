package com.procurement.access.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.dto.cn.NegotiationCnOnPnRequest
import com.procurement.access.infrastructure.dto.cn.NegotiationCnOnPnResponse
import com.procurement.access.infrastructure.entity.PNEntity
import com.procurement.access.infrastructure.generator.TestDataGenerator
import com.procurement.access.json.JsonFilePathGenerator
import com.procurement.access.json.JsonValidator
import com.procurement.access.json.deepCopy
import com.procurement.access.json.getArray
import com.procurement.access.json.getObject
import com.procurement.access.json.getString
import com.procurement.access.json.loadJson
import com.procurement.access.json.putAttribute
import com.procurement.access.json.putObject
import com.procurement.access.json.setAttribute
import com.procurement.access.json.testingBindingAndMapping
import com.procurement.access.json.toJson
import com.procurement.access.json.toNode
import com.procurement.access.model.dto.bpe.CommandMessage
import com.procurement.access.model.dto.bpe.CommandType
import com.procurement.access.model.dto.databinding.JsonDateTimeFormatter
import com.procurement.access.model.dto.ocds.Operation
import com.procurement.access.model.dto.ocds.ProcurementMethod
import com.procurement.access.model.dto.ocds.TenderStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

class NegotiationCnOnPnServiceTest {

    companion object {
        private const val PATH_PN_JSON =
            "json/service/create/cn_on_pn/entity/pn/entity_pn_with_items_with_documents.json"

        private const val LOT_STATUS = "active"
        private const val LOT_STATUS_DETAILS = "empty"

        private const val PERMANENT_LOT_ID_1 = "permanent-lot-1"
        private const val PERMANENT_LOT_ID_2 = "permanent-lot-2"

        private const val PERMANENT_ITEM_ID_1 = "permanent-item-1"
        private const val PERMANENT_ITEM_ID_2 = "permanent-item-2"
        private const val PERMANENT_ITEM_ID_3 = "permanent-item-3"
        private const val PERMANENT_ITEM_ID_4 = "permanent-item-4"
    }

    private lateinit var generationService: GenerationService
    private lateinit var tenderProcessDao: TenderProcessDao

    private lateinit var service: NegotiationCnOnPnService

    @BeforeEach
    fun init() {
        generationService = mock()
        tenderProcessDao = mock()

        service = NegotiationCnOnPnService(generationService, tenderProcessDao)
    }

    @DisplayName("Check Endpoint")
    @Nested
    inner class Check {

        @DisplayName("Check pmd in command.")
        @Test
        fun checkPMD() {
            val cm = commandMessage(
                pmd = "UNKNOWN",
                data = NullNode.instance
            )
            val exception = assertThrows<ErrorException> {
                service.checkNegotiationCnOnPn(cm)
            }

            assertEquals(ErrorType.INVALID_PMD, exception.error)
        }

        @DisplayName("Check error when tender by cpid and prev stage not found.")
        @Test
        fun tenderNotFound() {
            val data =
                loadJson("json/service/create/cn_on_pn/request/op/request_cn_with_auctions_on_pn.json").toNode()

            whenever(
                tenderProcessDao.getByCpIdAndStage(
                    eq(TestDataGenerator.CPID),
                    eq(TestDataGenerator.PREV_STAGE)
                )
            )
                .thenReturn(null)

            val cm = commandMessage(data = data)
            val exception = assertThrows<ErrorException> {
                service.checkNegotiationCnOnPn(cm)
            }

            assertEquals(ErrorType.DATA_NOT_FOUND, exception.error)
        }

        private val PATH_REQUEST_LP_JSON =
            "json/service/create/cn_on_pn/request/lp/request_cn_on_pn.json"

        @Nested
        inner class PNWithItems {
            private lateinit var requestNode: ObjectNode
            private lateinit var pnWithItems: ObjectNode

            @BeforeEach
            fun prepare() {
                requestNode = loadJson(PATH_REQUEST_LP_JSON).toNode() as ObjectNode
                pnWithItems = loadJson(PATH_PN_JSON).toNode() as ObjectNode
            }

            @Test
            fun success() {
                val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithItems.toString())
                whenever(
                    tenderProcessDao.getByCpIdAndStage(
                        eq(TestDataGenerator.CPID),
                        eq(TestDataGenerator.PREV_STAGE)
                    )
                )
                    .thenReturn(tenderProcessEntity)

                val cm = commandMessage(data = requestNode)
                val response = service.checkNegotiationCnOnPn(cm)
                assertEquals("ok", response.data)
            }

            @DisplayName("VR-3.8.1")
            @Test
            fun vr3_8_01() {
                val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithItems.toString())
                whenever(
                    tenderProcessDao.getByCpIdAndStage(
                        eq(TestDataGenerator.CPID),
                        eq(TestDataGenerator.PREV_STAGE)
                    )
                )
                    .thenReturn(tenderProcessEntity)

                val cm = commandMessage(token = "UNKNOWN", data = requestNode)
                val exception = assertThrows<ErrorException> {
                    service.checkNegotiationCnOnPn(cm)
                }

                assertEquals(ErrorType.INVALID_TOKEN, exception.error)
            }

            @DisplayName("VR-3.8.2")
            @Test
            fun vr3_8_02() {
                val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithItems.toString())
                whenever(
                    tenderProcessDao.getByCpIdAndStage(
                        eq(TestDataGenerator.CPID),
                        eq(TestDataGenerator.PREV_STAGE)
                    )
                )
                    .thenReturn(tenderProcessEntity)

                val cm = commandMessage(owner = "UNKNOWN", data = requestNode)
                val exception = assertThrows<ErrorException> {
                    service.checkNegotiationCnOnPn(cm)
                }

                assertEquals(ErrorType.INVALID_OWNER, exception.error)
            }

            @Nested
            @DisplayName("VR-3.8.3(CN on PN)")
            inner class VR3_8_03 {

                @DisplayName("VR-3.8.3(CN on PN) -> VR-3.6.1(CNEntity)")
                @Test
                fun vr3_8_03_vr_3_6_1() {
                    requestNode.getObject("tender")
                        .getArray("documents") {
                            val copyDocument = getObject(0).deepCopy()
                            putObject(copyDocument)
                        }
                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID),
                            eq(TestDataGenerator.PREV_STAGE)
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = commandMessage(data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkNegotiationCnOnPn(cm)
                    }

                    assertEquals(ErrorType.INVALID_DOCS_ID, exception.error)
                }

                @DisplayName("VR-3.8.3(CN on PN) -> VR-3.7.3(CNEntity)")
                @Test
                fun vr3_8_03_vr_3_7_3() {
                    requestNode.getObject("tender")
                        .getArray("documents") {
                            remove(0)
                        }
                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID),
                            eq(TestDataGenerator.PREV_STAGE)
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = commandMessage(data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkNegotiationCnOnPn(cm)
                    }

                    assertEquals(ErrorType.INVALID_DOCS_ID, exception.error)
                }
            }

            @DisplayName("VR-3.8.16(CN on PN)")
            @Test
            fun vr3_8_16() {
                val minStartDateOfContractPeriod: LocalDateTime =
                    pnWithItems.getObject("tender").getArray("lots").let { lots ->
                        lots.asSequence()
                            .map {
                                it.getObject("contractPeriod").getString("startDate").asText()
                            }
                            .map {
                                LocalDateTime.parse(it, JsonDateTimeFormatter.formatter)
                            }
                            .min()!!
                    }

                val startDate = minStartDateOfContractPeriod.plusDays(1).format(JsonDateTimeFormatter.formatter)

                val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithItems.toString())
                whenever(
                    tenderProcessDao.getByCpIdAndStage(
                        eq(TestDataGenerator.CPID),
                        eq(TestDataGenerator.PREV_STAGE)
                    )
                )
                    .thenReturn(tenderProcessEntity)

                val cm = commandMessage(startDate = startDate, data = requestNode)
                val exception = assertThrows<ErrorException> {
                    service.checkNegotiationCnOnPn(cm)
                }

                assertEquals(ErrorType.INVALID_LOT_CONTRACT_PERIOD, exception.error)
            }

            @DisplayName("VR-3.8.17(CN on PN)")
            @Test
            fun vr3_8_17() {
                requestNode.getObject("tender")
                    .getArray("documents")
                    .getObject(0)
                    .getArray("relatedLots")
                    .add("UNKNOWN")

                val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithItems.toString())
                whenever(
                    tenderProcessDao.getByCpIdAndStage(
                        eq(TestDataGenerator.CPID),
                        eq(TestDataGenerator.PREV_STAGE)
                    )
                )
                    .thenReturn(tenderProcessEntity)

                val cm = commandMessage(data = requestNode)
                val exception = assertThrows<ErrorException> {
                    service.checkNegotiationCnOnPn(cm)
                }

                assertEquals(ErrorType.INVALID_DOCS_RELATED_LOTS, exception.error)
            }

            @DisplayName("VR-3.8.18 Check error when tender is unsuccessful status.")
            @Test
            fun vr_3_8_18() {
                pnWithItems.getObject("tender")
                    .setAttribute("status", TenderStatus.UNSUCCESSFUL.value)

                val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithItems.toString())
                whenever(
                    tenderProcessDao.getByCpIdAndStage(
                        eq(TestDataGenerator.CPID),
                        eq(TestDataGenerator.PREV_STAGE)
                    )
                )
                    .thenReturn(tenderProcessEntity)

                val cm = commandMessage(data = requestNode)
                val exception = assertThrows<ErrorException> {
                    service.checkNegotiationCnOnPn(cm)
                }

                assertEquals(ErrorType.TENDER_IN_UNSUCCESSFUL_STATUS, exception.error)
            }
        }

        @Nested
        inner class PNWithoutItems {
            private lateinit var requestNode: ObjectNode
            private lateinit var pnWithoutItems: ObjectNode

            @BeforeEach
            fun prepare() {
                requestNode = loadJson(PATH_REQUEST_LP_JSON).toNode() as ObjectNode
                pnWithoutItems = (loadJson(PATH_PN_JSON).toNode() as ObjectNode).apply {
                    getObject("tender") {
                        putArray("lots")
                        putArray("items")
                    }
                }
            }

            @Test
            fun success() {
                val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                whenever(
                    tenderProcessDao.getByCpIdAndStage(
                        eq(TestDataGenerator.CPID),
                        eq(TestDataGenerator.PREV_STAGE)
                    )
                )
                    .thenReturn(tenderProcessEntity)

                val cm = commandMessage(data = requestNode)
                val response = service.checkNegotiationCnOnPn(cm)
                assertEquals("ok", response.data)
            }

            @DisplayName("VR-3.8.1")
            @Test
            fun vr3_8_01() {
                val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                whenever(
                    tenderProcessDao.getByCpIdAndStage(
                        eq(TestDataGenerator.CPID),
                        eq(TestDataGenerator.PREV_STAGE)
                    )
                )
                    .thenReturn(tenderProcessEntity)

                val cm = commandMessage(token = "UNKNOWN", data = requestNode)
                val exception = assertThrows<ErrorException> {
                    service.checkNegotiationCnOnPn(cm)
                }

                assertEquals(ErrorType.INVALID_TOKEN, exception.error)
            }

            @DisplayName("VR-3.8.2")
            @Test
            fun vr3_8_02() {
                val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                whenever(
                    tenderProcessDao.getByCpIdAndStage(
                        eq(TestDataGenerator.CPID),
                        eq(TestDataGenerator.PREV_STAGE)
                    )
                )
                    .thenReturn(tenderProcessEntity)

                val cm = commandMessage(owner = "UNKNOWN", data = requestNode)
                val exception = assertThrows<ErrorException> {
                    service.checkNegotiationCnOnPn(cm)
                }

                assertEquals(ErrorType.INVALID_OWNER, exception.error)
            }

            @Nested
            @DisplayName("VR-3.8.3(CN on PN)")
            inner class VR3_8_3 {

                @DisplayName("VR-3.8.3(CN on PN) -> VR-3.6.1(CNEntity)")
                @Test
                fun vr3_8_3_vr_3_6_1() {
                    requestNode.getObject("tender")
                        .getArray("documents") {
                            val copyDocument = getObject(0).deepCopy()
                            putObject(copyDocument)
                        }
                    val tenderProcessEntity =
                        TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID),
                            eq(TestDataGenerator.PREV_STAGE)
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = commandMessage(data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkNegotiationCnOnPn(cm)
                    }

                    assertEquals(ErrorType.INVALID_DOCS_ID, exception.error)
                }

                @DisplayName("VR-3.8.3(CN on PN) -> VR-3.7.3(CNEntity)")
                @Test
                fun vr3_8_3_vr_3_7_3() {
                    requestNode.getObject("tender")
                        .getArray("documents") {
                            remove(0)
                        }
                    val tenderProcessEntity =
                        TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID),
                            eq(TestDataGenerator.PREV_STAGE)
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = commandMessage(data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkNegotiationCnOnPn(cm)
                    }

                    assertEquals(ErrorType.INVALID_DOCS_ID, exception.error)
                }
            }

            @DisplayName("VR-3.8.4(CN on PN)")
            @Test
            fun vr3_8_04() {
                pnWithoutItems.getObject("planning", "budget", "amount")
                    .setAttribute("amount", BigDecimal(1))
                val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                whenever(
                    tenderProcessDao.getByCpIdAndStage(
                        eq(TestDataGenerator.CPID),
                        eq(TestDataGenerator.PREV_STAGE)
                    )
                )
                    .thenReturn(tenderProcessEntity)

                val cm = commandMessage(data = requestNode)
                val exception = assertThrows<ErrorException> {
                    service.checkNegotiationCnOnPn(cm)
                }

                assertEquals(ErrorType.INVALID_TENDER_AMOUNT, exception.error)
            }

            @DisplayName("VR-3.8.5(CN on PN)")
            @Test
            fun vr3_8_05() {
                requestNode.getObject("tender").getArray("lots") {
                    getObject(0) {
                        getObject("value")
                            .setAttribute("currency", "UNKNOWN")
                    }
                }

                val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                whenever(
                    tenderProcessDao.getByCpIdAndStage(
                        eq(TestDataGenerator.CPID),
                        eq(TestDataGenerator.PREV_STAGE)
                    )
                )
                    .thenReturn(tenderProcessEntity)

                val cm = commandMessage(data = requestNode)
                val exception = assertThrows<ErrorException> {
                    service.checkNegotiationCnOnPn(cm)
                }

                assertEquals(ErrorType.INVALID_LOT_CURRENCY, exception.error)
            }

            @Nested
            @DisplayName("VR-3.8.6(CN on PN)")
            inner class VR3_8_6 {

                @DisplayName("check start date")
                @Test
                fun startDate() {
                    val minStartDateOfContractPeriod: LocalDateTime =
                        requestNode.getObject("tender").getArray("lots").let { lots ->
                            lots.asSequence()
                                .map {
                                    it.getObject("contractPeriod").getString("startDate").asText()
                                }
                                .map {
                                    LocalDateTime.parse(it, JsonDateTimeFormatter.formatter)
                                }
                                .min()!!
                        }
                    val budgetBreakdownPeriodEndDate = minStartDateOfContractPeriod.minusDays(1)

                    pnWithoutItems.getObject("planning", "budget")
                        .getArray("budgetBreakdown")
                        .getObject(0)
                        .getObject("period")
                        .putAttribute("endDate", budgetBreakdownPeriodEndDate.format(JsonDateTimeFormatter.formatter))

                    val tenderProcessEntity =
                        TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID),
                            eq(TestDataGenerator.PREV_STAGE)
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = commandMessage(data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkNegotiationCnOnPn(cm)
                    }

                    assertEquals(ErrorType.INVALID_LOT_CONTRACT_PERIOD, exception.error)
                }

                @DisplayName("check end date")
                @Test
                fun endDate() {
                    val maxStartDateOfContractPeriod: LocalDateTime =
                        requestNode.getObject("tender").getArray("lots").let { lots ->
                            lots.asSequence()
                                .map {
                                    it.getObject("contractPeriod").getString("endDate").asText()
                                }
                                .map {
                                    LocalDateTime.parse(it, JsonDateTimeFormatter.formatter)
                                }
                                .max()!!
                        }

                    val budgetBreakdownPeriodStartDate = maxStartDateOfContractPeriod.plusDays(1)
                    val budgetBreakdownPeriodEndDate = maxStartDateOfContractPeriod.plusDays(10)
                    pnWithoutItems.getObject("planning", "budget")
                        .getArray("budgetBreakdown")
                        .getObject(0)
                        .getObject("period") {
                            putAttribute(
                                "startDate",
                                budgetBreakdownPeriodStartDate.format(JsonDateTimeFormatter.formatter)
                            )
                            putAttribute(
                                "endDate",
                                budgetBreakdownPeriodEndDate.plusYears(1).format(JsonDateTimeFormatter.formatter)
                            )
                        }

                    val tenderProcessEntity =
                        TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID),
                            eq(TestDataGenerator.PREV_STAGE)
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = commandMessage(data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkNegotiationCnOnPn(cm)
                    }

                    assertEquals(ErrorType.INVALID_LOT_CONTRACT_PERIOD, exception.error)
                }
            }

            @DisplayName("VR-3.8.7(CN on PN)")
            @Test
            fun vr3_8_07() {
                requestNode.getObject("tender").getArray("documents") {
                    getObject(0) {
                        getArray("relatedLots")
                            .add("UNKNOWN")
                    }
                }

                val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                whenever(
                    tenderProcessDao.getByCpIdAndStage(
                        eq(TestDataGenerator.CPID),
                        eq(TestDataGenerator.PREV_STAGE)
                    )
                )
                    .thenReturn(tenderProcessEntity)

                val cm = commandMessage(data = requestNode)
                val exception = assertThrows<ErrorException> {
                    service.checkNegotiationCnOnPn(cm)
                }

                assertEquals(ErrorType.INVALID_DOCS_RELATED_LOTS, exception.error)
            }

            @Nested
            @DisplayName("VR-3.8.8(CN on PN)")
            inner class VR3_8_8 {

                @DisplayName("Check contract period of a lot")
                @Test
                fun vr3_8_8_1() {
                    requestNode.getObject("tender").getArray("lots") {
                        getObject(0) {
                            val contractPeriod = getObject("contractPeriod")
                            val startDate = contractPeriod.getString("startDate").asText()
                            val endDate = contractPeriod.getString("endDate").asText()

                            contractPeriod.setAttribute("startDate", endDate)
                            contractPeriod.setAttribute("endDate", startDate)
                        }
                    }

                    val tenderProcessEntity =
                        TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID),
                            eq(TestDataGenerator.PREV_STAGE)
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = commandMessage(data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkNegotiationCnOnPn(cm)
                    }

                    assertEquals(ErrorType.INVALID_LOT_CONTRACT_PERIOD, exception.error)
                }

                @DisplayName("Check contract period of a lot")
                @Test
                fun vr3_8_8_2() {
                    val startDate = requestNode.getObject("tender")
                        .getArray("lots").getObject(0)
                        .getObject("contractPeriod")
                        .getString("startDate").asText()

                    val tenderProcessEntity =
                        TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID),
                            eq(TestDataGenerator.PREV_STAGE)
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = commandMessage(startDate = startDate, data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkNegotiationCnOnPn(cm)
                    }

                    assertEquals(ErrorType.INVALID_LOT_CONTRACT_PERIOD, exception.error)
                }
            }

            @DisplayName("VR-3.8.9(CN on PN)")
            @Test
            fun vr3_8_09() {
                requestNode.getObject("tender").getArray("items") {
                    getObject(0) {
                        setAttribute("quantity", 0)
                    }
                }

                val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                whenever(
                    tenderProcessDao.getByCpIdAndStage(
                        eq(TestDataGenerator.CPID),
                        eq(TestDataGenerator.PREV_STAGE)
                    )
                )
                    .thenReturn(tenderProcessEntity)

                val cm = commandMessage(data = requestNode)
                val exception = assertThrows<ErrorException> {
                    service.checkNegotiationCnOnPn(cm)
                }

                assertEquals(ErrorType.INVALID_ITEMS_QUANTITY, exception.error)
            }

            @Nested
            @DisplayName("VR-3.8.10(CN on PN)")
            inner class VR3_8_10 {

                @DisplayName("Checks the quantity of Lot object in Request")
                @Test
                fun vr3_8_10_1() {
                    requestNode.getObject("tender").putArray("lots")

                    val tenderProcessEntity =
                        TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID),
                            eq(TestDataGenerator.PREV_STAGE)
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = commandMessage(data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkNegotiationCnOnPn(cm)
                    }

                    assertEquals(ErrorType.EMPTY_LOTS, exception.error)
                }

                @DisplayName("Analyzes Lot.ID")
                @Test
                fun vr3_8_10_2() {
                    val lots = requestNode.getObject("tender")
                        .getArray("lots")
                    val newLot = lots.getObject(0).deepCopy {
                        putAttribute("id", "new-lot-id")
                    }
                    lots.add(newLot)

                    val tenderProcessEntity =
                        TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID),
                            eq(TestDataGenerator.PREV_STAGE)
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = commandMessage(data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkNegotiationCnOnPn(cm)
                    }

                    assertEquals(ErrorType.LOT_ID_NOT_MATCH_TO_RELATED_LOT_IN_ITEMS, exception.error)
                }
            }

            @DisplayName("VR-3.8.11(CN on PN)")
            @Test
            fun vr3_8_11() {
                requestNode.getObject("tender")
                    .getArray("items")
                    .getObject(0)
                    .putAttribute("relatedLot", UUID.randomUUID().toString())

                val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                whenever(
                    tenderProcessDao.getByCpIdAndStage(
                        eq(TestDataGenerator.CPID),
                        eq(TestDataGenerator.PREV_STAGE)
                    )
                )
                    .thenReturn(tenderProcessEntity)

                val cm = commandMessage(data = requestNode)
                val exception = assertThrows<ErrorException> {
                    service.checkNegotiationCnOnPn(cm)
                }

                assertEquals(ErrorType.INVALID_ITEMS_RELATED_LOTS, exception.error)
            }

            @DisplayName("VR-3.8.12(CN on PN)")
            @Test
            fun vr3_8_12() {
                val lots = requestNode.getObject("tender").getArray("lots")
                val duplicate = lots.getObject(0).deepCopy()
                lots.putObject(duplicate)

                val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                whenever(
                    tenderProcessDao.getByCpIdAndStage(
                        eq(TestDataGenerator.CPID),
                        eq(TestDataGenerator.PREV_STAGE)
                    )
                )
                    .thenReturn(tenderProcessEntity)

                val cm = commandMessage(data = requestNode)
                val exception = assertThrows<ErrorException> {
                    service.checkNegotiationCnOnPn(cm)
                }

                assertEquals(ErrorType.LOT_ID_DUPLICATED, exception.error)
            }

            @DisplayName("VR-3.8.13(CN on PN)")
            @Test
            fun vr3_8_13() {
                val items = requestNode.getObject("tender").getArray("items")
                val duplicate = items.getObject(0).deepCopy()
                items.putObject(duplicate)

                val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                whenever(
                    tenderProcessDao.getByCpIdAndStage(
                        eq(TestDataGenerator.CPID),
                        eq(TestDataGenerator.PREV_STAGE)
                    )
                )
                    .thenReturn(tenderProcessEntity)

                val cm = commandMessage(data = requestNode)
                val exception = assertThrows<ErrorException> {
                    service.checkNegotiationCnOnPn(cm)
                }

                assertEquals(ErrorType.ITEM_ID_IS_DUPLICATED, exception.error)
            }

            @DisplayName("VR-3.8.18 Check error when tender is unsuccessful status.")
            @Test
            fun vr_3_8_18() {
                pnWithoutItems.getObject("tender")
                    .setAttribute("status", TenderStatus.UNSUCCESSFUL.value)

                val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                whenever(
                    tenderProcessDao.getByCpIdAndStage(
                        eq(TestDataGenerator.CPID),
                        eq(TestDataGenerator.PREV_STAGE)
                    )
                )
                    .thenReturn(tenderProcessEntity)

                val cm = commandMessage(data = requestNode)
                val exception = assertThrows<ErrorException> {
                    service.checkNegotiationCnOnPn(cm)
                }

                assertEquals(ErrorType.TENDER_IN_UNSUCCESSFUL_STATUS, exception.error)
            }
        }
    }

    @DisplayName("Create Endpoint")
    @Nested
    inner class Create {

        @DisplayName("Check pmd in command.")
        @Test
        fun checkPMD() {
            val cm = commandMessage(pmd = "UNKNOWN", data = NullNode.instance)
            val exception = assertThrows<ErrorException> {
                service.checkNegotiationCnOnPn(cm)
            }

            assertEquals(ErrorType.INVALID_PMD, exception.error)
        }

        @DisplayName("Check error when tender by cpid and prev stage not found.")
        @Test
        fun tenderNotFound() {
            val data =
                loadJson("json/service/create/cn_on_pn/request/op/request_cn_with_auctions_on_pn.json").toNode()

            whenever(
                tenderProcessDao.getByCpIdAndStage(
                    eq(TestDataGenerator.CPID),
                    eq(TestDataGenerator.PREV_STAGE)
                )
            )
                .thenReturn(null)

            val cm = commandMessage(data = data)
            val exception = assertThrows<ErrorException> {
                service.checkNegotiationCnOnPn(cm)
            }

            assertEquals(ErrorType.DATA_NOT_FOUND, exception.error)
        }

        private val pmd: ProcurementMethod = ProcurementMethod.DA

        @Nested
        inner class PNWithItems {
            private val hasItemsInPN = true

            @Nested
            inner class WithDocuments {
                private val hasDocumentsInPN = true

                @Test
                fun test() {
                    val testData = WhenTestData(
                        hasItemsInPN = hasItemsInPN,
                        hasDocumentsInPN = hasDocumentsInPN
                    )
                    testOfCreate(testData)
                }
            }

            @Nested
            inner class WithoutDocuments {
                private val hasDocumentsInPN = false

                @Test
                fun test() {
                    val testData = WhenTestData(
                        hasItemsInPN = hasItemsInPN,
                        hasDocumentsInPN = hasDocumentsInPN
                    )
                    testOfCreate(testData)
                }
            }
        }

        @Nested
        inner class PNWithoutItems {
            private val hasItemsInPN = false

            @Nested
            inner class WithDocuments {
                private val hasDocumentsInPN = true

                @Test
                fun test() {
                    val testData = WhenTestData(
                        hasItemsInPN = hasItemsInPN,
                        hasDocumentsInPN = hasDocumentsInPN
                    )
                    testOfCreate(testData)
                }
            }

            @Nested
            inner class WithoutDocuments {
                private val hasDocumentsInPN = false

                @Test
                fun test() {
                    val testData = WhenTestData(
                        hasItemsInPN = hasItemsInPN,
                        hasDocumentsInPN = hasDocumentsInPN
                    )
                    testOfCreate(testData)
                }
            }
        }
    }

    private fun testOfCreate(testData: WhenTestData) {
        val pathToJsonFileOfRequest = testData.requestJsonFile().also {
            testingBindingAndMapping<NegotiationCnOnPnRequest>(it)
        }
        val pathToJsonFileOfPNEntity = testData.pnJsonFile().also {
            testingBindingAndMapping<PNEntity>(it)
        }
        val pathToJsonFileOfResponse = testData.responseJsonFile().also {
            testingBindingAndMapping<NegotiationCnOnPnResponse>(it)
        }

        val data = loadJson(pathToJsonFileOfRequest).toNode()
        val cm = commandMessage(command = CommandType.CREATE_CN_ON_PN, data = data)

        val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = loadJson(pathToJsonFileOfPNEntity))

        whenever(generationService.generatePermanentLotId())
            .thenReturn(PERMANENT_LOT_ID_1, PERMANENT_LOT_ID_2)
        whenever(generationService.generatePermanentItemId())
            .thenReturn(PERMANENT_ITEM_ID_1, PERMANENT_ITEM_ID_2, PERMANENT_ITEM_ID_3, PERMANENT_ITEM_ID_4)
        whenever(
            tenderProcessDao.getByCpIdAndStage(
                eq(TestDataGenerator.CPID),
                eq(TestDataGenerator.PREV_STAGE)
            )
        )
            .thenReturn(tenderProcessEntity)

        val actualJson = service.createNegotiationCnOnPn(cm).data!!.toJson()

        val expectedJson = loadJson(pathToJsonFileOfResponse)

        JsonValidator.equalsJsons(expectedJson, actualJson) {
            assert("$['tender']['lots'][0]['status']", LOT_STATUS)
            assert("$['tender']['lots'][1]['status']", LOT_STATUS)
            assert("$['tender']['lots'][0]['statusDetails']", LOT_STATUS_DETAILS)
            assert("$['tender']['lots'][1]['statusDetails']", LOT_STATUS_DETAILS)
        }
    }

    fun commandMessage(
        pmd: String = ProcurementMethod.DA.name,
        token: String = TestDataGenerator.TOKEN.toString(),
        owner: String = TestDataGenerator.OWNER,
        command: CommandType = CommandType.CHECK_CN_ON_PN,
        operationType: Operation = Operation.CREATE_CN_ON_PN,
        startDate: String = TestDataGenerator.START_DATE,
        data: JsonNode
    ): CommandMessage {
        return TestDataGenerator.commandMessage(
            pmd = pmd,
            token = token,
            owner = owner,
            command = command,
            startDate = startDate,
            operationType = operationType,
            data = data
        )
    }

    class WhenTestData(val hasItemsInPN: Boolean, val hasDocumentsInPN: Boolean) {

        fun requestJsonFile(): String {
            return "json/service/create/cn_on_pn/request/lp/request_cn_on_pn.json"
        }

        fun pnJsonFile(): String {
            val itemsSegment = JsonFilePathGenerator.itemsSegment(hasItemsInPN)
            val segmentDocuments = JsonFilePathGenerator.documentsSegment(hasDocumentsInPN)
            return "json/service/create/cn_on_pn/entity/pn/entity_pn_${itemsSegment}_${segmentDocuments}.json"
        }

        fun responseJsonFile(): String {
            val itemsSegment = JsonFilePathGenerator.itemsSegment(hasItemsInPN)
            val segmentDocuments = JsonFilePathGenerator.documentsSegment(hasDocumentsInPN)
            return "json/service/create/cn_on_pn/response/lp/response_cn_on_pn_${itemsSegment}_${segmentDocuments}.json"
        }
    }
}

