package com.procurement.access.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.access.application.model.MainMode
import com.procurement.access.application.service.pn.create.CreatePnContext
import com.procurement.access.application.service.pn.create.PnCreateData
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.enums.ProcurementMethod
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.dto.pn.PnCreateRequest
import com.procurement.access.infrastructure.dto.pn.PnCreateResponse
import com.procurement.access.infrastructure.dto.pn.converter.convert
import com.procurement.access.infrastructure.generator.CommandMessageGenerator
import com.procurement.access.infrastructure.generator.ContextGenerator
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
import com.procurement.access.model.dto.bpe.country
import com.procurement.access.model.dto.bpe.owner
import com.procurement.access.model.dto.bpe.pmd
import com.procurement.access.model.dto.bpe.stage
import com.procurement.access.model.dto.bpe.startDate
import com.procurement.access.model.dto.databinding.JsonDateTimeFormatter
import com.procurement.access.utils.toObject
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.time.LocalDateTime
import java.util.*

class PnServiceTest {
    companion object {
        private const val PERMANENT_CPID = "ocds-t1s2t3-MD-1552650554287"
        private const val PERMANENT_TENDER_PROCURING_ENTITY_ID_1 = "procuring-entity-id-1"

        private const val PERMANENT_LOT_ID_1 = "permanent-lot-1"
        private const val PERMANENT_LOT_ID_2 = "permanent-lot-2"
        private const val PERMANENT_LOT_ID_3 = "permanent-lot-3"

        private const val PERMANENT_ITEM_ID_1 = "permanent-item-1"
        private const val PERMANENT_ITEM_ID_2 = "permanent-item-2"
        private const val PERMANENT_ITEM_ID_3 = "permanent-item-3"
        private const val PERMANENT_ITEM_ID_4 = "permanent-item-4"

        private const val TENDER_ID = "ocds-t1s2t3-MD-1552650554287"

        private val PATTERN_MODE = "ocds-t1s2t3".toRegex()
        private val OCDS_PREFIX = "ocds-t1s2t3"
    }

    private lateinit var generationService: GenerationService
    private lateinit var tenderProcessDao: TenderProcessDao

    private lateinit var service: PnService

    @BeforeEach
    fun init() {
        generationService = mock()
        tenderProcessDao = mock()

        service = PnService(generationService, tenderProcessDao)

        whenever(generationService.generateOrganizationId(any(), any()))
            .thenReturn(PERMANENT_TENDER_PROCURING_ENTITY_ID_1)

        whenever(generationService.getCpId(eq(ContextGenerator.COUNTRY), any()))
            .thenReturn(PERMANENT_CPID)

        whenever(generationService.generateToken())
            .thenReturn(ContextGenerator.TOKEN)

        whenever(generationService.generatePermanentLotId())
            .thenReturn(PERMANENT_LOT_ID_1, PERMANENT_LOT_ID_2, PERMANENT_LOT_ID_3)

        whenever(generationService.generatePermanentItemId())
            .thenReturn(PERMANENT_ITEM_ID_1, PERMANENT_ITEM_ID_2, PERMANENT_ITEM_ID_3, PERMANENT_ITEM_ID_4)

        whenever(generationService.generatePermanentTenderId())
            .thenReturn(TENDER_ID)
    }

