package com.procurement.access.service

import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
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
import com.procurement.access.json.toJson
import com.procurement.access.json.toNode
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal
import java.time.LocalDateTime

class CnOnPnServiceTest {

    companion object {
        private const val PATH_PN_JSON =
            "json/entity/pn/with_items/with_documents/pn_with_items_with_documents_full.json"

        private val OPERATION_TYPE = Operation.CREATE_CN_ON_PN

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
        private val COMMAND_TYPE = CommandType.CHECK_CN_ON_PN

        @DisplayName("Check pmd in command.")
        @Test
        fun checkPMD() {
            val cm = TestDataGenerator.commandMessage(
                pmd = "UNKNOWN",
                command = COMMAND_TYPE,
                operationType = OPERATION_TYPE,
                data = NullNode.instance
            )
            val exception = assertThrows<ErrorException> {
                service.checkCnOnPn(cm)
            }

            assertEquals(ErrorType.INVALID_PMD, exception.error)
        }

        @DisplayName("Check error when tender by cpid and prev stage not found.")
        @Test
        fun tenderNotFound() {
            val data =
                loadJson("json/dto/create/cn_on_pn/request/op/with_auctions/with_documents/request_cn_with_auctions_with_documents_on_pn_full.json").toNode()
            val cm = TestDataGenerator.commandMessage(
                pmd = ProcurementMethod.OT.name,
                command = COMMAND_TYPE,
                operationType = OPERATION_TYPE,
                data = data
            )

            whenever(
                tenderProcessDao.getByCpIdAndStage(
                    eq(TestDataGenerator.CPID), eq(
                        TestDataGenerator.PREV_STAGE
                    )
                )
            )
                .thenReturn(null)

            val exception = assertThrows<ErrorException> {
                service.checkCnOnPn(cm)
            }

            assertEquals(ErrorType.DATA_NOT_FOUND, exception.error)
        }

        @Nested
        inner class Open {
            private val PATH_REQUEST_OP_JSON =
                "json/dto/create/cn_on_pn/request/op/with_auctions/with_documents/request_cn_with_auctions_with_documents_on_pn_full.json"
            private val pmd = ProcurementMethod.OT.name

            @Nested
            inner class PNEntityWithItems {
                private lateinit var requestNode: ObjectNode
                private lateinit var pnWithItems: ObjectNode

                @BeforeEach
                fun prepare() {
                    requestNode = loadJson(PATH_REQUEST_OP_JSON).toNode() as ObjectNode
                    pnWithItems = loadJson(PATH_PN_JSON).toNode() as ObjectNode
                }

                @Test
                fun success() {
                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = pmd,
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )

                    val response = service.checkCnOnPn(cm)
                    assertEquals("ok", response.data)
                }

                @DisplayName("VR-3.8.1(CNEntity on PNEntity)")
                @Test
                fun vr3_8_01() {
                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = pmd,
                        token = "UNKNOWN",
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )
                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
                    }

                    assertEquals(ErrorType.INVALID_TOKEN, exception.error)
                }

