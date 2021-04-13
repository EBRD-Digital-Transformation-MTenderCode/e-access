package com.procurement.access.service.validation

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.access.application.model.params.CheckEqualityCurrenciesParams
import com.procurement.access.application.model.params.CheckExistenceSignAuctionParams
import com.procurement.access.application.model.params.ValidateClassificationParams
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.ProcurementMethodModalities
import com.procurement.access.failure
import com.procurement.access.infrastructure.generator.TenderProcessEntityGenerator
import com.procurement.access.json.loadJson
import com.procurement.access.lib.functional.Result.Companion.success
import com.procurement.access.lib.functional.ValidationResult
import com.procurement.access.service.RulesService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.util.*

class ValidationServiceTest {

    private lateinit var tenderProcessDao: TenderProcessDao
    private lateinit var tenderProcessRepository: TenderProcessRepository
    private lateinit var rulesService: RulesService
    private lateinit var validationService: ValidationService

    companion object {
        private val CPID = Cpid.tryCreateOrNull("ocds-t1s2t3-MD-1565251033096")!!
        private val OCID = Ocid.SingleStage.tryCreateOrNull("ocds-b3wdp1-MD-1581509539187-EV-1581509653044")!!
        private val RELATED_CPID = Cpid.tryCreateOrNull("ocds-t1s2t3-OT-1565251033097")!!
        private val RELATED_OCID = Ocid.SingleStage.tryCreateOrNull("ocds-b3wdp1-MD-1581509539187-FE-1581509653045")!!
        private val VALID_CLASSIFICATION_ID = "7a6cd0c8-006d-474b-8ab3-7a2a6cb9aa22"
        private val STORED_CLASSIFICATION_ID = "7a6b670f-858b-4bed-8f19-dc5886b03ca4"
    }

    @BeforeEach
    fun init() {
        tenderProcessDao = mock()
        tenderProcessRepository = mock()
        rulesService = mock()
        validationService = ValidationService(tenderProcessDao, tenderProcessRepository, rulesService)
    }

    @Nested
    inner class CheckEqualityCurrencies {

        @Test
        fun currencyMatches_success() {
            val params = getParams()

            val entityJson = loadJson("json/service/check/currency/currency.json")
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = entityJson)
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(tenderProcessEntity))