    @Nested
    inner class ValidationRules {
        private lateinit var requestNode: ObjectNode

        @BeforeEach
        fun prepare() {
            requestNode =
                loadJson("json/service/create/pn/request/request_pn_with_items_with_documents.json").toNode() as ObjectNode
        }

        @DisplayName("VR-3.1.6 Tender Period: Start Date")
        @ParameterizedTest
        @EnumSource(ProcurementMethod::class)
        fun vr_3_1_06(pmd: ProcurementMethod) {
            requestNode.getObject("tender", "tenderPeriod")
                .setAttribute("startDate", "2019-02-10T01:01:01Z")

            val cm = commandMessage(pmd = pmd.name, data = requestNode)
            val payload = getCreatePnPayload(cm)
            val exception = assertThrows<ErrorException> {
                service.createPn(payload.context, payload.data)
            }

            assertEquals(ErrorType.INVALID_START_DATE, exception.error)
        }

        @Nested
        inner class WithItems {

            @DisplayName("VR-3.1.4 Tender Value")
            @ParameterizedTest
            @EnumSource(ProcurementMethod::class)
            fun vr_3_1_04(pmd: ProcurementMethod) {
                requestNode.getObject("planning", "budget", "amount")
                    .setAttribute("amount", 1.01)

                val cm = commandMessage(pmd = pmd.name, data = requestNode)
                val payload = getCreatePnPayload(cm)
                val exception = assertThrows<ErrorException> {
                    service.createPn(payload.context, payload.data)
                }

                assertEquals(ErrorType.INVALID_TENDER_AMOUNT, exception.error)
            }

            @DisplayName("VR-3.1.7 Currency (lot)")
            @ParameterizedTest
            @EnumSource(ProcurementMethod::class)
            fun vr_3_1_07(pmd: ProcurementMethod) {
                requestNode.getObject("planning", "budget", "amount")
                    .setAttribute("currency", "UNKNOWN")

                val cm = commandMessage(pmd = pmd.name, data = requestNode)
                val payload = getCreatePnPayload(cm)
                val exception = assertThrows<ErrorException> {
                    service.createPn(payload.context, payload.data)
                }

                assertEquals(ErrorType.INVALID_LOT_CURRENCY, exception.error)
            }

/*
        TODO разкомментировать после того как будет переписан секрви си десериализатор

        @DisplayName("VR-3.1.8 Quantity (item)")
        @ParameterizedTest
        @EnumSource(ProcurementMethod::class)
        fun vr_3_1_8(pmd: ProcurementMethod) {
            val l = requestNode.getObject("tender")
                .getArray("items")
                .getObject(0)
                .setAttribute("quantity", 0.0)

            val json = requestNode.toJson()
            val cm = commandMessage(
                pmd = pmd.key,

                data = json.toNode()
            )

            val exception = assertThrows<ErrorException> {
                service.createPn(cm)
            }

            assertEquals(ErrorType.INVALID_ITEMS_QUANTITY, exception.error)
        }
*/

            @Nested
            @DisplayName("VR-3.1.9 Contract Period (Tender)")
            inner class VR3_1_09 {

                @DisplayName("check start date")
                @ParameterizedTest
                @EnumSource(ProcurementMethod::class)
                fun startDate(pmd: ProcurementMethod) {
                    val minStartDateOfContractPeriod: LocalDateTime = requestNode.getObject("tender")
                        .let { tender ->
                            tender.getArray("lots").asSequence()
                                .map { lot ->
                                    lot.getObject("contractPeriod").getString("startDate").asText()
                                }
                                .map {
                                    LocalDateTime.parse(it, JsonDateTimeFormatter.formatter)
                                }
                                .min()!!
                        }
                    val budgetBreakdownPeriodEndDate = minStartDateOfContractPeriod.minusDays(1)
                    requestNode.getObject("planning", "budget")
                        .getArray("budgetBreakdown")
                        .getObject(0)
                        .getObject("period")
                        .putAttribute("endDate", budgetBreakdownPeriodEndDate.format(JsonDateTimeFormatter.formatter))

                    val cm = commandMessage(pmd = pmd.name, data = requestNode)
                    val payload = getCreatePnPayload(cm)
                    val exception = assertThrows<ErrorException> {
                        service.createPn(payload.context, payload.data)
                    }

                    assertEquals(ErrorType.INVALID_LOT_CONTRACT_PERIOD, exception.error)
                }

                @DisplayName("check end date")
                @ParameterizedTest
                @EnumSource(ProcurementMethod::class)
                fun endDate(pmd: ProcurementMethod) {
                    val maxStartDateOfContractPeriod: LocalDateTime = requestNode.getObject("tender").let { tender ->
                        tender.getArray("lots").asSequence()
                            .map { lot ->
                                lot.getObject("contractPeriod").getString("endDate").asText()
                            }
                            .map {
                                LocalDateTime.parse(it, JsonDateTimeFormatter.formatter)
                            }
                            .max()!!
                    }
                    val budgetBreakdownPeriodStartDate = maxStartDateOfContractPeriod.plusDays(1)
                    val budgetBreakdownPeriodEndDate = maxStartDateOfContractPeriod.plusDays(10)
                    requestNode.getObject("planning", "budget")
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

                    val cm = commandMessage(pmd = pmd.name, data = requestNode)
                    val payload = getCreatePnPayload(cm)
                    val exception = assertThrows<ErrorException> {
                        service.createPn(payload.context, payload.data)
                    }

                    assertEquals(ErrorType.INVALID_LOT_CONTRACT_PERIOD, exception.error)
                }
            }

            @DisplayName("VR-3.1.10 Related Lots (documents)")
            @ParameterizedTest
            @EnumSource(ProcurementMethod::class)
            fun vr3_1_10(pmd: ProcurementMethod) {
                requestNode.getObject("tender").getArray("documents") {
                    getObject(0) {
                        getArray("relatedLots")
                            .add("UNKNOWN")
                    }
                }

                val cm = commandMessage(pmd = pmd.name, data = requestNode)
                val payload = getCreatePnPayload(cm)
                val exception = assertThrows<ErrorException> {
                    service.createPn(payload.context, payload.data)
                }

                assertEquals(ErrorType.INVALID_DOCS_RELATED_LOTS, exception.error)
            }

            @Nested
            @DisplayName("VR-3.1.11 Contract Period (Lot)")
            inner class VR3_1_11 {

                @DisplayName("Contract Period: Start Date < Contract Period: End Date")
                @ParameterizedTest
                @EnumSource(ProcurementMethod::class)
                fun vr3_1_11_1(pmd: ProcurementMethod) {
                    requestNode.getObject("tender").getArray("lots") {
                        getObject(0) {
                            val contractPeriod = getObject("contractPeriod")
                            val startDate = contractPeriod.getString("startDate").asText()
                            val endDate = contractPeriod.getString("endDate").asText()

                            contractPeriod.setAttribute("startDate", endDate)
                            contractPeriod.setAttribute("endDate", startDate)
                        }
                    }

                    val cm = commandMessage(pmd = pmd.name, data = requestNode)
                    val payload = getCreatePnPayload(cm)
                    val exception = assertThrows<ErrorException> {
                        service.createPn(payload.context, payload.data)
                    }

                    assertEquals(ErrorType.INVALID_LOT_CONTRACT_PERIOD, exception.error)
                }

                @DisplayName("Tender Period: Start Date < Contract Period: Start Date")
                @ParameterizedTest
                @EnumSource(ProcurementMethod::class)
                fun vr3_1_11_2(pmd: ProcurementMethod) {
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

                    val tenderPeriodEndDate = minStartDateOfContractPeriod.plusMonths(1).let {
                        val day = 1
                        val month = it.month
                        val year = it.year
                        LocalDateTime.of(year, month, day, 10, 15, 20)
                    }

                    requestNode.getObject("tender", "tenderPeriod")
                        .setAttribute("startDate", tenderPeriodEndDate.format(JsonDateTimeFormatter.formatter))

                    val cm = commandMessage(pmd = pmd.name, data = requestNode)
                    val payload = getCreatePnPayload(cm)
                    val exception = assertThrows<ErrorException> {
                        service.createPn(payload.context, payload.data)
                    }

                    assertEquals(ErrorType.INVALID_LOT_CONTRACT_PERIOD, exception.error)
                }
            }

            @Nested
            @DisplayName("VR-3.1.12 Lots")
            inner class VR3_1_12 {

                @DisplayName("The set lots are not empty.")
                @ParameterizedTest
                @EnumSource(ProcurementMethod::class)
                fun vr3_1_12_1(pmd: ProcurementMethod) {
                    requestNode.getObject("tender").putArray("lots")

                    val cm = commandMessage(pmd = pmd.name, data = requestNode)
                    println(requestNode)

                    val exception = assertThrows<ErrorException> {
                        val payload = getCreatePnPayload(cm)
                        service.createPn(payload.context, payload.data)
                    }

                    assertEquals(ErrorType.IS_EMPTY, exception.error)
            }

                @DisplayName("The lots ids are presented in a list of values the relatedLot of the items.")
                @ParameterizedTest
                @EnumSource(ProcurementMethod::class)
                fun vr3_1_12_2(pmd: ProcurementMethod) {
                    requestNode.getObject("tender").getArray("lots")
                        .getObject(0)
                        .setAttribute("id", UUID.randomUUID().toString())

                    val cm = commandMessage(pmd = pmd.name, data = requestNode)
                    val payload = getCreatePnPayload(cm)
                    val exception = assertThrows<ErrorException> {
                        service.createPn(payload.context, payload.data)
                    }

                    assertEquals(ErrorType.LOT_ID_NOT_MATCH_TO_RELATED_LOT_IN_ITEMS, exception.error)
                }
            }

            @DisplayName("VR-3.1.13 Items")
            @ParameterizedTest
            @EnumSource(ProcurementMethod::class)
            fun vr3_1_13(pmd: ProcurementMethod) {
                val items = requestNode.getObject("tender").getArray("items")
                val item = items.getObject(0).deepCopy {
                    setAttribute("id", UUID.randomUUID().toString())
                    setAttribute("relatedLot", UUID.randomUUID().toString())
                }
                items.putObject(item)

                val cm = commandMessage(pmd = pmd.name, data = requestNode)
                val payload = getCreatePnPayload(cm)
                val exception = assertThrows<ErrorException> {
                    service.createPn(payload.context, payload.data)
                }

                assertEquals(ErrorType.INVALID_ITEMS_RELATED_LOTS, exception.error)
            }

            @DisplayName("VR-3.1.14 The set of lots is unique by id.")
            @ParameterizedTest
            @EnumSource(ProcurementMethod::class)
            fun vr3_1_14(pmd: ProcurementMethod) {
                requestNode.getObject("tender")
                    .getArray("lots") {
                        val id = UUID.randomUUID().toString()
                        getObject(0).setAttribute("id", id)
                        getObject(1).setAttribute("id", id)
                    }
                val cm = commandMessage(pmd = pmd.name, data = requestNode)
                val payload = getCreatePnPayload(cm)
                val exception = assertThrows<ErrorException> {
                    service.createPn(payload.context, payload.data)
                }

                assertEquals(ErrorType.LOT_ID_DUPLICATED, exception.error)
            }

            @DisplayName("VR-3.1.15 The set of items is unique by id.")
            @ParameterizedTest
            @EnumSource(ProcurementMethod::class)
            fun vr3_1_15(pmd: ProcurementMethod) {
                requestNode.getObject("tender")
                    .getArray("items") {
                        val id = UUID.randomUUID().toString()
                        getObject(0).setAttribute("id", id)
                        getObject(1).setAttribute("id", id)
                    }
                val cm = commandMessage(pmd = pmd.name, data = requestNode)
                val payload = getCreatePnPayload(cm)
                val exception = assertThrows<ErrorException> {
                    service.createPn(payload.context, payload.data)
                }

                assertEquals(ErrorType.ITEM_ID_DUPLICATED, exception.error)
            }
        }
    }

