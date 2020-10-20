package com.procurement.access.service.validation

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.access.application.model.params.CheckEqualityCurrenciesParams
import com.procurement.access.application.model.params.ValidateClassificationParams
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.util.Result.Companion.success
import com.procurement.access.domain.util.ValidationResult
import com.procurement.access.infrastructure.generator.TenderProcessEntityGenerator
import com.procurement.access.json.loadJson
import com.procurement.access.service.RulesService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*

class ValidationServiceTest {

    private lateinit var tenderProcessDao: TenderProcessDao
    private lateinit var tenderProcessRepository: TenderProcessRepository
    private lateinit var rulesService: RulesService
    private lateinit var validationService: ValidationService

    companion object{
        private val CPID = Cpid.tryCreateOrNull("ocds-t1s2t3-MD-1565251033096")!!
        private val OCID = Ocid.tryCreateOrNull("ocds-b3wdp1-MD-1581509539187-EV-1581509653044")!!
        private val RELATED_CPID = Cpid.tryCreateOrNull("ocds-t1s2t3-OT-1565251033097")!!
        private val RELATED_OCID = Ocid.tryCreateOrNull("ocds-b3wdp1-MD-1581509539187-FE-1581509653045")!!
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
        fun currencyMatches_success(){
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

            val actual =  validationService.checkEqualityCurrencies(params = getParams()).error

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

            val actual =  validationService.checkEqualityCurrencies(params = getParams()).error

            val expectedErrorCode = "VR.COM-1.33.2"
            val expectedErrorMessage = "Tender not found by cpid='${params.relatedCpid}' and ocid='${params.relatedOcid}'."

            assertEquals(expectedErrorCode, actual.code)
            assertEquals(expectedErrorMessage, actual.description)
        }


        @Test
        fun currencyDoesNotMatch_fail(){
            val params = getParams()

            val entityJson = loadJson("json/service/check/currency/currency.json")
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = entityJson)
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(tenderProcessEntity))

            val relatedEntityJson = loadJson("json/service/check/currency/unmatching_currency.json")
            val relatedTenderProcessEntity = TenderProcessEntityGenerator.generate(data = relatedEntityJson)
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.relatedCpid, stage = params.relatedOcid.stage))
                .thenReturn(success(relatedTenderProcessEntity))

            val actual =  validationService.checkEqualityCurrencies(params = getParams()).error

            val expectedErrorCode = "VR.COM-1.33.3"
            val expectedErrorMessage = "Tenders' currencies do not match."

            assertEquals(expectedErrorCode, actual.code)
            assertEquals(expectedErrorMessage, actual.description)
        }

        private fun getParams() = CheckEqualityCurrenciesParams.tryCreate(
            cpid = CPID.toString(),
            ocid = OCID.toString(),
            relatedCpid = RELATED_CPID.toString(),
            relatedOcid = RELATED_OCID.toString()
        ).get
    }

    @Nested
    inner class ValidateClassification{

        @Test
        fun recordNotFound_fail(){
            val params = getParams(UUID.randomUUID().toString())

            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(null))

            val actual =  validationService.validateClassification(params).error

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

            val actual =  validationService.validateClassification(params).error

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
            cpid = CPID.toString(),
            ocid = OCID.toString(),
            tender = ValidateClassificationParams.Tender(ValidateClassificationParams.Tender.Classification(id = classificationId))
        ).get
    }


}