            val relatedEntityJson = loadJson("json/service/check/currency/currency.json")
            val relatedTenderProcessEntity = TenderProcessEntityGenerator.generate(data = relatedEntityJson)
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.relatedCpid, stage = params.relatedOcid.stage))
                .thenReturn(success(relatedTenderProcessEntity))

            val actual =  validationService.checkEqualityCurrencies(params = getParams())

            assertTrue(actual is ValidationResult.Ok)
        }

        @Test
        fun recordNotFound_fail(){
            val params = getParams()

            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(null))

            val actual = validationService.checkEqualityCurrencies(params = getParams()).failure()

            val expectedErrorCode = "VR.COM-1.33.1"
            val expectedErrorMessage = "Tender not found by cpid='${params.cpid}' and ocid='${params.ocid}'."

            assertEquals(expectedErrorCode, actual.code)
            assertEquals(expectedErrorMessage, actual.description)
        }

        @Test
        fun relatedRecordNotFound_fail(){
            val params = getParams()

            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = "")
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(tenderProcessEntity))

            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.relatedCpid, stage = params.relatedOcid.stage))
                .thenReturn(success(null))

            val actual = validationService.checkEqualityCurrencies(params = getParams()).failure()

            val expectedErrorCode = "VR.COM-1.33.2"
            val expectedErrorMessage = "Tender not found by cpid='${params.relatedCpid}' and ocid='${params.relatedOcid}'."

            assertEquals(expectedErrorCode, actual.code)
            assertEquals(expectedErrorMessage, actual.description)
        }

        @Test
        fun currencyDoesNotMatch_fail() {
            val params = getParams()

            val entityJson = loadJson("json/service/check/currency/currency.json")
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = entityJson)
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(tenderProcessEntity))

            val relatedEntityJson = loadJson("json/service/check/currency/unmatching_currency.json")
            val relatedTenderProcessEntity = TenderProcessEntityGenerator.generate(data = relatedEntityJson)
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.relatedCpid, stage = params.relatedOcid.stage))
                .thenReturn(success(relatedTenderProcessEntity))

            val actual = validationService.checkEqualityCurrencies(params = getParams()).failure()

            val expectedErrorCode = "VR.COM-1.33.3"
            val expectedErrorMessage = "Tenders' currencies do not match."

            assertEquals(expectedErrorCode, actual.code)
            assertEquals(expectedErrorMessage, actual.description)
        }

        private fun getParams() = CheckEqualityCurrenciesParams.tryCreate(
            cpid = CPID.value,
            ocid = OCID.value,
            relatedCpid = RELATED_CPID.value,
            relatedOcid = RELATED_OCID.value
        ).get
    }

    @Nested
    inner class CheckExistenceSignAuction {

        @ParameterizedTest
        @CsvSource(
            "tender_entity_without_electronic_auction.json",
            "tender_entity_without_procurement_method_modalities.json"
        )
        fun electronicAuctionReceivedButNotStored_fail(jsonPath: String) {
            val params = getParams(ProcurementMethodModalities.ELECTRONIC_AUCTION)
            val entity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/check/auction/$jsonPath"))
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(entity))

            val actual = validationService.checkExistenceSignAuction(params).failure()

            val expectedErrorCode = "VR.COM-1.32.2"
            val expectedErrorMessage = "Stored tender must contain 'electronicAuction' in procurementMethodModalities."

            assertEquals(expectedErrorCode, actual.code)
            assertEquals(expectedErrorMessage, actual.description)
        }

        @ParameterizedTest
        @CsvSource(
            "tender_entity_with_electronic_auction.json",
            "tender_entity_with_electronic_auction_and_requires_electronic_catalogue.json"
        )
        fun electronicAuctionReceivedAndStored_success(jsonPath: String) {
            val params = getParams(ProcurementMethodModalities.ELECTRONIC_AUCTION)
            val entity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/check/auction/$jsonPath"))
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(entity))

            val actual = validationService.checkExistenceSignAuction(params)

            assertTrue(actual is ValidationResult.Ok)
        }

        @ParameterizedTest
        @CsvSource(
            "tender_entity_with_electronic_auction.json",
            "tender_entity_with_electronic_auction_and_requires_electronic_catalogue.json"
        )
        fun electronicAuctionNotReceivedButStored_fail(jsonPath: String) {
            val params = getParams(ProcurementMethodModalities.REQUIRES_ELECTRONIC_CATALOGUE)
            val entity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/check/auction/$jsonPath"))
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(entity))

            val actual = validationService.checkExistenceSignAuction(params).failure()

            val expectedErrorCode = "VR.COM-1.32.3"
            val expectedErrorMessage = "Stored tender must not contain 'electronicAuction' in procurementMethodModalities."

            assertEquals(expectedErrorCode, actual.code)
            assertEquals(expectedErrorMessage, actual.description)
        }

        @ParameterizedTest
        @CsvSource(
            "tender_entity_without_electronic_auction.json",
            "tender_entity_without_procurement_method_modalities.json"
        )
        fun electronicAuctionNotReceivedAndNotStored_success(jsonPath: String) {
            val params = getParams(ProcurementMethodModalities.REQUIRES_ELECTRONIC_CATALOGUE)
            val entity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/check/auction/$jsonPath"))
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(entity))

            val actual = validationService.checkExistenceSignAuction(params)

            assertTrue(actual is ValidationResult.Ok)
        }

        @Test
        fun tenderNotFound_fail() {
            val params = getParams(ProcurementMethodModalities.ELECTRONIC_AUCTION)
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(null))
            val actual = validationService.checkExistenceSignAuction(params).failure()

            val expectedErrorCode = "VR.COM-1.32.1"
            val expectedErrorMessage = "Tender not found by cpid='${params.cpid}' and ocid='${params.ocid}'."

            assertEquals(expectedErrorCode, actual.code)
            assertEquals(expectedErrorMessage, actual.description)
        }

        private fun getParams(procurementMethodModality:ProcurementMethodModalities) = CheckExistenceSignAuctionParams.tryCreate(
            cpid = CPID.value,
            ocid = OCID.value,
            tender = CheckExistenceSignAuctionParams.Tender.tryCreate(
                procurementMethodModalities = listOf(procurementMethodModality.key)
            ).get
        ).get
    }

    @Nested
    inner class ValidateClassification{

        @Test
        fun recordNotFound_fail(){
            val params = getParams(UUID.randomUUID().toString())

            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(null))

            val actual = validationService.validateClassification(params).failure()

            val expectedErrorCode = "VR.COM-1.30.1"
            val expectedErrorMessage = "Tender not found by cpid='${params.cpid}' and ocid='${params.ocid}'."

            assertEquals(expectedErrorCode, actual.code)
            assertEquals(expectedErrorMessage, actual.description)
        }

        @Test
        fun classificationDoesNotMatch_fail(){
            val params = getParams(UUID.randomUUID().toString())

            val json = loadJson("json/service/validate/classification/tender_classification_entity.json")
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = json)

            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(tenderProcessEntity))

            val actual = validationService.validateClassification(params).failure()

            val expectedErrorCode = "VR.COM-1.30.2"
            val expectedErrorMessage = "First three symbols of received classification id '${params.tender.classification.id}' does not match stored one '$STORED_CLASSIFICATION_ID'."

            assertEquals(expectedErrorCode, actual.code)
            assertEquals(expectedErrorMessage, actual.description)
        }

        @Test
        fun classificationsMatch_success(){
            val params = getParams(VALID_CLASSIFICATION_ID)

            val json = loadJson("json/service/validate/classification/tender_classification_entity.json")
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = json)

            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(tenderProcessEntity))

            val actual =  validationService.validateClassification(params)

            assertTrue(actual is ValidationResult.Ok)
        }

        private fun getParams(classificationId: String) = ValidateClassificationParams.tryCreate(
            cpid = CPID.value,
            ocid = OCID.value,
            tender = ValidateClassificationParams.Tender(ValidateClassificationParams.Tender.Classification(id = classificationId))
        ).get
    }


}