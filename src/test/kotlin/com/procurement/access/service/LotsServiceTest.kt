package com.procurement.access.service

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.access.application.model.params.GetLotsValueParams
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.infrastructure.generator.TenderProcessEntityGenerator
import com.procurement.access.infrastructure.handler.v1.model.response.GetLotsValueResult
import com.procurement.access.json.loadJson
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.Result.Companion.success
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.*

internal class LotsServiceTest {

    companion object {
        private val CPID = Cpid.tryCreateOrNull("ocds-t1s2t3-MD-1565251033096")!!
        private val OCID = Ocid.tryCreateOrNull("ocds-b3wdp1-MD-1581509539187-EV-1581509653044")!!
    }

    private lateinit var lotsService: LotsService
    private lateinit var tenderProcessDao: TenderProcessDao
    private lateinit var tenderProcessRepository: TenderProcessRepository

    @BeforeEach
    fun init() {
        tenderProcessDao = mock()
        tenderProcessRepository = mock()
        lotsService = LotsService(tenderProcessDao, tenderProcessRepository)
    }

    @Nested
    inner class GetLotsValue {

        @Test
        fun success() {
            val params = getLotsValueParams()
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/lots/value/tender_lots_value.json"))
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.getLotsValue(params)

            val expectedResult = GetLotsValueResult(
                GetLotsValueResult.Tender(
                    listOf(
                        GetLotsValueResult.Tender.Lot(
                            id = "801d927c-1477-4aca-b545-b5afce3326e6",
                            value = GetLotsValueResult.Tender.Lot.Value(
                                amount = BigDecimal(0).setScale(2),
                                currency = "currency"
                            )
                        ),
                        GetLotsValueResult.Tender.Lot(
                            id = "3456a5dc-e8c1-48eb-88f7-e03c5161fb0f",
                            value = GetLotsValueResult.Tender.Lot.Value(
                                amount = BigDecimal(1).setScale(2),
                                currency = "currency"
                            )
                        )
                    )
                )
            )
            assertEquals(expectedResult, actual.get)
        }

        @Test
        fun tenderNotFound_fail() {
            val params = getLotsValueParams()
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(null))
            val actual = lotsService.getLotsValue(params)
            val expectedErrorCode = "VR.COM-1.38.1"
            val expectedErrorMessage = "Tender not found by cpid='${params.cpid}' and ocid='${params.ocid}'."

            val failure = (actual as Result.Failure).reason
            assertEquals(expectedErrorCode, failure.code)
            assertEquals(expectedErrorMessage, failure.description)
        }

        private fun getLotsValueParams(): GetLotsValueParams {
            return GetLotsValueParams(
                cpid = CPID, ocid = OCID, tender = GetLotsValueParams.Tender(
                    listOf(
                        GetLotsValueParams.Tender.Lot(id = UUID.fromString("801d927c-1477-4aca-b545-b5afce3326e6")),
                        GetLotsValueParams.Tender.Lot(id = UUID.fromString("3456a5dc-e8c1-48eb-88f7-e03c5161fb0f"))
                    )
                )
            )
        }

        @Test
        fun someLotsNotFound_fail() {
            val params = getParamsWithOneUnknownLot()
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/lots/value/tender_lots_value.json"))
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.getLotsValue(params)
            val expectedErrorCode = "VR.COM-1.38.2"
            val expectedErrorMessage = "Lot(s) by id(s) '${params.tender.lots.first().id}' not found."

            val failure = (actual as Result.Failure).reason
            assertEquals(expectedErrorCode, failure.code)
            assertEquals(expectedErrorMessage, failure.description)
        }

        private fun getParamsWithOneUnknownLot(): GetLotsValueParams {
            return GetLotsValueParams(
                cpid = CPID, ocid = OCID, tender = GetLotsValueParams.Tender(
                    listOf(
                        GetLotsValueParams.Tender.Lot(id = UUID.randomUUID()),
                        GetLotsValueParams.Tender.Lot(id = UUID.fromString("3456a5dc-e8c1-48eb-88f7-e03c5161fb0f"))
                    )
                )
            )
        }
    }
}