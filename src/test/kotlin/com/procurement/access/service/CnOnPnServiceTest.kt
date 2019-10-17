package com.procurement.access.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.clearInvocations
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.dto.cn.CnOnPnRequest
import com.procurement.access.infrastructure.dto.cn.CnOnPnResponse
import com.procurement.access.infrastructure.entity.PNEntity
import com.procurement.access.infrastructure.generator.CommandMessageGenerator
import com.procurement.access.infrastructure.generator.ContextGenerator
import com.procurement.access.infrastructure.generator.TenderProcessEntityGenerator
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

class CnOnPnServiceTest {

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
    private lateinit var rulesService: RulesService

    private lateinit var service: CnOnPnService

    @BeforeEach
    fun init() {
        generationService = mock()
        tenderProcessDao = mock()
        rulesService = mock()

        service = CnOnPnService(generationService, tenderProcessDao, rulesService)
    }

    @DisplayName("Check Endpoint")
    @Nested
    inner class Check {
        private val command = CommandType.CHECK_CN_ON_PN

        @DisplayName("Check pmd in command.")
        @Test
        fun checkPMD() {
            val cm = commandMessage(command = command, pmd = "UNKNOWN", data = NullNode.instance)
            val exception = assertThrows<ErrorException> {
                service.checkCnOnPn(cm)
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
                    eq(ContextGenerator.CPID),
                    eq(ContextGenerator.PREV_STAGE)
                )
            )
                .thenReturn(null)

            val cm = commandMessage(command = command, data = data)
            val exception = assertThrows<ErrorException> {
                service.checkCnOnPn(cm)
            }

            assertEquals(ErrorType.DATA_NOT_FOUND, exception.error)
        }

        private val PATH_REQUEST_OP_JSON =
            "json/service/create/cn_on_pn/request/op/request_cn_with_auctions_on_pn.json"

