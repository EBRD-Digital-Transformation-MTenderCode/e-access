package com.procurement.access.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.util.extension.nowDefaultUTC
import com.procurement.access.infrastructure.api.v1.CommandTypeV1
import com.procurement.access.infrastructure.generator.CommandMessageGenerator
import com.procurement.access.infrastructure.generator.ContextGenerator
import com.procurement.access.json.JsonValidator
import com.procurement.access.json.getArray
import com.procurement.access.json.getObject
import com.procurement.access.json.loadJson
import com.procurement.access.json.putAttribute
import com.procurement.access.json.toJson
import com.procurement.access.json.toNode
import com.procurement.access.model.dto.bpe.CommandMessage
import com.procurement.access.model.entity.TenderProcessEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PnUpdateServiceTest {

    companion object {
        private const val CPID = "ocds-t1s2t3-MD-1579523524876"
        private const val STAGE = "PN"
        private const val PMD = "PMD"
        const val REQUEST_PATH = "json/service/update/pn/update_pn_full.json"
        const val MOCK_TENDER_PATH = "json/service/update/pn/tender_process.json"
        const val EXPECTED_JSON_PATH = "json/service/update/pn/expected_output.json"
    }

    private lateinit var generationService: GenerationService
    private lateinit var tenderProcessDao: TenderProcessDao
    private lateinit var service: PnUpdateService

    @BeforeEach
    fun init() {
        generationService = mock()
        tenderProcessDao = mock()
        service = PnUpdateService(generationService, tenderProcessDao)
    }

    @Nested
    inner class BusinessRules {
        private lateinit var requestNode: ObjectNode
        private lateinit var mockTenderProcess: ObjectNode

        @BeforeEach
        fun prepare() {
            requestNode = loadJson(REQUEST_PATH).toNode() as ObjectNode
            mockTenderProcess = loadJson(MOCK_TENDER_PATH).toNode() as ObjectNode
        }

        @DisplayName("test Of Update from request")
        @Test
        fun testOfUpdate() {
            getByCpidAndStage(tenderProcess = mockTenderProcess)
            val commandMessage = commandMessage(pmd = PMD, data = requestNode)
            val result = service.updatePn(cm = commandMessage)
            val actualJson = result.data!!.toJson()
            val expectedJson = loadJson(EXPECTED_JSON_PATH)
            JsonValidator.equalsJsons(expectedJson, actualJson)
        }

        @DisplayName("internal id from request - null, TP - not null")
        @Test
        fun internalIdTest() {

            mockTenderProcess.getObject("tender")
                .getArray("lots")
                .getObject(0)
                .putAttribute("internalId", "lot[0].internalId")

            mockTenderProcess.getObject("tender")
                .getArray("items")
                .getObject(0)
                .putAttribute("internalId", "item[0].internalId")

            getByCpidAndStage(tenderProcess = mockTenderProcess)

            requestNode.getObject("tender")
                .getArray("lots")
                .getObject(0)
                .remove("internalId")

            requestNode.getObject("tender")
                .getArray("items")
                .getObject(0)
                .remove("internalId")

            val commandMessage = commandMessage(pmd = PMD, data = requestNode)
            val result = service.updatePn(cm = commandMessage)
            val actualJson = result.data!!.toJson()
            val expectedJson = loadJson(EXPECTED_JSON_PATH).toNode()

            expectedJson.getObject("tender")
                .getArray("lots")
                .getObject(0)
                .putAttribute("internalId", "lot[0].internalId")

            expectedJson.getObject("tender")
                .getArray("items")
                .getObject(0)
                .putAttribute("internalId", "item[0].internalId")


            JsonValidator.equalsJsons(expectedJson = expectedJson.toJson(), actualJson = actualJson)
        }

        private fun getByCpidAndStage(tenderProcess: ObjectNode) {
            val entity = TenderProcessEntity(
                cpId = CPID,
                stage = STAGE,
                createdDate = nowDefaultUTC(),
                jsonData = tenderProcess.toJson(),
                owner = ContextGenerator.OWNER,
                token = ContextGenerator.TOKEN
            )
            whenever(tenderProcessDao.getByCpIdAndStage(cpId = any(), stage = any()))
                .thenReturn(entity)
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
            command = CommandTypeV1.UPDATE_PN,
            context = context,
            data = data
        )
    }
}
