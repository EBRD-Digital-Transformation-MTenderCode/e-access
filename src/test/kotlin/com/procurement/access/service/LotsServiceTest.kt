package com.procurement.access.service

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.infrastructure.dto.converter.convert
import com.procurement.access.infrastructure.dto.lot.GetLotIdsRequest
import com.procurement.access.infrastructure.generator.TenderProcessEntityGenerator
import com.procurement.access.json.JSON
import com.procurement.access.json.JsonValidator
import com.procurement.access.json.loadJson
import com.procurement.access.json.toJson
import com.procurement.access.utils.toObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LotsServiceTest {

    companion object {
        private const val PATH_TO_REQUEST = "json/service/lots/getLotIds/get_lot_ids_full.json"
        private const val PATH_TO_TENDER_PROCESS = "json/service/lots/getLotIds/tender_process.json"
        private const val PATH_TO_OUTPUT = "json/service/lots/getLotIds/expected_output.json"
    }

    private lateinit var tenderProcessDao: TenderProcessDao
    private lateinit var service: LotsService

    @BeforeEach
    fun init() {
        tenderProcessDao = mock()
        service = LotsService(tenderProcessDao = tenderProcessDao)
    }

    @DisplayName("Get Lot Ids")
    @Nested
    inner class GetLotIds {
        private lateinit var request: JSON
        private lateinit var tenderProcess: JSON
        private lateinit var output: JSON

        @BeforeEach
        fun prepare() {
            request = loadJson(PATH_TO_REQUEST)
            tenderProcess = loadJson(PATH_TO_TENDER_PROCESS)
            output = loadJson(PATH_TO_OUTPUT)
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = tenderProcess)
            whenever(tenderProcessDao.getByCpIdAndStage(cpId = any(), stage = any()))
                .thenReturn(tenderProcessEntity)
        }

        @Test
        @DisplayName("Success result")
        fun success() {

            val data = toObject(GetLotIdsRequest::class.java, request)

            val result = service.getLotIds(data = data.convert())

            JsonValidator.equalsJsons(expectedJson = output, actualJson = result.convert().toJson())
        }
    }
}