        @Nested
        inner class PNWithItems {
            private lateinit var requestNode: ObjectNode
            private lateinit var pnWithItems: ObjectNode

            @BeforeEach
            fun prepare() {
                requestNode = loadJson(PATH_REQUEST_OP_JSON).toNode() as ObjectNode
                pnWithItems = loadJson(PATH_PN_JSON).toNode() as ObjectNode
            }

            @Test
            fun success() {
                mockGetByCpIdAndStage(
                    cpid = ContextGenerator.CPID,
                    stage = ContextGenerator.PREV_STAGE,
                    data = pnWithItems
                )

                val cm = commandMessage(command = command, data = requestNode)
                val response = service.checkCnOnPn(cm)
                assertEquals("ok", response.data)
            }

            @DisplayName("VR-3.8.1(CNEntity on PNEntity)")
            @Test
            fun vr3_8_01() {
                mockGetByCpIdAndStage(
                    cpid = ContextGenerator.CPID,
                    stage = ContextGenerator.PREV_STAGE,
                    data = pnWithItems
                )

                val cm = commandMessage(command = command, token = "UNKNOWN", data = requestNode)
                val exception = assertThrows<ErrorException> {
                    service.checkCnOnPn(cm)
                }

                assertEquals(ErrorType.INVALID_TOKEN, exception.error)
            }

            @DisplayName("VR-3.8.2(CN on PN)")
            @Test
            fun vr3_8_02() {
                mockGetByCpIdAndStage(
                    cpid = ContextGenerator.CPID,
                    stage = ContextGenerator.PREV_STAGE,
                    data = pnWithItems
                )

                val cm = commandMessage(command = command, owner = "UNKNOWN", data = requestNode)
                val exception = assertThrows<ErrorException> {
                    service.checkCnOnPn(cm)
                }

                assertEquals(ErrorType.INVALID_OWNER, exception.error)
            }

            @Nested
            @DisplayName("VR-3.8.3(CN on PN)")
            inner class VR3_8_03 {
                @DisplayName("VR-3.8.3(CN on PN) -> VR-3.6.1(CNEntity)")
                @Test
                fun vr3_8_3_vr_3_6_1() {
                    requestNode.getObject("tender")
                        .getArray("documents") {
                            val copyDocument = getObject(0).deepCopy()
                            putObject(copyDocument)
                        }
                    mockGetByCpIdAndStage(
                        cpid = ContextGenerator.CPID,
                        stage = ContextGenerator.PREV_STAGE,
                        data = pnWithItems
                    )

                    val cm = commandMessage(command = command, data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
                    }

                    assertEquals(ErrorType.INVALID_DOCS_ID, exception.error)
                }

                @DisplayName("VR-3.8.3(CN on PN) -> VR-3.6.1(CNEntity) PN without documents")
                @Test
                fun vr3_8_03_vr_3_6_1_PN_without_documents() {
                    requestNode.getObject("tender")
                        .getArray("documents") {
                            val copyDocument = getObject(0).deepCopy()
                            putObject(copyDocument)
                        }
                    pnWithItems.getObject("tender")
                        .remove("documents")

                    mockGetByCpIdAndStage(
                        cpid = ContextGenerator.CPID,
                        stage = ContextGenerator.PREV_STAGE,
                        data = pnWithItems
                    )

                    val cm = commandMessage(command = command, data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
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
                    mockGetByCpIdAndStage(
                        cpid = ContextGenerator.CPID,
                        stage = ContextGenerator.PREV_STAGE,
                        data = pnWithItems
                    )

                    val cm = commandMessage(command = command, data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
                    }

                    assertEquals(ErrorType.INVALID_DOCS_ID, exception.error)
                }
            }

            @Nested
            @DisplayName("VR-3.8.15(CN on PN)")
            inner class VR3_8_15 {
                @DisplayName("Checks procurementMethodModalities in required auction.")
                @Test
                fun vr3_8_15_01() {
                    requestNode.getObject("tender")
                        .putArray("procurementMethodModalities")

                    whenever(rulesService.isAuctionRequired(any(), any(), any()))
                        .thenReturn(true)

                    mockGetByCpIdAndStage(
                        cpid = ContextGenerator.CPID,
                        stage = ContextGenerator.PREV_STAGE,
                        data = pnWithItems
                    )

                    val cm = commandMessage(command = command, data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
                    }

                    assertEquals(ErrorType.INVALID_PMM, exception.error)
                }

                @DisplayName("Checks electronicAuctions in required auction.")
                @Test
                fun vr3_8_15_02() {
                    requestNode.getObject("tender")
                        .remove("electronicAuctions")

                    whenever(rulesService.isAuctionRequired(any(), any(), any()))
                        .thenReturn(true)

                    mockGetByCpIdAndStage(
                        cpid = ContextGenerator.CPID,
                        stage = ContextGenerator.PREV_STAGE,
                        data = pnWithItems
                    )

                    val cm = commandMessage(command = command, data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
                    }

                    assertEquals(ErrorType.INVALID_AUCTION_IS_EMPTY, exception.error)
                }

                @DisplayName("3. Checks the uniqueness of all electronicAuctions.details.ID from Request")
                @Test
                fun vr3_8_15_3() {
                    val auctions = requestNode.getObject("tender", "electronicAuctions").getArray("details")
                    val duplicate = auctions.getObject(0).deepCopy()
                    auctions.putObject(duplicate)

                    mockGetByCpIdAndStage(
                        cpid = ContextGenerator.CPID,
                        stage = ContextGenerator.PREV_STAGE,
                        data = pnWithItems
                    )

                    val cm = commandMessage(command = command, data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
                    }

                    assertEquals(ErrorType.AUCTION_ID_DUPLICATED, exception.error)
                }

                @DisplayName("4. Checks the uniqueness of all electronicAuctions.details.relatedLot values from Request")
                @Test
                fun vr3_8_15_4() {
                    val auctions = requestNode.getObject("tender", "electronicAuctions").getArray("details")
                    val newAuction = auctions.getObject(0).deepCopy {
                        putAttribute("id", "new-auction-id")
                    }
                    auctions.putObject(newAuction)

                    mockGetByCpIdAndStage(
                        cpid = ContextGenerator.CPID,
                        stage = ContextGenerator.PREV_STAGE,
                        data = pnWithItems
                    )

                    val cm = commandMessage(command = command, data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
                    }

                    assertEquals(ErrorType.AUCTIONS_CONTAIN_DUPLICATE_RELATED_LOTS, exception.error)
                }

                @DisplayName("5. Compares Lots list from DB and electronicAuctions.details list from Request")
                @Test
                fun vr3_8_15_5() {
                    val auctions = requestNode.getObject("tender", "electronicAuctions").getArray("details")
                    val newAuction = auctions.getObject(0).deepCopy {
                        putAttribute("id", "new-auction-id")
                        putAttribute("relatedLot", "UNKNOWN")
                    }
                    auctions.putObject(newAuction)

                    mockGetByCpIdAndStage(
                        cpid = ContextGenerator.CPID,
                        stage = ContextGenerator.PREV_STAGE,
                        data = pnWithItems
                    )

                    val cm = commandMessage(command = command, data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
                    }

                    assertEquals(ErrorType.NUMBER_AUCTIONS_NOT_MATCH_TO_LOTS, exception.error)
                }

                @DisplayName("6. eAccess analyzes the values of electronicAuctions.details.relatedLot from Request")
                @Test
                fun vr3_8_15_6() {
                    requestNode.getObject("tender", "electronicAuctions")
                        .getArray("details")
                        .getObject(0) {
                            putAttribute("relatedLot", "UNKNOWN")
                        }

                    mockGetByCpIdAndStage(
                        cpid = ContextGenerator.CPID,
                        stage = ContextGenerator.PREV_STAGE,
                        data = pnWithItems
                    )

                    val cm = commandMessage(command = command, data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
                    }

                    assertEquals(ErrorType.LOT_ID_NOT_MATCH_TO_RELATED_LOT_IN_AUCTIONS, exception.error)
                }

                @Nested
                @DisplayName("5. eAccess checks eligibleMinimumDifference by rule VR-15.1.2(Auction)")
                inner class VR3_8_15_7 {

                    @DisplayName("Check amount of the eligible minimum difference auction.")
                    @Test
                    fun vr3_8_15_7_1() {
                        requestNode.getObject("tender", "electronicAuctions")
                            .getArray("details")
                            .getObject(0)
                            .getArray("electronicAuctionModalities")
                            .getObject(0)
                            .getObject("eligibleMinimumDifference")
                            .setAttribute("amount", 1000.0)

                        mockGetByCpIdAndStage(
                            cpid = ContextGenerator.CPID,
                            stage = ContextGenerator.PREV_STAGE,
                            data = pnWithItems
                        )

                        val cm = commandMessage(command = command, data = requestNode)
                        val exception = assertThrows<ErrorException> {
                            service.checkCnOnPn(cm)
                        }

                        assertEquals(ErrorType.INVALID_AUCTION_MINIMUM, exception.error)
                    }

                    @DisplayName("Check currency of the eligible minimum difference auction.")
                    @Test
                    fun vr3_8_15_7_2() {
                        requestNode.getObject("tender", "electronicAuctions")
                            .getArray("details")
                            .getObject(0)
                            .getArray("electronicAuctionModalities")
                            .getObject(0)
                            .getObject("eligibleMinimumDifference")
                            .setAttribute("currency", "UNKNOWN")

                        mockGetByCpIdAndStage(
                            cpid = ContextGenerator.CPID,
                            stage = ContextGenerator.PREV_STAGE,
                            data = pnWithItems
                        )

                        val cm = commandMessage(command = command, data = requestNode)
                        val exception = assertThrows<ErrorException> {
                            service.checkCnOnPn(cm)
                        }

                        assertEquals(ErrorType.INVALID_AUCTION_CURRENCY, exception.error)
                    }
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

                val tenderPeriodStartDate = minStartDateOfContractPeriod.plusDays(1)
                val tenderPeriodEndDate = minStartDateOfContractPeriod.plusDays(2)
                requestNode.getObject("tender", "tenderPeriod")
                    .setAttribute(
                        name = "startDate",
                        value = tenderPeriodStartDate.format(JsonDateTimeFormatter.formatter)
                    )
                    .setAttribute(
                        name = "endDate",
                        value = tenderPeriodEndDate.format(JsonDateTimeFormatter.formatter)
                    )

                mockGetByCpIdAndStage(
                    cpid = ContextGenerator.CPID,
                    stage = ContextGenerator.PREV_STAGE,
                    data = pnWithItems
                )

                val cm = commandMessage(command = command, data = requestNode)
                val exception = assertThrows<ErrorException> {
                    service.checkCnOnPn(cm)
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

                mockGetByCpIdAndStage(
                    cpid = ContextGenerator.CPID,
                    stage = ContextGenerator.PREV_STAGE,
                    data = pnWithItems
                )

                val cm = commandMessage(command = command, data = requestNode)
                val exception = assertThrows<ErrorException> {
                    service.checkCnOnPn(cm)
                }

                assertEquals(ErrorType.INVALID_DOCS_RELATED_LOTS, exception.error)
            }

            @DisplayName("VR-3.8.18 Check error when tender is unsuccessful status.")
            @Test
            fun vr_3_8_18() {
                pnWithItems.getObject("tender")
                    .setAttribute("status", TenderStatus.UNSUCCESSFUL.value)

                mockGetByCpIdAndStage(
                    cpid = ContextGenerator.CPID,
                    stage = ContextGenerator.PREV_STAGE,
                    data = pnWithItems
                )

                val cm = commandMessage(command = command, data = requestNode)
                val exception = assertThrows<ErrorException> {
                    service.checkCnOnPn(cm)
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
                requestNode = loadJson(PATH_REQUEST_OP_JSON).toNode() as ObjectNode
                pnWithoutItems = (loadJson(PATH_PN_JSON).toNode() as ObjectNode).apply {
                    getObject("tender") {
                        putArray("lots")
                        putArray("items")
                    }
                }
            }

            @Test
            fun success() {
                mockGetByCpIdAndStage(
                    cpid = ContextGenerator.CPID,
                    stage = ContextGenerator.PREV_STAGE,
                    data = pnWithoutItems
                )

                val cm = commandMessage(command = command, data = requestNode)
                val response = service.checkCnOnPn(cm)
                assertEquals("ok", response.data)
            }

            @DisplayName("VR-3.8.1(CN on PN)")
            @Test
            fun vr3_8_01() {
                mockGetByCpIdAndStage(
                    cpid = ContextGenerator.CPID,
                    stage = ContextGenerator.PREV_STAGE,
                    data = pnWithoutItems
                )

                val cm = commandMessage(command = command, token = "UNKNOWN", data = requestNode)
                val exception = assertThrows<ErrorException> {
                    service.checkCnOnPn(cm)
                }

                assertEquals(ErrorType.INVALID_TOKEN, exception.error)
            }

            @DisplayName("VR-3.8.2(CN on PN)")
            @Test
            fun vr3_8_02() {
                mockGetByCpIdAndStage(
                    cpid = ContextGenerator.CPID,
                    stage = ContextGenerator.PREV_STAGE,
                    data = pnWithoutItems
                )

                val cm = commandMessage(command = command, owner = "UNKNOWN", data = requestNode)
                val exception = assertThrows<ErrorException> {
                    service.checkCnOnPn(cm)
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
                    mockGetByCpIdAndStage(
                        cpid = ContextGenerator.CPID,
                        stage = ContextGenerator.PREV_STAGE,
                        data = pnWithoutItems
                    )

                    val cm = commandMessage(command = command, data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
                    }

                    assertEquals(ErrorType.INVALID_DOCS_ID, exception.error)
                }

                @DisplayName("VR-3.8.3(CN on PN) -> VR-3.6.1(CNEntity) PN without documents")
                @Test
                fun vr3_8_03_vr_3_6_1_PN_without_documents() {
                    requestNode.getObject("tender")
                        .getArray("documents") {
                            val copyDocument = getObject(0).deepCopy()
                            putObject(copyDocument)
                        }
                    pnWithoutItems.getObject("tender")
                        .remove("documents")

                    mockGetByCpIdAndStage(
                        cpid = ContextGenerator.CPID,
                        stage = ContextGenerator.PREV_STAGE,
                        data = pnWithoutItems
                    )

                    val cm = commandMessage(command = command, data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
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
                    mockGetByCpIdAndStage(
                        cpid = ContextGenerator.CPID,
                        stage = ContextGenerator.PREV_STAGE,
                        data = pnWithoutItems
                    )

                    val cm = commandMessage(command = command, data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
                    }

                    assertEquals(ErrorType.INVALID_DOCS_ID, exception.error)
                }
            }

            @DisplayName("VR-3.8.4(CN on PN)")
            @Test
            fun vr3_8_04() {
                pnWithoutItems.getObject("planning", "budget", "amount")
                    .setAttribute("amount", BigDecimal(1.0))
                mockGetByCpIdAndStage(
                    cpid = ContextGenerator.CPID,
                    stage = ContextGenerator.PREV_STAGE,
                    data = pnWithoutItems
                )

                val cm = commandMessage(command = command, data = requestNode)
                val exception = assertThrows<ErrorException> {
                    service.checkCnOnPn(cm)
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

                mockGetByCpIdAndStage(
                    cpid = ContextGenerator.CPID,
                    stage = ContextGenerator.PREV_STAGE,
                    data = pnWithoutItems
                )

                val cm = commandMessage(command = command, data = requestNode)
                val exception = assertThrows<ErrorException> {
                    service.checkCnOnPn(cm)
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

                    mockGetByCpIdAndStage(
                        cpid = ContextGenerator.CPID,
                        stage = ContextGenerator.PREV_STAGE,
                        data = pnWithoutItems
                    )

                    val cm = commandMessage(command = command, data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
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
                    val budgetBreakdownPeriodEndDate = maxStartDateOfContractPeriod.plusYears(1)

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
                                budgetBreakdownPeriodEndDate.format(JsonDateTimeFormatter.formatter)
                            )
                        }

                    mockGetByCpIdAndStage(
                        cpid = ContextGenerator.CPID,
                        stage = ContextGenerator.PREV_STAGE,
                        data = pnWithoutItems
                    )

                    val cm = commandMessage(command = command, data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
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

                mockGetByCpIdAndStage(
                    cpid = ContextGenerator.CPID,
                    stage = ContextGenerator.PREV_STAGE,
                    data = pnWithoutItems
                )

                val cm = commandMessage(command = command, data = requestNode)
                val exception = assertThrows<ErrorException> {
                    service.checkCnOnPn(cm)
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

                    mockGetByCpIdAndStage(
                        cpid = ContextGenerator.CPID,
                        stage = ContextGenerator.PREV_STAGE,
                        data = pnWithoutItems
                    )

                    val cm = commandMessage(command = command, data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
                    }

                    assertEquals(ErrorType.INVALID_LOT_CONTRACT_PERIOD, exception.error)
                }

                @DisplayName("Check contract period of a lot")
                @Test
                fun vr3_8_8_2() {
                    requestNode.getObject("tender") {
                        val tenderPeriod = getObject("tenderPeriod")
                        val contractPeriod = getArray("lots").getObject(0).getObject("contractPeriod")
                        val startDate = contractPeriod.getString("startDate").asText()
                        tenderPeriod.setAttribute("endDate", startDate)

                    }

                    mockGetByCpIdAndStage(
                        cpid = ContextGenerator.CPID,
                        stage = ContextGenerator.PREV_STAGE,
                        data = pnWithoutItems
                    )

                    val cm = commandMessage(command = command, data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
                    }

                    assertEquals(ErrorType.INVALID_LOT_CONTRACT_PERIOD, exception.error)
                }
            }

            @DisplayName("VR-3.8.9(CN on PN)")
            @Test
            fun vr3_8_09() {
                requestNode.getObject("tender").getArray("items") {
                    getObject(0) {
                        setAttribute("quantity", 0.0)
                    }
                }

                mockGetByCpIdAndStage(
                    cpid = ContextGenerator.CPID,
                    stage = ContextGenerator.PREV_STAGE,
                    data = pnWithoutItems
                )

                val cm = commandMessage(command = command, data = requestNode)
                val exception = assertThrows<ErrorException> {
                    service.checkCnOnPn(cm)
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

                    mockGetByCpIdAndStage(
                        cpid = ContextGenerator.CPID,
                        stage = ContextGenerator.PREV_STAGE,
                        data = pnWithoutItems
                    )

                    val cm = commandMessage(command = command, data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
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

                    mockGetByCpIdAndStage(
                        cpid = ContextGenerator.CPID,
                        stage = ContextGenerator.PREV_STAGE,
                        data = pnWithoutItems
                    )

                    val cm = commandMessage(command = command, data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
                    }

                    assertEquals(ErrorType.LOT_ID_NOT_MATCH_TO_RELATED_LOT_IN_ITEMS, exception.error)
                }
            }

            @DisplayName("VR-3.8.11(CN on PN)")
            @Test
            fun vr3_8_11() {
                val items = requestNode.getObject("tender").getArray("items")
                val item = items.getObject(0).deepCopy {
                    setAttribute("relatedLot", UUID.randomUUID().toString())
                }
                items.putObject(item)

                mockGetByCpIdAndStage(
                    cpid = ContextGenerator.CPID,
                    stage = ContextGenerator.PREV_STAGE,
                    data = pnWithoutItems
                )

                val cm = commandMessage(command = command, data = requestNode)
                val exception = assertThrows<ErrorException> {
                    service.checkCnOnPn(cm)
                }

                assertEquals(ErrorType.INVALID_ITEMS_RELATED_LOTS, exception.error)
            }

            @DisplayName("VR-3.8.12(CN on PN)")
            @Test
            fun vr3_8_12() {
                val lots = requestNode.getObject("tender").getArray("lots")
                val duplicate = lots.getObject(0).deepCopy()
                lots.putObject(duplicate)

                mockGetByCpIdAndStage(
                    cpid = ContextGenerator.CPID,
                    stage = ContextGenerator.PREV_STAGE,
                    data = pnWithoutItems
                )

                val cm = commandMessage(command = command, data = requestNode)
                val exception = assertThrows<ErrorException> {
                    service.checkCnOnPn(cm)
                }

                assertEquals(ErrorType.LOT_ID_DUPLICATED, exception.error)
            }

            @DisplayName("VR-3.8.13(CN on PN)")
            @Test
            fun vr3_8_13() {
                val items = requestNode.getObject("tender").getArray("items")
                val duplicate = items.getObject(0).deepCopy()
                items.putObject(duplicate)

                mockGetByCpIdAndStage(
                    cpid = ContextGenerator.CPID,
                    stage = ContextGenerator.PREV_STAGE,
                    data = pnWithoutItems
                )

                val cm = commandMessage(command = command, data = requestNode)
                val exception = assertThrows<ErrorException> {
                    service.checkCnOnPn(cm)
                }

                assertEquals(ErrorType.ITEM_ID_IS_DUPLICATED, exception.error)
            }

            @Nested
            @DisplayName("VR-3.8.14(CN on PN)")
            inner class VR3_8_14 {
                @DisplayName("Checks procurementMethodModalities in required auction.")
                @Test
                fun vr3_8_14_01() {
                    requestNode.getObject("tender")
                        .putArray("procurementMethodModalities")

                    whenever(rulesService.isAuctionRequired(any(), any(), any()))
                        .thenReturn(true)

                    mockGetByCpIdAndStage(
                        cpid = ContextGenerator.CPID,
                        stage = ContextGenerator.PREV_STAGE,
                        data = pnWithoutItems
                    )

                    val cm = commandMessage(command = command, data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
                    }

                    assertEquals(ErrorType.INVALID_PMM, exception.error)
                }

                @DisplayName("Checks electronicAuctions in required auction.")
                @Test
                fun vr3_8_14_02() {
                    requestNode.getObject("tender")
                        .remove("electronicAuctions")

                    whenever(rulesService.isAuctionRequired(any(), any(), any()))
                        .thenReturn(true)

                    mockGetByCpIdAndStage(
                        cpid = ContextGenerator.CPID,
                        stage = ContextGenerator.PREV_STAGE,
                        data = pnWithoutItems
                    )

                    val cm = commandMessage(command = command, data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
                    }

                    assertEquals(ErrorType.INVALID_AUCTION_IS_EMPTY, exception.error)
                }

                @DisplayName("1. Checks the uniqueness of all electronicAuctions.details.ID from Request")
                @Test
                fun vr3_8_14_1() {
                    val auctions = requestNode.getObject("tender", "electronicAuctions").getArray("details")
                    val duplicate = auctions.getObject(0).deepCopy()
                    auctions.putObject(duplicate)

                    mockGetByCpIdAndStage(
                        cpid = ContextGenerator.CPID,
                        stage = ContextGenerator.PREV_STAGE,
                        data = pnWithoutItems
                    )

                    val cm = commandMessage(command = command, data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
                    }

                    assertEquals(ErrorType.AUCTION_ID_DUPLICATED, exception.error)
                }

                @DisplayName("2. Checks the uniqueness of all electronicAuctions.details.relatedLot values from Request")
                @Test
                fun vr3_8_14_2() {
                    val auctions = requestNode.getObject("tender", "electronicAuctions").getArray("details")
                    val newAuction = auctions.getObject(0).deepCopy {
                        putAttribute("id", "new-auction-id")
                    }
                    auctions.putObject(newAuction)

                    mockGetByCpIdAndStage(
                        cpid = ContextGenerator.CPID,
                        stage = ContextGenerator.PREV_STAGE,
                        data = pnWithoutItems
                    )

                    val cm = commandMessage(command = command, data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
                    }

                    assertEquals(ErrorType.AUCTIONS_CONTAIN_DUPLICATE_RELATED_LOTS, exception.error)
                }

                @DisplayName("3. eAccess compares Lots list from Request and electronicAuctions.details list from Request")
                @Test
                fun vr3_8_14_3() {
                    val auctions = requestNode.getObject("tender", "electronicAuctions").getArray("details")
                    val newAuction = auctions.getObject(0).deepCopy {
                        putAttribute("id", "new-auction-id")
                        putAttribute("relatedLot", "UNKNOWN")
                    }
                    auctions.putObject(newAuction)

                    mockGetByCpIdAndStage(
                        cpid = ContextGenerator.CPID,
                        stage = ContextGenerator.PREV_STAGE,
                        data = pnWithoutItems
                    )

                    val cm = commandMessage(command = command, data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
                    }

                    assertEquals(ErrorType.NUMBER_AUCTIONS_NOT_MATCH_TO_LOTS, exception.error)
                }

                @DisplayName("4. eAccess analyzes the values of electronicAuctions.details.relatedLot from Request")
                @Test
                fun vr3_8_14_4() {
                    requestNode.getObject("tender", "electronicAuctions")
                        .getArray("details")
                        .getObject(0) {
                            putAttribute("relatedLot", "UNKNOWN")
                        }

                    mockGetByCpIdAndStage(
                        cpid = ContextGenerator.CPID,
                        stage = ContextGenerator.PREV_STAGE,
                        data = pnWithoutItems
                    )

                    val cm = commandMessage(command = command, data = requestNode)
                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
                    }

                    assertEquals(ErrorType.LOT_ID_NOT_MATCH_TO_RELATED_LOT_IN_AUCTIONS, exception.error)
                }

                @Nested
                @DisplayName("5. eAccess checks eligibleMinimumDifference by rule VR-15.1.2(Auction)")
                inner class VR3_8_14_5 {

                    @DisplayName("Check amount of the eligible minimum difference auction.")
                    @Test
                    fun vr3_8_14_5_1() {
                        requestNode.getObject("tender", "electronicAuctions")
                            .getArray("details")
                            .getObject(0)
                            .getArray("electronicAuctionModalities")
                            .getObject(0)
                            .getObject("eligibleMinimumDifference")
                            .setAttribute("amount", 1000.0)

                        mockGetByCpIdAndStage(
                            cpid = ContextGenerator.CPID,
                            stage = ContextGenerator.PREV_STAGE,
                            data = pnWithoutItems
                        )

                        val cm = commandMessage(command = command, data = requestNode)
                        val exception = assertThrows<ErrorException> {
                            service.checkCnOnPn(cm)
                        }

                        assertEquals(ErrorType.INVALID_AUCTION_MINIMUM, exception.error)
                    }

                    @DisplayName("Check currency of the eligible minimum difference auction.")
                    @Test
                    fun vr3_8_14_5_2() {
                        requestNode.getObject("tender", "electronicAuctions")
                            .getArray("details")
                            .getObject(0)
                            .getArray("electronicAuctionModalities")
                            .getObject(0)
                            .getObject("eligibleMinimumDifference")
                            .setAttribute("currency", "UNKNOWN")

                        mockGetByCpIdAndStage(
                            cpid = ContextGenerator.CPID,
                            stage = ContextGenerator.PREV_STAGE,
                            data = pnWithoutItems
                        )

                        val cm = commandMessage(command = command, data = requestNode)
                        val exception = assertThrows<ErrorException> {
                            service.checkCnOnPn(cm)
                        }

                        assertEquals(ErrorType.INVALID_AUCTION_CURRENCY, exception.error)
                    }
                }
            }

            @DisplayName("VR-3.8.18 Check error when tender is unsuccessful status.")
            @Test
            fun vr_3_8_18() {
                pnWithoutItems.getObject("tender")
                    .setAttribute("status", TenderStatus.UNSUCCESSFUL.value)

                mockGetByCpIdAndStage(
                    cpid = ContextGenerator.CPID,
                    stage = ContextGenerator.PREV_STAGE,
                    data = pnWithoutItems
                )

                val cm = commandMessage(command = command, data = requestNode)
                val exception = assertThrows<ErrorException> {
                    service.checkCnOnPn(cm)
                }

                assertEquals(ErrorType.TENDER_IN_UNSUCCESSFUL_STATUS, exception.error)
            }
        }

        @Nested
        inner class ProcuringEntity {

            private val CHECK_REQUEST_JSON = "json/dto/create/cn_on_pn/op/request/request_cn_on_pn_full.json"
            private lateinit var requestNode: ObjectNode
            private lateinit var pnEntity: ObjectNode

            @BeforeEach
            fun setup() {
                requestNode = loadJson(CHECK_REQUEST_JSON).toNode() as ObjectNode
                pnEntity = loadJson(PATH_PN_JSON).toNode() as ObjectNode

                mockGetByCpIdAndStage(
                    cpid = ContextGenerator.CPID,
                    stage = ContextGenerator.PREV_STAGE,
                    data = pnEntity
                )

                val expectedId = pnEntity.getObject("tender").getObject("procuringEntity").get("id").asText()
                requestNode.getObject("tender").getObject("procuringEntity").setAttribute("id", expectedId)
            }

            @AfterEach
            fun clear() {
                clearInvocations(tenderProcessDao)
            }

            @Test
            fun `without procuring entity`() {
                requestNode.getObject("tender").remove("procuringEntity")
                val cm = commandMessage(command = command, data = requestNode)

                assertDoesNotThrow { service.checkCnOnPn(cm) }
            }

            @Nested
            inner class VR_1_0_1_10_1 {

                @Test
                fun `Request_procuringentity_Id == DB_procuringentity_Id`() {

                    val person = requestNode.getObject("tender")
                        .getObject("procuringEntity")
                        .getArray("persones")
                        .get(0) as ObjectNode

                    val businessFunction = person.getArray("businessFunctions").get(0) as ObjectNode
                    businessFunction.getObject("period").putAttribute("startDate", ContextGenerator.START_DATE)

                    val cm = commandMessage(command = command, data = requestNode)

                    assertDoesNotThrow { service.checkCnOnPn(cm) }
                }

                @Test
                fun `Request_procuringEntity_Id != DB_procuringEntity_Id`() {

                    requestNode.getObject("tender").getObject("procuringEntity").setAttribute("id", "UNKNOWN_ID")
                    val cm = commandMessage(command = command, data = requestNode)

                    val exception = assertThrows<ErrorException> { service.checkCnOnPn(cm) }
                    assertEquals(ErrorType.INVALID_PROCURING_ENTITY, exception.error)
                    assertTrue(exception.message!!.contains("Invalid identifier of procuring entity"))
                }
            }

            @Nested
            inner class VR_1_0_1_10_2 {
                @Test
                fun `no persones`() {
                    requestNode.getObject("tender").getObject("procuringEntity").putArray("persones")
                    val cm = commandMessage(command = command, data = requestNode)

                    val exception = assertThrows<ErrorException> { service.checkCnOnPn(cm) }
                    assertEquals(ErrorType.INVALID_PROCURING_ENTITY, exception.error)
                    assertTrue(exception.message!!.contains("At least one Person should be added"))
                }
            }

            @Nested
            inner class VR_1_0_1_10_3 {

                @Test
                fun `not unique persones id`() {
                    val person = requestNode.getObject("tender")
                        .getObject("procuringEntity")
                        .getArray("persones")
                        .get(0) as ObjectNode

                    requestNode.getObject("tender")
                        .getObject("procuringEntity")
                        .putArray("persones")
                        .putObject(person)
                        .putObject(person)

                    val cm = commandMessage(command = command, data = requestNode)

                    val exception = assertThrows<ErrorException> { service.checkCnOnPn(cm) }
                    assertEquals(ErrorType.INVALID_PROCURING_ENTITY, exception.error)
                    assertTrue(exception.message!!.contains("Persones objects should be unique in Request"))
                }
            }

            @Nested
            inner class VR_1_0_1_10_6 {

                @Test
                fun `two authority person`() {

                    val person = requestNode.getObject("tender")
                        .getObject("procuringEntity")
                        .getArray("persones")
                        .get(0) as ObjectNode

                    val newPerson = person.deepCopy().also {
                        it.getObject("identifier").setAttribute("id", "NEW_UNIQUE_ID")
                        val businessFunction = it.getArray("businessFunctions").get(0) as ObjectNode
                        businessFunction.setAttribute("type", "authority")
                    }

                    requestNode.getObject("tender")
                        .getObject("procuringEntity")
                        .putArray("persones")
                        .putObject(person)
                        .putObject(newPerson)

                    val cm = commandMessage(command = command, data = requestNode)

                    val exception = assertThrows<ErrorException> { service.checkCnOnPn(cm) }
                    assertEquals(ErrorType.INVALID_PROCURING_ENTITY, exception.error)
                    assertTrue(exception.message!!.contains("Authority person should be specified in Request"))
                }
            }

            @Nested
            inner class VR_1_0_1_10_4 {

                @Test
                fun `no businessFunctions`() {

                    val person = requestNode.getObject("tender")
                        .getObject("procuringEntity")
                        .getArray("persones")
                        .get(0) as ObjectNode

                    val newPerson = person.deepCopy().also {
                        it.getObject("identifier").setAttribute("id", "NEW_UNIQUE_ID")
                        println(it)
                        val businessFunction = it.getArray("businessFunctions").get(0) as ObjectNode
                        businessFunction.setAttribute("type", "authority")
                    }
                    person.putArray("businessFunctions")

                    requestNode.getObject("tender")
                        .getObject("procuringEntity")
                        .putArray("persones")
                        .putObject(person)
                        .putObject(newPerson)

                    val cm = commandMessage(command = command, data = requestNode)

                    val exception = assertThrows<ErrorException> { service.checkCnOnPn(cm) }
                    assertEquals(ErrorType.INVALID_PROCURING_ENTITY, exception.error)
                    assertTrue(exception.message!!.contains("At least one businessFunctions detalization should be added"))
                }
            }

            @Nested
            inner class VR_1_0_1_10_5 {

                @Test
                fun `not unique businessFunction id in person`() {
                    val person = requestNode.getObject("tender")
                        .getObject("procuringEntity")
                        .getArray("persones")
                        .get(0) as ObjectNode

                    val businessFunction = person.getArray("businessFunctions").get(0) as ObjectNode
                    val duplicatedBusinessFunction = businessFunction.deepCopy()

                    person.putArray("businessFunctions")
                        .add(businessFunction)
                        .add(duplicatedBusinessFunction)

                    val cm = commandMessage(command = command, data = requestNode)

                    val exception = assertThrows<ErrorException> { service.checkCnOnPn(cm) }
                    assertEquals(ErrorType.INVALID_PROCURING_ENTITY, exception.error)

                    // TODO uncomment when BussinesFunctionType enum will have at least 2 value
                    //assertTrue(exception.message!!.contains("businessFunctions objects should be unique in every Person from Request"))
                }
            }

            @Nested
            inner class VR_1_0_1_10_7 {

                @Test
                fun `startDate in request greater than in context`() {
                    val REQUEST_START_DATE = "2012-06-05T17:59:00Z"

                    val person = requestNode.getObject("tender")
                        .getObject("procuringEntity")
                        .getArray("persones")
                        .get(0) as ObjectNode

                    val businessFunction = person.getArray("businessFunctions").get(0) as ObjectNode
                    businessFunction.getObject("period").putAttribute("startDate", REQUEST_START_DATE)

                    val cm = commandMessage(command = command, data = requestNode)

                    val exception = assertThrows<ErrorException> { service.checkCnOnPn(cm) }
                    assertEquals(ErrorType.INVALID_PROCURING_ENTITY, exception.error)

                    assertTrue(exception.message!!.contains("Invalid period in bussiness function specification"))
                }

                @Test
                fun `startDate in request == startDate context`() {
                    val person = requestNode.getObject("tender")
                        .getObject("procuringEntity")
                        .getArray("persones")
                        .get(0) as ObjectNode

                    val businessFunction = person.getArray("businessFunctions").get(0) as ObjectNode
                    businessFunction.getObject("period").putAttribute("startDate", ContextGenerator.START_DATE)

                    val cm = commandMessage(command = command, data = requestNode)

                    assertDoesNotThrow { service.checkCnOnPn(cm) }
                }
            }

            @Nested
            inner class VR_1_0_1_2_1 {

                @Test
                fun `not unique document id`() {

                    val person = requestNode.getObject("tender")
                        .getObject("procuringEntity")
                        .getArray("persones")
                        .get(0) as ObjectNode

                    val businessFunction = person.getArray("businessFunctions").get(0) as ObjectNode
                    businessFunction.getObject("period").putAttribute("startDate", ContextGenerator.START_DATE)

                    val document = businessFunction.getArray("documents").get(0) as ObjectNode
                    businessFunction.putArray("documents")
                        .add(document)
                        .add(document)

                    val cm = commandMessage(command = command, data = requestNode)

                    val exception = assertThrows<ErrorException> { service.checkCnOnPn(cm) }
                    assertEquals(ErrorType.INVALID_PROCURING_ENTITY, exception.error)

                    assertTrue(exception.message!!.contains("Invalid documents IDs"))
                }
            }

            @Nested
            inner class VR_1_0_1_2_7 {

                @Test
                fun `no document passed`() {

                    val person = requestNode.getObject("tender")
                        .getObject("procuringEntity")
                        .getArray("persones")
                        .get(0) as ObjectNode

                    val businessFunction = person.getArray("businessFunctions").get(0) as ObjectNode
                    businessFunction.getObject("period").putAttribute("startDate", ContextGenerator.START_DATE)

                    businessFunction.putArray("documents")

                    val cm = commandMessage(command = command, data = requestNode)

                    val exception = assertThrows<ErrorException> { service.checkCnOnPn(cm) }
                    assertEquals(ErrorType.INVALID_PROCURING_ENTITY, exception.error)

                    assertTrue(exception.message!!.contains("At least one document should be added"))
                }
            }

            @Nested
            inner class VR_1_0_1_2_8 {

                @Test
                fun `invalid document type`() {

                    val person = requestNode.getObject("tender")
                        .getObject("procuringEntity")
                        .getArray("persones")
                        .get(0) as ObjectNode

                    val businessFunction = person.getArray("businessFunctions").get(0) as ObjectNode
                    businessFunction.getObject("period").putAttribute("startDate", ContextGenerator.START_DATE)

                    val document = businessFunction.getArray("documents").get(0) as ObjectNode
                    document.setAttribute("documentType", "ANOTHER_DOCUMENT_TYPE")

                    val cm = commandMessage(command = command, data = requestNode)

                    assertThrows<Exception> { service.checkCnOnPn(cm) }
                }
            }
        }

        private fun mockGetByCpIdAndStage(cpid: String, stage: String, data: JsonNode) {
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = data.toString())
            whenever(tenderProcessDao.getByCpIdAndStage(eq(cpid), eq(stage)))
                .thenReturn(tenderProcessEntity)
        }
    }

    @DisplayName("Create Endpoint")
    @Nested
    inner class Create {
        private val command = CommandType.CREATE_CN_ON_PN

        @DisplayName("Check pmd in command.")
        @Test
        fun checkPMD() {
            val cm = commandMessage(command = command, pmd = "UNKNOWN", data = NullNode.instance)
            val exception = assertThrows<ErrorException> {
                service.checkCnOnPn(cm)
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
                    eq(ContextGenerator.CPID),
                    eq(ContextGenerator.PREV_STAGE)
                )
            )
                .thenReturn(null)

            val cm = commandMessage(command = command, data = data)
            val exception = assertThrows<ErrorException> {
                service.checkCnOnPn(cm)
            }

            assertEquals(ErrorType.DATA_NOT_FOUND, exception.error)
        }

        @Nested
        inner class WithAuctions {
            private val hasAuctionsInRequest = true

            @Nested
            inner class PNWithItems {
                private val hasItemsInPN = true

                @Nested
                inner class WithDocuments {
                    private val hasDocumentsInPN = true

                    @ParameterizedTest(name = "{index} => auctions are required: {0}")
                    @CsvSource(value = ["true", "false"])
                    fun test(isAuctionRequired: Boolean) {
                        val testData = WhenTestData(
                            isAuctionRequired = isAuctionRequired,
                            hasAuctionsInRequest = hasAuctionsInRequest,
                            hasItemsInPN = hasItemsInPN,
                            hasDocumentsInPN = hasDocumentsInPN
                        )
                        testOfCreate(testData)
                    }
                }

                @Nested
                inner class WithoutDocuments {
                    private val hasDocumentsInPN = false

                    @ParameterizedTest(name = "{index} => auctions are required: {0}")
                    @CsvSource(value = ["true", "false"])
                    fun test(isAuctionRequired: Boolean) {
                        val testData = WhenTestData(
                            isAuctionRequired = isAuctionRequired,
                            hasAuctionsInRequest = hasAuctionsInRequest,
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

                    @ParameterizedTest(name = "{index} => auctions are required: {0}")
                    @CsvSource(value = ["true", "false"])
                    fun test(isAuctionRequired: Boolean) {
                        val testData = WhenTestData(
                            isAuctionRequired = isAuctionRequired,
                            hasAuctionsInRequest = hasAuctionsInRequest,
                            hasItemsInPN = hasItemsInPN,
                            hasDocumentsInPN = hasDocumentsInPN
                        )
                        testOfCreate(testData)
                    }
                }

                @Nested
                inner class WithoutDocuments {
                    private val hasDocumentsInPN = false

                    @ParameterizedTest(name = "{index} => auctions are required: {0}")
                    @CsvSource(value = ["true", "false"])
                    fun test(isAuctionRequired: Boolean) {
                        val testData = WhenTestData(
                            isAuctionRequired = isAuctionRequired,
                            hasAuctionsInRequest = hasAuctionsInRequest,
                            hasItemsInPN = hasItemsInPN,
                            hasDocumentsInPN = hasDocumentsInPN
                        )
                        testOfCreate(testData)
                    }
                }
            }
        }

        @Nested
        inner class WithoutAuctions {
            private val hasAuctionsInRequest = false

            @Nested
            inner class PNWithItems {
                private val hasItemsInPN = true

                @Nested
                inner class WithDocuments {
                    private val hasDocumentsInPN = true

                    @ParameterizedTest(name = "{index} => auctions are required: {0}")
                    @CsvSource(value = ["true", "false"])
                    fun test(isAuctionRequired: Boolean) {
                        val testData = WhenTestData(
                            isAuctionRequired = isAuctionRequired,
                            hasAuctionsInRequest = hasAuctionsInRequest,
                            hasItemsInPN = hasItemsInPN,
                            hasDocumentsInPN = hasDocumentsInPN
                        )
                        testOfCreate(testData)
                    }
                }

                @Nested
                inner class WithoutDocuments {
                    private val hasDocumentsInPN = false

                    @ParameterizedTest(name = "{index} => auctions are required: {0}")
                    @CsvSource(value = ["true", "false"])
                    fun test(isAuctionRequired: Boolean) {
                        val testData = WhenTestData(
                            isAuctionRequired = isAuctionRequired,
                            hasAuctionsInRequest = hasAuctionsInRequest,
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

                    @ParameterizedTest(name = "{index} => auctions are required: {0}")
                    @CsvSource(value = ["true", "false"])
                    fun test(isAuctionRequired: Boolean) {
                        val testData = WhenTestData(
                            isAuctionRequired = isAuctionRequired,
                            hasAuctionsInRequest = hasAuctionsInRequest,
                            hasItemsInPN = hasItemsInPN,
                            hasDocumentsInPN = hasDocumentsInPN
                        )
                        testOfCreate(testData)
                    }
                }

                @Nested
                inner class WithoutDocuments {
                    private val hasDocumentsInPN = false

                    @ParameterizedTest(name = "{index} => auctions are required: {0}")
                    @CsvSource(value = ["true", "false"])
                    fun test(isAuctionRequired: Boolean) {
                        val testData = WhenTestData(
                            isAuctionRequired = isAuctionRequired,
                            hasAuctionsInRequest = hasAuctionsInRequest,
                            hasItemsInPN = hasItemsInPN,
                            hasDocumentsInPN = hasDocumentsInPN
                        )
                        testOfCreate(testData)
                    }
                }
            }
        }

        private fun testOfCreate(testData: WhenTestData) {
            val pathToJsonFileOfRequest = testData.requestJsonFile()
            val pathToJsonFileOfPNEntity = testData.pnJsonFile()
            val pathToJsonFileOfResponse = testData.responseJsonFile()

            val data = loadJson(pathToJsonFileOfRequest).toNode()
            val cm = commandMessage(
                command = command,
                data = data
            )

            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson(pathToJsonFileOfPNEntity))

            whenever(generationService.generatePermanentLotId())
                .thenReturn(PERMANENT_LOT_ID_1, PERMANENT_LOT_ID_2)
            whenever(generationService.generatePermanentItemId())
                .thenReturn(PERMANENT_ITEM_ID_1, PERMANENT_ITEM_ID_2, PERMANENT_ITEM_ID_3, PERMANENT_ITEM_ID_4)
            whenever(rulesService.isAuctionRequired(any(), any(), any()))
                .thenReturn(testData.isAuctionRequired)
            whenever(
                tenderProcessDao.getByCpIdAndStage(
                    eq(ContextGenerator.CPID),
                    eq(ContextGenerator.PREV_STAGE)
                )
            )
                .thenReturn(tenderProcessEntity)

            val actualJson = service.createCnOnPn(cm).data!!.toJson()

            val expectedJson = loadJson(pathToJsonFileOfResponse)

            JsonValidator.equalsJsons(expectedJson, actualJson) {
                assert("$['tender']['lots'][0]['status']", LOT_STATUS)
                assert("$['tender']['lots'][1]['status']", LOT_STATUS)
                assert("$['tender']['lots'][0]['statusDetails']", LOT_STATUS_DETAILS)
                assert("$['tender']['lots'][1]['statusDetails']", LOT_STATUS_DETAILS)
            }
        }
    }

    fun commandMessage(
        command: CommandType,
        token: String = ContextGenerator.TOKEN.toString(),
        owner: String = ContextGenerator.OWNER,
        pmd: String = ProcurementMethod.OT.name,
        startDate: String = ContextGenerator.START_DATE,
        data: JsonNode
    ): CommandMessage {
        val context = ContextGenerator.generate(
            token = token,
            owner = owner,
            pmd = pmd,
            operationType = Operation.CREATE_CN_ON_PN.value,
            startDate = startDate
        )

        return CommandMessageGenerator.generate(
            command = command,
            context = context,
            data = data
        )
    }

    class WhenTestData(
        val isAuctionRequired: Boolean,
        val hasAuctionsInRequest: Boolean,
        val hasItemsInPN: Boolean,
        val hasDocumentsInPN: Boolean
    ) {

        fun requestJsonFile(): String {
            val auctionsSegment = JsonFilePathGenerator.auctionSegment(hasAuctionsInRequest)
            return "json/service/create/cn_on_pn/request/op/request_cn_${auctionsSegment}_on_pn.json"
                .also {
                    testingBindingAndMapping<CnOnPnRequest>(it)
                }
        }

        fun pnJsonFile(): String {
            val itemsSegment = JsonFilePathGenerator.itemsSegment(hasItemsInPN)
            val segmentDocuments = JsonFilePathGenerator.documentsSegment(hasDocumentsInPN)
            return "json/service/create/cn_on_pn/entity/pn/entity_pn_${itemsSegment}_${segmentDocuments}.json"
                .also {
                    testingBindingAndMapping<PNEntity>(it)
                }
        }

        fun responseJsonFile(): String {
            val auctionsSegment = JsonFilePathGenerator.auctionSegment(hasAuctionsInRequest)
            val itemsSegment = JsonFilePathGenerator.itemsSegment(hasItemsInPN)
            val segmentDocuments = JsonFilePathGenerator.documentsSegment(hasDocumentsInPN)
            return "json/service/create/cn_on_pn/response/op/response_cn_${auctionsSegment}_on_pn_${itemsSegment}_${segmentDocuments}.json"
                .also {
                    testingBindingAndMapping<CnOnPnResponse>(it)
                }
        }
    }
}

