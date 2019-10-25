package com.procurement.access.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.nhaarman.mockito_kotlin.clearInvocations
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.access.application.service.CreateCnOnPnContext
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.procurementMethod.ProcurementMethod
import com.procurement.access.infrastructure.dto.cn.CnOnPnRequest
import com.procurement.access.infrastructure.dto.cn.CnOnPnResponse
import com.procurement.access.infrastructure.generator.ContextGenerator
import com.procurement.access.infrastructure.generator.TenderProcessEntityGenerator
import com.procurement.access.json.getObject
import com.procurement.access.json.loadJson
import com.procurement.access.json.toNode
import com.procurement.access.json.toObject
import com.procurement.access.model.dto.databinding.toLocalDateTime
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CnOnPnServiceBLTest {

    companion object {
        private const val PATH_PN_JSON =
            "json/service/create/cn_on_pn/entity/pn/entity_pn_with_items_with_documents.json"
    }

    private lateinit var cnOnPnService: CnOnPnService
    private val generationService: GenerationService = mock()
    private val tenderProcessDao: TenderProcessDao = mock()
    private val rulesService: RulesService = mock()

    @BeforeAll
    fun init() {
        cnOnPnService = CnOnPnService(generationService, tenderProcessDao, rulesService)
    }

    @AfterEach
    fun clear() {
        clearInvocations(generationService, tenderProcessDao, rulesService)
    }

    @Nested
    inner class CreateCnOnPn {

        @Test
        fun `correct tempId to permanentId replacement`() {
            val firstLotIdExcepted = "101"
            val secondLotIdExcepted = "102"

            val firstItemIdExcepted = "111"
            val secondItemIdExcepted = "112"

            whenever(generationService.generatePermanentLotId())
                .thenReturn(firstLotIdExcepted)
                .thenReturn(secondLotIdExcepted)

            whenever(generationService.generatePermanentItemId())
                .thenReturn(firstItemIdExcepted)
                .thenReturn(secondItemIdExcepted)

            val pnWithoutItems = (loadJson(PATH_PN_JSON).toNode() as ObjectNode).apply {
                getObject("tender") {
                    putArray("lots")
                    putArray("items")
                }
            }

            mockGetByCpIdAndStage(
                cpid = ContextGenerator.CPID,
                stage = ContextGenerator.PREV_STAGE,
                data = pnWithoutItems
            )

            val data: CnOnPnRequest =
                loadJson("json/dto/create/cn_on_pn/op/request/request_cn_on_pn_full.json").toObject()
            val context = createContext()

            val response: CnOnPnResponse = cnOnPnService.createCnOnPn(context = context, data = data)

            assertEquals(firstLotIdExcepted, response.tender.lots[0].id)
            assertEquals(secondLotIdExcepted, response.tender.lots[1].id)

            assertEquals(firstItemIdExcepted, response.tender.items[0].id)
            assertEquals(secondItemIdExcepted, response.tender.items[1].id)

            assertEquals(secondLotIdExcepted, response.tender.criteria!![1].relatedItem)

            assertEquals(firstItemIdExcepted, response.tender.criteria!![2].relatedItem)
            assertEquals(secondItemIdExcepted, response.tender.criteria!![3].relatedItem)

            verify(generationService, times(2))
                .generatePermanentLotId()
            verify(generationService, times(2))
                .generatePermanentItemId()
        }
    }

    private fun mockGetByCpIdAndStage(cpid: String, stage: String, data: JsonNode) {
        val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = data.toString())
        whenever(tenderProcessDao.getByCpIdAndStage(eq(cpid), eq(stage)))
            .thenReturn(tenderProcessEntity)
    }
}

fun createContext(
    startDate: String = ContextGenerator.START_DATE
): CreateCnOnPnContext = CreateCnOnPnContext(
    cpid = ContextGenerator.CPID,
    previousStage = ContextGenerator.PREV_STAGE,
    stage = ContextGenerator.STAGE,
    country = ContextGenerator.COUNTRY,
    pmd = ProcurementMethod.SV,
    startDate = startDate.toLocalDateTime()
)
