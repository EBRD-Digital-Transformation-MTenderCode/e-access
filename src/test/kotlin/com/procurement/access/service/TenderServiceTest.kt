package com.procurement.access.service

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.access.application.model.params.GetCurrencyParams
import com.procurement.access.application.model.params.GetMainProcurementCategoryParams
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.MainProcurementCategory
import com.procurement.access.failure
import com.procurement.access.infrastructure.generator.TenderProcessEntityGenerator
import com.procurement.access.infrastructure.handler.get.currency.GetCurrencyResult
import com.procurement.access.infrastructure.handler.get.tender.procurement.GetMainProcurementCategoryResult
import com.procurement.access.json.loadJson
import com.procurement.access.lib.functional.Result
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TenderServiceTest {

    private lateinit var tenderProcessDao: TenderProcessDao
    private lateinit var tenderProcessRepository: TenderProcessRepository
    private lateinit var generationService: GenerationService
    private lateinit var tenderService: TenderService

    companion object {
        private val CPID = Cpid.tryCreateOrNull("ocds-t1s2t3-MD-1565251033096")!!
        private val OCID = Ocid.tryCreateOrNull("ocds-b3wdp1-MD-1581509539187-EV-1581509653044")!!
    }

    @BeforeEach
    fun init() {
        tenderProcessDao = mock()
        tenderProcessRepository = mock()
        generationService = mock()
        tenderService = TenderService(tenderProcessDao, generationService, tenderProcessRepository)
    }

    @Nested
    inner class GetCurrency {

        @Test
        fun getCurrency_success() {
            val params = getParams()
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/currency/get/tender_currency.json"))
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(Result.success(tenderProcessEntity))
            val actual = tenderService.getCurrency(params).get

            val expected = GetCurrencyResult(
                GetCurrencyResult.Tender(GetCurrencyResult.Tender.Value("tenderCurrency"))
            )

            Assertions.assertEquals(expected, actual)
        }

        @Test
        fun recordNotFound_fail() {
            val params = getParams()
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(Result.success(null))
            val actual = tenderService.getCurrency(params).failure()

            val expectedErrorCode = "VR.COM-1.34.1"
            val expectedErrorMessage = "Tender not found by cpid='${params.cpid}' and ocid='${params.ocid}'."

            Assertions.assertEquals(expectedErrorCode, actual.code)
            Assertions.assertEquals(expectedErrorMessage, actual.description)
        }

        private fun getParams() = GetCurrencyParams.tryCreate(
            cpid = CPID.toString(), ocid = OCID.toString()
        ).get
    }


    @Nested
    inner class GetMainProcurementCategory {

        @Test
        fun getCategory_success() {
            val params = getParams()
            val tenderCategoryEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/procurement/category/get/tender_main_procurement_category.json"))
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(Result.success(tenderCategoryEntity))
            val actual = tenderService.getMainProcurementCategory(params).get

            val expected = GetMainProcurementCategoryResult(
                GetMainProcurementCategoryResult.Tender(MainProcurementCategory.GOODS)
            )

            Assertions.assertEquals(expected, actual)
        }

        @Test
        fun recordNotFound_fail() {
            val params = getParams()
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(Result.success(null))
            val actual = tenderService.getMainProcurementCategory(params).failure()

            val expectedErrorCode = "VR.COM-1.37.1"
            val expectedErrorMessage = "Tender not found by cpid='${params.cpid}' and ocid='${params.ocid}'."

            Assertions.assertEquals(expectedErrorCode, actual.code)
            Assertions.assertEquals(expectedErrorMessage, actual.description)
        }

        private fun getParams() = GetMainProcurementCategoryParams(
            cpid = CPID, ocid = OCID
        )
    }
}