    @Nested
    inner class BusinessRules {

        @Nested
        inner class WithItems {
            private val hasItems = true

            @Nested
            inner class WithDocuments {
                private val hasDocuments = true

                @ParameterizedTest
                @EnumSource(ProcurementMethod::class)
                fun test(pmd: ProcurementMethod) {
                    val testData = WhenTestData(
                        hasItemsInRequest = hasItems,
                        hasDocumentsInRequest = hasDocuments
                    )
                    testOfCreate(pmd = pmd, testData = testData)
                }
            }

            @Nested
            inner class WithoutDocuments {
                private val hasDocuments = false

                @ParameterizedTest
                @EnumSource(ProcurementMethod::class)
                fun test(pmd: ProcurementMethod) {
                    val testData = WhenTestData(
                        hasItemsInRequest = hasItems,
                        hasDocumentsInRequest = hasDocuments
                    )
                    testOfCreate(pmd = pmd, testData = testData)
                }
            }
        }

        @Nested
        inner class WithoutItems {
            private val hasItems = false

            @Nested
            inner class WithDocuments {
                private val hasDocuments = true

                @ParameterizedTest
                @EnumSource(ProcurementMethod::class)
                fun test(pmd: ProcurementMethod) {
                    val testData = WhenTestData(
                        hasItemsInRequest = hasItems,
                        hasDocumentsInRequest = hasDocuments
                    )
                    testOfCreate(pmd = pmd, testData = testData)
                }
            }

            @Nested
            inner class WithoutDocuments {
                private val hasDocuments = false

                @ParameterizedTest
                @EnumSource(ProcurementMethod::class)
                fun test(pmd: ProcurementMethod) {
                    val testData = WhenTestData(
                        hasItemsInRequest = hasItems,
                        hasDocumentsInRequest = hasDocuments
                    )
                    testOfCreate(pmd = pmd, testData = testData)
                }
            }
        }

        @Nested
        inner class CheckDucumentsRelationWithLot {

            @Test
            fun missingLot() {
                val (lot1, lot2) = listOf("1", "2")
                val (doc1, doc2, doc3) = listOf("doc1", "doc2", "doc3")
                val receivedLots = setOf(lot1, lot2)
                val docsWithRelatedLot = mapOf(
                    doc1 to listOf(lot1),
                    doc2 to listOf(lot2),
                    doc3 to listOf("UNKNOWN")
                )

                val exception = assertThrows<ErrorException> {
                    service.checkDocumentsRelationWithLot(docsWithRelatedLot, receivedLots)
                }

                assertEquals(ErrorType.INVALID_DOCS_RELATED_LOTS, exception.error)
            }

            @Test
            fun allLotsReceived() {
                val (lot1, lot2) = listOf("1", "2")
                val (doc1, doc2) = listOf("doc1", "doc2")
                val receivedLots = setOf(lot1, lot2)
                val docsWithRelatedLot = mapOf(
                    doc1 to listOf(lot1),
                    doc2 to listOf(lot2)
                )

                assertDoesNotThrow { service.checkDocumentsRelationWithLot(docsWithRelatedLot, receivedLots) }
            }

        }

        private fun testOfCreate(
            pmd: ProcurementMethod,
            testData: WhenTestData
        ) {
            val pathToJsonFileOfRequest = testData.requestJsonFile()
            val pathToJsonFileOfResponse = testData.responseJsonFile()

            val data = loadJson(pathToJsonFileOfRequest).toNode()
            val cm = commandMessage(pmd = pmd.name, data = data)

            val payload = getCreatePnPayload(cm)

            val actualJson = service.createPn(payload.context, payload.data).convert().toJson()

            val expectedJson = loadJson(pathToJsonFileOfResponse).let { json ->
                val node = json.toNode()
                node.getObject("tender")
                    .setAttribute("procurementMethod", pmd.key)
                node.toJson()
            }

            JsonValidator.equalsJsons(expectedJson, actualJson)
        }
    }

