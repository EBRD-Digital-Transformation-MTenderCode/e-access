package com.procurement.access.service

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.access.application.model.params.GetBuyersOwnersParams
import com.procurement.access.application.model.params.GetCurrencyParams
import com.procurement.access.application.model.params.GetMainProcurementCategoryParams
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.application.service.Transform
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.MainProcurementCategory
import com.procurement.access.domain.model.enums.RelatedProcessType
import com.procurement.access.domain.model.process.RelatedProcessId
import com.procurement.access.domain.model.process.RelatedProcessIdentifier
import com.procurement.access.failure
import com.procurement.access.infrastructure.entity.APEntity
import com.procurement.access.infrastructure.entity.FEEntity
import com.procurement.access.infrastructure.entity.PNEntity
import com.procurement.access.infrastructure.entity.process.RelatedProcess
import com.procurement.access.infrastructure.generator.TenderProcessEntityGenerator
import com.procurement.access.infrastructure.handler.v2.model.response.GetBuyersOwnersResult
import com.procurement.access.infrastructure.handler.v2.model.response.GetCurrencyResult
import com.procurement.access.infrastructure.handler.v2.model.response.GetMainProcurementCategoryResult
import com.procurement.access.json.loadJson
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asSuccess
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TenderServiceTest {

    private lateinit var tenderProcessDao: TenderProcessDao
    private lateinit var tenderProcessRepository: TenderProcessRepository
    private lateinit var generationService: GenerationService
    private lateinit var transform: Transform
    private lateinit var tenderService: TenderService

    companion object {
        private val CPID = Cpid.tryCreateOrNull("ocds-t1s2t3-MD-1565251033096")!!
        private val OCID = Ocid.SingleStage.tryCreateOrNull("ocds-b3wdp1-MD-1581509539187-EV-1581509653044")!!
    }

    @BeforeEach
    fun init() {
        tenderProcessDao = mock()
        tenderProcessRepository = mock()
        generationService = mock()
        transform = mock()
        tenderService = TenderService(tenderProcessDao, generationService,  tenderProcessRepository, transform)
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
            cpid = CPID.value, ocid = OCID.value
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

    @Nested
    inner class GetBuyersOwners{
        @Test
        fun success() {
            val params = getParams()
            val stubTenderEntity = TenderProcessEntityGenerator.generate(data = "")
            val acOcid = Ocid.SingleStage.tryCreateOrNull("ocds-b3wdp2-MD-1581509539187-AC-1581509653044")!!
            val pnOcid = Ocid.SingleStage.tryCreateOrNull("ocds-b3wdp3-MD-1581509539187-PN-1581509653044")!!

            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(Result.success(stubTenderEntity))
            whenever(transform.tryDeserialization("", FEEntity::class.java))
                .thenReturn(getFeEntity(acOcid).asSuccess())
            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = acOcid))
                .thenReturn(Result.success(stubTenderEntity))
            whenever(transform.tryDeserialization("", APEntity::class.java))
                .thenReturn(getApEntity(pnOcid).asSuccess())
            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = Cpid.tryCreateOrNull("ocds-b3wdp3-MD-1581509539187")!!, ocid = pnOcid))
                .thenReturn(Result.success(stubTenderEntity))
            whenever(transform.tryDeserialization("", PNEntity::class.java)).thenReturn(getPnEntity())

            val actual = tenderService.getBuyersOwners(params).get

            val expected = GetBuyersOwnersResult(
                listOf(
                    GetBuyersOwnersResult.Buyer(
                    id = "buyer.id",
                    name = "buyer.name",
                    owner = stubTenderEntity.owner
                ))
            )

            assertEquals(expected, actual)
        }

        @Test
        fun feNotFound_fail() {
            val params = getParams()
            val acOcid = Ocid.SingleStage.tryCreateOrNull("ocds-b3wdp2-MD-1581509539187-AC-1581509653044")!!

            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(Result.success(null))
            whenever(transform.tryDeserialization("", FEEntity::class.java))
                .thenReturn(getFeEntity(acOcid).asSuccess())

            val actual = tenderService.getBuyersOwners(params) as Result.Failure
            val errorCode = "VR.COM-1.48.1"
            val errorMessage = "FE record not found by cpid='$CPID' and ocid='$OCID'."

            assertEquals(errorCode, actual.reason.code)
            assertEquals(errorMessage, actual.reason.description)
        }

        @Test
        fun aggregatePlanningNotFound_fail() {
            val params = getParams()
            val stubTenderEntity = TenderProcessEntityGenerator.generate(data = "")
            val acOcid = Ocid.SingleStage.tryCreateOrNull("ocds-b3wdp2-MD-1581509539187-AC-1581509653044")!!

            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(Result.success(stubTenderEntity))
            whenever(transform.tryDeserialization("", FEEntity::class.java))
                .thenReturn(getFeEntity(acOcid).copy(relatedProcesses = emptyList()).asSuccess())

            val actual = tenderService.getBuyersOwners(params) as Result.Failure
            val errorCode = "VR.COM-1.48.2"
            val errorMessage = "Relationship '${RelatedProcessType.AGGREGATE_PLANNING}' not found."

            assertEquals(errorCode, actual.reason.code)
            assertEquals(errorMessage, actual.reason.description)
        }


        @Test
        fun apNotFound_fail() {
            val params = getParams()
            val stubTenderEntity = TenderProcessEntityGenerator.generate(data = "")
            val acOcid = Ocid.SingleStage.tryCreateOrNull("ocds-b3wdp2-MD-1581509539187-AC-1581509653044")!!

            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(Result.success(stubTenderEntity))
            whenever(transform.tryDeserialization("", FEEntity::class.java))
                .thenReturn(getFeEntity(acOcid).asSuccess())
            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = acOcid))
                .thenReturn(Result.success(null))

            val actual = tenderService.getBuyersOwners(params) as Result.Failure
            val errorCode = "VR.COM-1.48.3"
            val errorMessage = "AP record not found by cpid='$CPID' and ocid='$acOcid'."

            assertEquals(errorCode, actual.reason.code)
            assertEquals(errorMessage, actual.reason.description)
        }

        @Test
        fun x_scopeNotFound_fail() {
            val params = getParams()
            val stubTenderEntity = TenderProcessEntityGenerator.generate(data = "")
            val acOcid = Ocid.SingleStage.tryCreateOrNull("ocds-b3wdp2-MD-1581509539187-AC-1581509653044")!!
            val pnOcid = Ocid.SingleStage.tryCreateOrNull("ocds-b3wdp3-MD-1581509539187-PN-1581509653044")!!

            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(Result.success(stubTenderEntity))
            whenever(transform.tryDeserialization("", FEEntity::class.java))
                .thenReturn(getFeEntity(acOcid).asSuccess())
            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = acOcid))
                .thenReturn(Result.success(stubTenderEntity))
            whenever(transform.tryDeserialization("", APEntity::class.java))
                .thenReturn(getApEntity(pnOcid).copy(relatedProcesses = emptyList()).asSuccess())


            val actual = tenderService.getBuyersOwners(params) as Result.Failure
            val errorCode = "VR.COM-1.48.4"
            val errorMessage = "Relationship '${RelatedProcessType.X_SCOPE}' not found."

            assertEquals(errorCode, actual.reason.code)
            assertEquals(errorMessage, actual.reason.description)
        }

        @Test
        fun pnNotFound_fail() {
            val params = getParams()
            val stubTenderEntity = TenderProcessEntityGenerator.generate(data = "")
            val acOcid = Ocid.SingleStage.tryCreateOrNull("ocds-b3wdp2-MD-1581509539187-AC-1581509653044")!!
            val pnOcid = Ocid.SingleStage.tryCreateOrNull("ocds-b3wdp3-MD-1581509539187-PN-1581509653044")!!

            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(Result.success(stubTenderEntity))
            whenever(transform.tryDeserialization("", FEEntity::class.java))
                .thenReturn(getFeEntity(acOcid).asSuccess())
            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = acOcid))
                .thenReturn(Result.success(stubTenderEntity))
            whenever(transform.tryDeserialization("", APEntity::class.java))
                .thenReturn(getApEntity(pnOcid).asSuccess())
            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = Cpid.tryCreateOrNull("ocds-b3wdp3-MD-1581509539187")!!, ocid = pnOcid))
                .thenReturn(Result.success(null))

            val actual = tenderService.getBuyersOwners(params) as Result.Failure
            val errorCode = "VR.COM-1.48.5"
            val errorMessage = "PN record not found by cpid='ocds-b3wdp3-MD-1581509539187' and ocid='$pnOcid'."

            assertEquals(errorCode, actual.reason.code)
            assertEquals(errorMessage, actual.reason.description)
        }

        private fun getPnEntity(): Result<PNEntity, Fail.Incident.Transform.Deserialization> =
            PNEntity(
                ocid = "",
                tender = mock(),
                relatedProcesses = mock(),
                planning = mock(),
                buyer = PNEntity.Buyer(
                    id = "buyer.id",
                    name = "buyer.name",
                    identifier = mock(),
                    details = mock(),
                    contactPoint = mock(),
                    address = mock(),
                    additionalIdentifiers = mock()
                )
            ).asSuccess()

        private fun getApEntity(pnOcid: Ocid.SingleStage) =
            APEntity(
                ocid = "",
                parties = mock(),
                tender = mock(),
                relatedProcesses = listOf(
                    RelatedProcess(
                        id = RelatedProcessId.randomUUID(),
                        uri = "",
                        scheme = mock(),
                        relationship = listOf(RelatedProcessType.X_SCOPE),
                        identifier = RelatedProcessIdentifier.create(pnOcid.value)
                    )
                )
            )

        private fun getFeEntity(acOcid: Ocid.SingleStage) =
            FEEntity(
                ocid = "",
                parties = mock(),
                token = "",
                tender = mock(),
                relatedProcesses = listOf(
                    RelatedProcess(
                        id = RelatedProcessId.randomUUID(),
                        uri = "",
                        scheme = mock(),
                        relationship = listOf(RelatedProcessType.AGGREGATE_PLANNING),
                        identifier = RelatedProcessIdentifier.create(acOcid.value)
                    )
                )
            )

        fun getParams() = GetBuyersOwnersParams(
            cpid = CPID,
            ocid = OCID
        )
    }

}