                @DisplayName("VR-3.8.2(CN on PN)")
                @Test
                fun vr3_8_02() {
                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = pmd,
                        owner = "UNKNOWN",
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )
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
                        val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithItems.toString())
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
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
                        val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithItems.toString())
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
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

                        val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithItems.toString())
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
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

                        val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithItems.toString())
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
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

                        val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithItems.toString())
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
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

                        val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithItems.toString())
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
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

                        val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithItems.toString())
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
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

                        val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithItems.toString())
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
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
                                .setAttribute("amount", 1000)

                            val tenderProcessEntity =
                                TestDataGenerator.tenderProcessEntity(data = pnWithItems.toString())
                            whenever(
                                tenderProcessDao.getByCpIdAndStage(
                                    eq(TestDataGenerator.CPID), eq(
                                        TestDataGenerator.PREV_STAGE
                                    )
                                )
                            )
                                .thenReturn(tenderProcessEntity)

                            val cm = TestDataGenerator.commandMessage(
                                pmd = pmd,
                                command = COMMAND_TYPE,
                                operationType = OPERATION_TYPE,
                                data = requestNode
                            )
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

                            val tenderProcessEntity =
                                TestDataGenerator.tenderProcessEntity(data = pnWithItems.toString())
                            whenever(
                                tenderProcessDao.getByCpIdAndStage(
                                    eq(TestDataGenerator.CPID), eq(
                                        TestDataGenerator.PREV_STAGE
                                    )
                                )
                            )
                                .thenReturn(tenderProcessEntity)

                            val cm = TestDataGenerator.commandMessage(
                                pmd = pmd,
                                command = COMMAND_TYPE,
                                operationType = OPERATION_TYPE,
                                data = requestNode
                            )
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
                    requestNode.getObject("tender") {
                        val tenderPeriod = getObject("tenderPeriod")
                        val contractPeriod = getArray("lots").getObject(0).getObject("contractPeriod")
                        val startDate = contractPeriod.getString("startDate").asText()
                        tenderPeriod.setAttribute("endDate", startDate)
                    }

                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = pmd,
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )
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

                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = pmd,
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )
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

                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = ProcurementMethod.OT.name,
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )

                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
                    }

                    assertEquals(ErrorType.TENDER_IN_UNSUCCESSFUL_STATUS, exception.error)
                }
            }

            @Nested
            inner class PNEntityWithoutItems {
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
                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = pmd,
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )

                    val response = service.checkCnOnPn(cm)
                    assertEquals("ok", response.data)
                }

                @DisplayName("VR-3.8.1(CN on PN)")
                @Test
                fun vr3_8_01() {
                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = pmd,
                        token = "UNKNOWN",
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )
                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
                    }

                    assertEquals(ErrorType.INVALID_TOKEN, exception.error)
                }

                @DisplayName("VR-3.8.2(CN on PN)")
                @Test
                fun vr3_8_02() {
                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = pmd,
                        owner = "UNKNOWN",
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )
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
                        val tenderProcessEntity =
                            TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
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
                        val tenderProcessEntity =
                            TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
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
                        .setAttribute("amount", BigDecimal(1))
                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = pmd,
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )
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

                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = pmd,
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )
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
                        val contractPeriodStartDate: LocalDateTime = requestNode.getObject("tender").let { tender ->
                            val arrayLots = tender.getArray("lots") {
                                assertEquals(2, this.size())
                            }

                            val lot1StartDate = arrayLots.getObject(0).let { lot1 ->
                                lot1.getObject("contractPeriod")
                                    .getString("startDate")
                                    .asText()
                                    .let {
                                        LocalDateTime.parse(it, JsonDateTimeFormatter.formatter)
                                    }
                            }
                            val lot2StartDate = arrayLots.getObject(1).let { lot2 ->
                                lot2.getObject("contractPeriod")
                                    .getString("startDate")
                                    .asText()
                                    .let {
                                        LocalDateTime.parse(it, JsonDateTimeFormatter.formatter)
                                    }
                            }

                            val minStartDate = if (lot1StartDate.isBefore(lot2StartDate))
                                lot1StartDate
                            else
                                lot2StartDate

                            minStartDate.minusDays(1)
                        }

                        pnWithoutItems.getObject("planning", "budget")
                            .getArray("budgetBreakdown")
                            .getObject(0)
                            .getObject("period")
                            .putAttribute("endDate", contractPeriodStartDate.format(JsonDateTimeFormatter.formatter))

                        val tenderProcessEntity =
                            TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
                        val exception = assertThrows<ErrorException> {
                            service.checkCnOnPn(cm)
                        }

                        assertEquals(ErrorType.INVALID_LOT_CONTRACT_PERIOD, exception.error)
                    }

                    @DisplayName("check end date")
                    @Test
                    fun endDate() {
                        val contractPeriodEndDate: LocalDateTime = requestNode.getObject("tender").let { tender ->
                            val arrayLots = tender.getArray("lots") {
                                assertEquals(2, this.size())
                            }

                            val lot1EndDate = arrayLots.getObject(0).let { lot1 ->
                                lot1.getObject("contractPeriod")
                                    .getString("endDate")
                                    .asText()
                                    .let {
                                        LocalDateTime.parse(it, JsonDateTimeFormatter.formatter)
                                    }
                            }
                            val lot2EndDate = arrayLots.getObject(1).let { lot2 ->
                                lot2.getObject("contractPeriod")
                                    .getString("endDate")
                                    .asText()
                                    .let {
                                        LocalDateTime.parse(it, JsonDateTimeFormatter.formatter)
                                    }
                            }

                            val maxEndDate = if (lot1EndDate.isAfter(lot2EndDate))
                                lot1EndDate
                            else
                                lot2EndDate

                            maxEndDate.plusDays(1)
                        }

                        pnWithoutItems.getObject("planning", "budget")
                            .getArray("budgetBreakdown")
                            .getObject(0)
                            .getObject("period") {
                                putAttribute("startDate", contractPeriodEndDate.format(JsonDateTimeFormatter.formatter))
                                putAttribute(
                                    "endDate",
                                    contractPeriodEndDate.plusYears(1).format(JsonDateTimeFormatter.formatter)
                                )
                            }

                        val tenderProcessEntity =
                            TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
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

                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = pmd,
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )
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

                        val tenderProcessEntity =
                            TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
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

                        val tenderProcessEntity =
                            TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
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
                            setAttribute("quantity", 0)
                        }
                    }

                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = pmd,
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )
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

                        val tenderProcessEntity =
                            TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
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

                        val tenderProcessEntity =
                            TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
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
                    val newItem = items.getObject(0).deepCopy {
                        putAttribute("relatedLot", "UNKNOWN")
                    }
                    items.add(newItem)

                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = pmd,
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )
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

                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = pmd,
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )
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

                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = pmd,
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )
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

                        val tenderProcessEntity =
                            TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
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

                        val tenderProcessEntity =
                            TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
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

                        val tenderProcessEntity =
                            TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
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

                        val tenderProcessEntity =
                            TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
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

                        val tenderProcessEntity =
                            TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
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

                        val tenderProcessEntity =
                            TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
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
                                .setAttribute("amount", 1000)

                            val tenderProcessEntity =
                                TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                            whenever(
                                tenderProcessDao.getByCpIdAndStage(
                                    eq(TestDataGenerator.CPID), eq(
                                        TestDataGenerator.PREV_STAGE
                                    )
                                )
                            )
                                .thenReturn(tenderProcessEntity)

                            val cm = TestDataGenerator.commandMessage(
                                pmd = pmd,
                                command = COMMAND_TYPE,
                                operationType = OPERATION_TYPE,
                                data = requestNode
                            )
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

                            val tenderProcessEntity =
                                TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                            whenever(
                                tenderProcessDao.getByCpIdAndStage(
                                    eq(TestDataGenerator.CPID), eq(
                                        TestDataGenerator.PREV_STAGE
                                    )
                                )
                            )
                                .thenReturn(tenderProcessEntity)

                            val cm = TestDataGenerator.commandMessage(
                                pmd = pmd,
                                command = COMMAND_TYPE,
                                operationType = OPERATION_TYPE,
                                data = requestNode
                            )
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

                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = ProcurementMethod.OT.name,
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )

                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
                    }

                    assertEquals(ErrorType.TENDER_IN_UNSUCCESSFUL_STATUS, exception.error)
                }
            }
        }

        @Nested
        inner class Limited {
            private val PATH_REQUEST_LP_JSON =
                "json/dto/create/cn_on_pn/request/lp/without_auctions/with_documents/request_cn_without_auctions_with_documents_on_pn_full.json"
            private val pmd = ProcurementMethod.DA.name

            @Nested
            inner class PNEntityWithItems {
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
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = pmd,
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )

                    val response = service.checkCnOnPn(cm)
                    assertEquals("ok", response.data)
                }

                @DisplayName("VR-3.8.1")
                @Test
                fun vr3_8_01() {
                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = pmd,
                        token = "UNKNOWN",
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )
                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
                    }

                    assertEquals(ErrorType.INVALID_TOKEN, exception.error)
                }

                @DisplayName("VR-3.8.2")
                @Test
                fun vr3_8_02() {
                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = pmd,
                        owner = "UNKNOWN",
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )
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
                        val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithItems.toString())
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
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
                        val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithItems.toString())
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
                        val exception = assertThrows<ErrorException> {
                            service.checkCnOnPn(cm)
                        }

                        assertEquals(ErrorType.INVALID_DOCS_ID, exception.error)
                    }
                }

                @DisplayName("VR-3.8.16(CN on PN)")
                @Test
                fun vr3_8_16() {
                    val startDate = requestNode.getObject("tender")
                        .getArray("lots").getObject(0)
                        .getObject("contractPeriod")
                        .getString("startDate").asText()

                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = pmd,
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        startDate = startDate,
                        data = requestNode
                    )
                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
                    }

                    assertEquals(ErrorType.INVALID_LOT_CONTRACT_PERIOD, exception.error)
                }

                @DisplayName("VR-3.8.18 Check error when tender is unsuccessful status.")
                @Test
                fun vr_3_8_18() {
                    pnWithItems.getObject("tender")
                        .setAttribute("status", TenderStatus.UNSUCCESSFUL.value)

                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = ProcurementMethod.OT.name,
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )

                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
                    }

                    assertEquals(ErrorType.TENDER_IN_UNSUCCESSFUL_STATUS, exception.error)
                }
            }

            @Nested
            inner class PNEntityWithoutItems {
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
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = pmd,
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )

                    val response = service.checkCnOnPn(cm)
                    assertEquals("ok", response.data)
                }

                @DisplayName("VR-3.8.1")
                @Test
                fun vr3_8_01() {
                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = pmd,
                        token = "UNKNOWN",
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )
                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
                    }

                    assertEquals(ErrorType.INVALID_TOKEN, exception.error)
                }

                @DisplayName("VR-3.8.2")
                @Test
                fun vr3_8_02() {
                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = pmd,
                        owner = "UNKNOWN",
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )
                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
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
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
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
                        val tenderProcessEntity =
                            TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
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
                        .setAttribute("amount", BigDecimal(1))
                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = pmd,
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )
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

                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = pmd,
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )
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
                        val contractPeriodStartDate: LocalDateTime = requestNode.getObject("tender").let { tender ->
                            val arrayLots = tender.getArray("lots") {
                                assertEquals(2, this.size())
                            }

                            val lot1StartDate = arrayLots.getObject(0).let { lot1 ->
                                lot1.getObject("contractPeriod")
                                    .getString("startDate")
                                    .asText()
                                    .let {
                                        LocalDateTime.parse(it, JsonDateTimeFormatter.formatter)
                                    }
                            }
                            val lot2StartDate = arrayLots.getObject(1).let { lot2 ->
                                lot2.getObject("contractPeriod")
                                    .getString("startDate")
                                    .asText()
                                    .let {
                                        LocalDateTime.parse(it, JsonDateTimeFormatter.formatter)
                                    }
                            }

                            val minStartDate = if (lot1StartDate.isBefore(lot2StartDate))
                                lot1StartDate
                            else
                                lot2StartDate

                            minStartDate.minusDays(1)
                        }

                        pnWithoutItems.getObject("planning", "budget")
                            .getArray("budgetBreakdown")
                            .getObject(0)
                            .getObject("period")
                            .putAttribute("endDate", contractPeriodStartDate.format(JsonDateTimeFormatter.formatter))

                        val tenderProcessEntity =
                            TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
                        val exception = assertThrows<ErrorException> {
                            service.checkCnOnPn(cm)
                        }

                        assertEquals(ErrorType.INVALID_LOT_CONTRACT_PERIOD, exception.error)
                    }

                    @DisplayName("check end date")
                    @Test
                    fun endDate() {
                        val contractPeriodEndDate: LocalDateTime = requestNode.getObject("tender").let { tender ->
                            val arrayLots = tender.getArray("lots") {
                                assertEquals(2, this.size())
                            }

                            val lot1EndDate = arrayLots.getObject(0).let { lot1 ->
                                lot1.getObject("contractPeriod")
                                    .getString("endDate")
                                    .asText()
                                    .let {
                                        LocalDateTime.parse(it, JsonDateTimeFormatter.formatter)
                                    }
                            }
                            val lot2EndDate = arrayLots.getObject(1).let { lot2 ->
                                lot2.getObject("contractPeriod")
                                    .getString("endDate")
                                    .asText()
                                    .let {
                                        LocalDateTime.parse(it, JsonDateTimeFormatter.formatter)
                                    }
                            }

                            val maxEndDate = if (lot1EndDate.isAfter(lot2EndDate))
                                lot1EndDate
                            else
                                lot2EndDate

                            maxEndDate.plusDays(1)
                        }

                        pnWithoutItems.getObject("planning", "budget")
                            .getArray("budgetBreakdown")
                            .getObject(0)
                            .getObject("period") {
                                putAttribute("startDate", contractPeriodEndDate.format(JsonDateTimeFormatter.formatter))
                                putAttribute(
                                    "endDate",
                                    contractPeriodEndDate.plusYears(1).format(JsonDateTimeFormatter.formatter)
                                )
                            }

                        val tenderProcessEntity =
                            TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
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

                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = pmd,
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )
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

                        val tenderProcessEntity =
                            TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
                        val exception = assertThrows<ErrorException> {
                            service.checkCnOnPn(cm)
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
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            startDate = startDate,
                            data = requestNode
                        )
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
                            setAttribute("quantity", 0)
                        }
                    }

                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = pmd,
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )
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

                        val tenderProcessEntity =
                            TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
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

                        val tenderProcessEntity =
                            TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
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
                    val newItem = items.getObject(0).deepCopy {
                        putAttribute("relatedLot", "UNKNOWN")
                    }
                    items.add(newItem)

                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = pmd,
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )
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

                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = pmd,
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )
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

                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = pmd,
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )
                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
                    }

                    assertEquals(ErrorType.ITEM_ID_IS_DUPLICATED, exception.error)
                }

                @Nested
                @DisplayName("VR-3.8.14(CN on PN)")
                inner class VR3_8_14 {
                    @DisplayName("Checks auction required")
                    @Test
                    fun vr3_8_14_0() {
                        requestNode.getObject("tender")
                            .putObject("electronicAuctions").putArray("details")

                        val tenderProcessEntity =
                            TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                        whenever(
                            tenderProcessDao.getByCpIdAndStage(
                                eq(TestDataGenerator.CPID), eq(
                                    TestDataGenerator.PREV_STAGE
                                )
                            )
                        )
                            .thenReturn(tenderProcessEntity)

                        val cm = TestDataGenerator.commandMessage(
                            pmd = pmd,
                            command = COMMAND_TYPE,
                            operationType = OPERATION_TYPE,
                            data = requestNode
                        )
                        val exception = assertThrows<ErrorException> {
                            service.checkCnOnPn(cm)
                        }

                        assertEquals(ErrorType.INVALID_AUCTION_IS_NON_EMPTY, exception.error)
                    }
                }

                @DisplayName("VR-3.8.18 Check error when tender is unsuccessful status.")
                @Test
                fun vr_3_8_18() {
                    pnWithoutItems.getObject("tender")
                        .setAttribute("status", TenderStatus.UNSUCCESSFUL.value)

                    val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = pnWithoutItems.toString())
                    whenever(
                        tenderProcessDao.getByCpIdAndStage(
                            eq(TestDataGenerator.CPID), eq(
                                TestDataGenerator.PREV_STAGE
                            )
                        )
                    )
                        .thenReturn(tenderProcessEntity)

                    val cm = TestDataGenerator.commandMessage(
                        pmd = ProcurementMethod.OT.name,
                        command = COMMAND_TYPE,
                        operationType = OPERATION_TYPE,
                        data = requestNode
                    )

                    val exception = assertThrows<ErrorException> {
                        service.checkCnOnPn(cm)
                    }

                    assertEquals(ErrorType.TENDER_IN_UNSUCCESSFUL_STATUS, exception.error)
                }
            }
        }
    }

    @DisplayName("Create Endpoint")
    @Nested
    inner class Create {
        private val COMMAND_TYPE = CommandType.CREATE_CN_ON_PN

        @DisplayName("Check pmd in command.")
        @Test
        fun checkPMD() {
            val cm = TestDataGenerator.commandMessage(
                pmd = "UNKNOWN",
                command = COMMAND_TYPE,
                operationType = OPERATION_TYPE,
                data = NullNode.instance
            )
            val exception = assertThrows<ErrorException> {
                service.checkCnOnPn(cm)
            }

            assertEquals(ErrorType.INVALID_PMD, exception.error)
        }

        @DisplayName("Check error when tender by cpid and prev stage not found.")
        @Test
        fun tenderNotFound() {
            val data =
                loadJson("json/dto/create/cn_on_pn/request/op/with_auctions/with_documents/request_cn_with_auctions_with_documents_on_pn_full.json").toNode()
            val cm = TestDataGenerator.commandMessage(
                pmd = ProcurementMethod.OT.name,
                command = COMMAND_TYPE,
                operationType = OPERATION_TYPE,
                data = data
            )

            whenever(
                tenderProcessDao.getByCpIdAndStage(
                    eq(TestDataGenerator.CPID), eq(
                        TestDataGenerator.PREV_STAGE
                    )
                )
            )
                .thenReturn(null)

            val exception = assertThrows<ErrorException> {
                service.checkCnOnPn(cm)
            }

            assertEquals(ErrorType.DATA_NOT_FOUND, exception.error)
        }

        @Nested
        inner class OP {
            private val pmd: ProcurementMethod = ProcurementMethod.OT

            @Nested
            inner class WithAuctions {
                private val hasAuctionsInRequest = true

                @Nested
                inner class WithItems {
                    private val hasItemsInPN = true

                    @Nested
                    inner class WithDocuments {
                        private val hasDocumentsInPN = true

                        @ParameterizedTest(name = "{index} => auctions are required: {0}")
                        @CsvSource(value = ["true", "false"])
                        fun test(isAuctionRequired: Boolean) {
                            val testData = WhenTestData(
                                pmd = pmd,
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
                                pmd = pmd,
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
                inner class WithoutItems {
                    private val hasItemsInPN = false

                    @Nested
                    inner class WithDocuments {
                        private val hasDocumentsInPN = true

                        @ParameterizedTest(name = "{index} => auctions are required: {0}")
                        @CsvSource(value = ["true", "false"])
                        fun test(isAuctionRequired: Boolean) {
                            val testData = WhenTestData(
                                pmd = pmd,
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
                                pmd = pmd,
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
                inner class WithItems {
                    private val hasItemsInPN = true

                    @Nested
                    inner class WithDocuments {
                        private val hasDocumentsInPN = true

                        @ParameterizedTest(name = "{index} => auctions are required: {0}")
                        @CsvSource(value = ["true", "false"])
                        fun test(isAuctionRequired: Boolean) {
                            val testData = WhenTestData(
                                pmd = pmd,
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
                                pmd = pmd,
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
                inner class WithoutItems {
                    private val hasItemsInPN = false

                    @Nested
                    inner class WithDocuments {
                        private val hasDocumentsInPN = true

                        @ParameterizedTest(name = "{index} => auctions are required: {0}")
                        @CsvSource(value = ["true", "false"])
                        fun test(isAuctionRequired: Boolean) {
                            val testData = WhenTestData(
                                pmd = pmd,
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
                                pmd = pmd,
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
        }

        @Nested
        inner class LP {
            private val pmd: ProcurementMethod = ProcurementMethod.DA

            @Nested
            inner class WithoutAuctions {
                private val hasAuctionsInRequest = false

                @Nested
                inner class WithItems {
                    private val hasItemsInPN = true

                    @Nested
                    inner class WithDocuments {
                        private val hasDocumentsInPN = true

                        @ParameterizedTest(name = "{index} => auctions are required: {0}")
                        @CsvSource(value = ["false"])
                        fun test(isAuctionRequired: Boolean) {
                            val testData = WhenTestData(
                                pmd = pmd,
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
                        @CsvSource(value = ["false"])
                        fun test(isAuctionRequired: Boolean) {
                            val testData = WhenTestData(
                                pmd = pmd,
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
                inner class WithoutItems {
                    private val hasItemsInPN = false

                    @Nested
                    inner class WithDocuments {
                        private val hasDocumentsInPN = true

                        @ParameterizedTest(name = "{index} => auctions are required: {0}")
                        @CsvSource(value = ["false"])
                        fun test(isAuctionRequired: Boolean) {
                            val testData = WhenTestData(
                                pmd = pmd,
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
                        @CsvSource(value = ["false"])
                        fun test(isAuctionRequired: Boolean) {
                            val testData = WhenTestData(
                                pmd = pmd,
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
        }
    }

    private fun testOfCreate(
        testData: WhenTestData
    ) {
        val fileRequest = testData.requestJsonFile()
        val filePN = testData.pnJsonFile()
        val fileResponse = testData.responseJsonFile()

        val data = loadJson(fileRequest).toNode()
        val cm = TestDataGenerator.commandMessage(
            pmd = testData.pmd.name,
            command = CommandType.CREATE_CN_ON_PN,
            operationType = OPERATION_TYPE,
            data = data
        )

        val tenderProcessEntity = TestDataGenerator.tenderProcessEntity(data = loadJson(filePN))

        whenever(generationService.generatePermanentLotId())
            .thenReturn(PERMANENT_LOT_ID_1, PERMANENT_LOT_ID_2)
        whenever(generationService.generatePermanentItemId())
            .thenReturn(PERMANENT_ITEM_ID_1, PERMANENT_ITEM_ID_2, PERMANENT_ITEM_ID_3, PERMANENT_ITEM_ID_4)
        whenever(rulesService.isAuctionRequired(any(), any(), any()))
            .thenReturn(testData.isAuctionRequired)
        whenever(
            tenderProcessDao.getByCpIdAndStage(
                eq(TestDataGenerator.CPID), eq(
                    TestDataGenerator.PREV_STAGE
                )
            )
        )
            .thenReturn(tenderProcessEntity)

        val actualJson = service.createCnOnPn(cm).data.toJson()

        val expectedJson = loadJson(fileResponse)

        JsonValidator.equalsJsons(expectedJson, actualJson) {
            assert("$['tender']['lots'][0]['status']", LOT_STATUS)
            assert("$['tender']['lots'][1]['status']", LOT_STATUS)
            assert("$['tender']['lots'][0]['statusDetails']", LOT_STATUS_DETAILS)
            assert("$['tender']['lots'][1]['statusDetails']", LOT_STATUS_DETAILS)
        }
    }

    class WhenTestData(
        val pmd: ProcurementMethod,
        val isAuctionRequired: Boolean,
        val hasAuctionsInRequest: Boolean,
        val hasItemsInPN: Boolean,
        val hasDocumentsInPN: Boolean
    ) {

        fun requestJsonFile() = JsonFilePathGenerator.CnOnPn.request(
            pmd = pmd,
            hasAuctions = hasAuctionsInRequest,
            hasDocuments = true
        )

        fun pnJsonFile() = JsonFilePathGenerator.Entites.pn(
            hasItems = hasItemsInPN,
            hasDocuments = hasDocumentsInPN
        )

        fun responseJsonFile() = JsonFilePathGenerator.CnOnPn.response(
            pmd = pmd,
            hasAuctions = hasAuctionsInRequest,
            hasItems = hasItemsInPN,
            hasDocuments = hasDocumentsInPN
        )
    }
}

