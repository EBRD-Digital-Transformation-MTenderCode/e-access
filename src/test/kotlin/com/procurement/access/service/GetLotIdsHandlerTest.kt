package com.procurement.access.service

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.infrastructure.generator.TenderProcessEntityGenerator
import com.procurement.access.infrastructure.web.dto.ApiSuccessResponse
import com.procurement.access.infrastructure.web.dto.ApiVersion
import com.procurement.access.json.JSON
import com.procurement.access.json.loadJson
import com.procurement.access.json.toNode
import com.procurement.access.service.handler.GetLotIdsHandler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*

class GetLotIdsHandlerTest {

    companion object {
        private const val PATH_TO_REQUEST = "json/service/lots/getLotIds/get_lot_ids_full.json"
        private const val PATH_TO_TENDER_PROCESS = "json/service/lots/getLotIds/tender_process.json"
        private val ID = UUID.fromString("8a0b50c7-8ded-49d8-a785-d6f6ff5289fd")
        private val VERSION = ApiVersion(2,0,0)
    }

    private lateinit var tenderProcessDao: TenderProcessDao
    private lateinit var handler: GetLotIdsHandler

    @BeforeEach
    fun init() {
        tenderProcessDao = mock()
        handler = GetLotIdsHandler(tenderProcessDao = tenderProcessDao)
    }

    @DisplayName("Get Lot Ids Handler")
    @Nested
    inner class GetLotIds {
        private lateinit var request: JSON
        private lateinit var tenderProcess: JSON

        @BeforeEach
        fun prepare() {
            request = loadJson(PATH_TO_REQUEST)
            tenderProcess = loadJson(PATH_TO_TENDER_PROCESS)
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = tenderProcess)
            whenever(tenderProcessDao.getByCpIdAndStage(cpId = any(), stage = any()))
                .thenReturn(tenderProcessEntity)
        }

        @Test
        @DisplayName("Success result")
        fun success() {

            val request = request.toNode()

            val result = handler.handle(request = request)
            val output = createApiResponse(
                data = listOf(
                    LotId.fromString("577fd5c4-e314-40a9-aabe-732d7f5269ad"),
                    LotId.fromString("577fd5c4-e314-40a9-aabe-732d7f5269a3"),
                    LotId.fromString("577fd5c4-e314-40a9-aabe-732d7f5269a4"),
                    LotId.fromString("577fd5c4-e314-40a9-aabe-732d7f5269a5")
                )
            )
            assertEquals(output.id, result.id)
            assertEquals(output.result, result.result)
            assertEquals(output.version, result.version)
        }

        private fun createApiResponse(data: Any) = ApiSuccessResponse(
            id = ID,
            result = data,
            version = VERSION
        )
    }
}