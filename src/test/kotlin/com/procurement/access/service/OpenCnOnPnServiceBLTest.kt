package com.procurement.access.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.nhaarman.mockito_kotlin.clearInvocations
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.access.application.service.CreateOpenCnOnPnContext
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.ProcurementMethod
import com.procurement.access.domain.util.extension.toLocalDateTime
import com.procurement.access.infrastructure.generator.ContextGenerator
import com.procurement.access.infrastructure.generator.TenderProcessEntityGenerator
import com.procurement.access.infrastructure.handler.v1.model.request.OpenCnOnPnRequest
import com.procurement.access.infrastructure.handler.v1.model.response.OpenCnOnPnResponse
import com.procurement.access.json.getObject
import com.procurement.access.json.loadJson
import com.procurement.access.json.toNode
import com.procurement.access.json.toObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OpenCnOnPnServiceBLTest {

    companion object {
        private const val PATH_PN_JSON =
            "json/service/create/cn_on_pn/entity/pn/entity_pn_with_items_with_documents.json"
    }

    private lateinit var cnOnPnService: OpenCnOnPnService
    private val generationService: GenerationService = mock()
    private val tenderProcessDao: TenderProcessDao = mock()
    private val rulesService: RulesService = mock()

    @BeforeAll
    fun init() {
        cnOnPnService = OpenCnOnPnService(generationService, tenderProcessDao, rulesService)
    }

    @AfterEach
    fun clear() {
        clearInvocations(generationService, tenderProcessDao, rulesService)
    }

    @Nested
    inner class CreateCnOnPn {

        @Test
        fun `correct tempId to permanentId replacement`() {
            val PERMANENT_AUCTION_ID_1 = "1"
            val PERMANENT_AUCTION_ID_2 = "2"

            val firstLotIdExcepted = "101"
            val secondLotIdExcepted = "102"

            val firstItemIdExcepted = "111"
            val secondItemIdExcepted = "112"

            val tenderId = "ocds-t1s2t3-MD-1552650554287"

            whenever(generationService.generatePermanentTenderId())
                .thenReturn(tenderId)

            whenever(generationService.generatePermanentLotId())
                .thenReturn(firstLotIdExcepted)
                .thenReturn(secondLotIdExcepted)

            whenever(generationService.generatePermanentItemId())
                .thenReturn(firstItemIdExcepted)
                .thenReturn(secondItemIdExcepted)

            whenever(generationService.generatePermanentAuctionId())
                .thenReturn(PERMANENT_AUCTION_ID_1, PERMANENT_AUCTION_ID_2)

            val context = createContext()
            val ocid = Ocid.SingleStage.tryCreateOrNull(ContextGenerator.OCID)!!
            whenever(generationService.generateOcid(cpid = context.cpid, stage = context.stage))
                .thenReturn(ocid)

            val pnWithoutItems = (loadJson(PATH_PN_JSON).toNode() as ObjectNode).apply {
                getObject("tender") {
                    putArray("lots")
                    putArray("items")
                }
            }

            mockGetByCpIdAndStage(
                cpid = ContextGenerator.CPID,
                ocid = ContextGenerator.OCID,
                data = pnWithoutItems
            )

            val data: OpenCnOnPnRequest =
                loadJson("json/dto/create/cn_on_pn/open/request/request_open_cn_on_pn_full.json").toObject()

            val response: OpenCnOnPnResponse = cnOnPnService.create(context = context, data = data)

            assertEquals(firstLotIdExcepted, response.tender.lots[0].id)
            assertEquals(secondLotIdExcepted, response.tender.lots[1].id)

            assertEquals(firstItemIdExcepted, response.tender.items[0].id)
            assertEquals(secondItemIdExcepted, response.tender.items[1].id)

            assertEquals(secondLotIdExcepted, response.tender.criteria!![1].relatedItem)

            assertEquals(firstItemIdExcepted, response.tender.criteria!![2].relatedItem)
            assertEquals(secondItemIdExcepted, response.tender.criteria!![3].relatedItem)

            assertEquals(ocid.value, response.ocid)

            verify(generationService, times(2))
                .generatePermanentLotId()
            verify(generationService, times(2))
                .generatePermanentItemId()
            verify(generationService, times(1))
                .generatePermanentTenderId()
        }
    }

    private fun mockGetByCpIdAndStage(cpid: String, ocid: String, data: JsonNode) {
        val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = data.toString())
        whenever(tenderProcessDao.getByCpidAndOcid(eq(cpid), eq(ocid)))
            .thenReturn(tenderProcessEntity)
    }
}

fun createContext(
    startDate: String = ContextGenerator.START_DATE
): CreateOpenCnOnPnContext = CreateOpenCnOnPnContext(
    cpid = ContextGenerator.CPID,
    ocid = ContextGenerator.OCID,
    stage = ContextGenerator.STAGE,
    country = ContextGenerator.COUNTRY,
    pmd = ProcurementMethod.SV,
    startDate = startDate.toLocalDateTime().orThrow { it.reason }
)