    fun commandMessage(
        owner: String = ContextGenerator.OWNER,
        pmd: String,
        startDate: String = ContextGenerator.START_DATE,
        data: JsonNode
    ): CommandMessage {
        val context = ContextGenerator.generate(
            owner = owner,
            pmd = pmd,
            phase = "planning",
            startDate = startDate,
            operationId = "124124"
        )
        return CommandMessageGenerator.generate(
            command = CommandType.CREATE_PN,
            context = context,
            data = data
        )
    }

    class WhenTestData(
        val hasItemsInRequest: Boolean,
        val hasDocumentsInRequest: Boolean
    ) {

        fun requestJsonFile(): String {
            val itemsSegment = JsonFilePathGenerator.itemsSegment(hasItemsInRequest)
            val segmentDocuments = JsonFilePathGenerator.documentsSegment(hasDocumentsInRequest)
            return "json/service/create/pn/request/request_pn_${itemsSegment}_${segmentDocuments}.json"
                .also {
                    testingBindingAndMapping<PnCreateRequest>(it)
                }
        }

        fun responseJsonFile(): String {
            val itemsSegment = JsonFilePathGenerator.itemsSegment(hasItemsInRequest)
            val segmentDocuments = JsonFilePathGenerator.documentsSegment(hasDocumentsInRequest)
            return "json/service/create/pn/response/response_pn_${itemsSegment}_${segmentDocuments}.json"
                .also {
                    testingBindingAndMapping<PnCreateResponse>(it)
                }
        }
    }

    private data class CreatePnPayload(
        val context: CreatePnContext,
        val data: PnCreateData
    )

    private fun getCreatePnPayload(cm: CommandMessage): CreatePnPayload {
        val context = CreatePnContext(
            stage = cm.stage,
            owner = cm.owner,
            pmd = cm.pmd,
            country = cm.country,
            startDate = cm.startDate,
            mode = MainMode(prefix = OCDS_PREFIX, pattern = PATTERN_MODE)
        )
        val request: PnCreateRequest = toObject(PnCreateRequest::class.java, cm.data)
        val data: PnCreateData = request.convert()
        return CreatePnPayload(context = context, data = data)
    }
}
