package com.procurement.access.service

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.access.application.model.errors.ValidateRfqDataErrors
import com.procurement.access.application.model.params.ValidateRfqDataParams
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.application.service.Transform
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.ProcurementMethodModalities
import com.procurement.access.infrastructure.configuration.properties.UriProperties
import com.procurement.access.infrastructure.generator.TenderProcessEntityGenerator
import com.procurement.access.json.loadJson
import com.procurement.access.lib.functional.ValidationResult
import com.procurement.access.lib.functional.asSuccess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.util.*

internal class RfqServiceImplTest {
    companion object {
        private val CPID = Cpid.tryCreateOrNull("ocds-t1s2t3-MD-1565251033096")!!
        private val OCID = Ocid.SingleStage.tryCreateOrNull("ocds-b3wdp1-MD-1581509539187-EV-1581509653044")!!
        private val LOT_ID = UUID.fromString("dccd933c-10d1-463f-97f2-8966dfc211c8")
        private val ITEM_ID = "item_id"
        private val CURRENCY = "currency"

        private const val FORMAT_PATTERN = "uuuu-MM-dd'T'HH:mm:ss'Z'"
        private val FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(FORMAT_PATTERN)
            .withResolverStyle(ResolverStyle.STRICT)
        private val DATE = LocalDateTime.parse("2020-02-10T08:49:55Z", FORMATTER)
    }

    private lateinit var rfqService: RfqService
    private lateinit var tenderProcessRepository: TenderProcessRepository
    private lateinit var generationService: GenerationService
    private lateinit var uriProperties: UriProperties
    private lateinit var transform: Transform

    @BeforeEach
    fun init() {
        tenderProcessRepository = mock()
        generationService = mock()
        uriProperties = mock()
        transform = mock()

        rfqService = RfqServiceImpl(tenderProcessRepository, generationService, uriProperties, transform)
    }

    @Nested
    inner class ValidateRfqData {

        @Test
        fun success() {
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/rfq/entity_pn.json"))

            whenever(tenderProcessRepository.getByCpIdAndOcid(CPID, OCID)).thenReturn(tenderProcessEntity.asSuccess())
            val actual = rfqService.validateRfqData(getParams())

            assertTrue(actual is ValidationResult.Ok)
        }

        @Test
        fun twoLotsReceived_fail() {
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/rfq/entity_pn.json"))

            whenever(tenderProcessRepository.getByCpIdAndOcid(CPID, OCID)).thenReturn(tenderProcessEntity.asSuccess())
            val params = getParams()
            val paramsWithTwoLots = params.copy(tender = params.tender.copy(lots = params.tender.lots + params.tender.lots))
            val actual = rfqService.validateRfqData(paramsWithTwoLots) as ValidationResult.Error
            val errorCode = "VR.COM-46.1.1"
            val errorMessage = "Expected number of lots: 1. Actual: '${paramsWithTwoLots.tender.lots.size}'."

            assertEquals(errorCode, actual.reason.code)
            assertEquals(errorMessage, actual.reason.description)
        }

        @Test
        fun pnNotFound_fail() {
            whenever(tenderProcessRepository.getByCpIdAndOcid(CPID, OCID)).thenReturn(null.asSuccess())
            val actual = rfqService.validateRfqData(getParams()) as ValidationResult.Error
            val errorCode = "VR.COM-46.1.2"
            val errorMessage = "Pn record by cpid '$CPID' and ocid '$OCID' not found."

            assertEquals(errorCode, actual.reason.code)
            assertEquals(errorMessage, actual.reason.description)
        }

        @Test
        fun invalidCurrencyRecieved_fail() {
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/rfq/entity_pn.json"))

            whenever(tenderProcessRepository.getByCpIdAndOcid(CPID, OCID)).thenReturn(tenderProcessEntity.asSuccess())
            val params = getParams()
            val invalidCurrency = "invalidCurrency"
            val paramsWithInvalidCurrency = params.copy(
                tender = params.tender.copy(
                    lots = listOf(
                        params.tender.lots.first()
                            .copy(value = ValidateRfqDataParams.Tender.Lot.Value(invalidCurrency))
                    )
                )
            )
            val actual = rfqService.validateRfqData(paramsWithInvalidCurrency) as ValidationResult.Error
            val errorCode = "VR.COM-46.1.3"
            val errorMessage = "Expected currency: 'currency'. Actual: '$invalidCurrency'."

            assertEquals(errorCode, actual.reason.code)
            assertEquals(errorMessage, actual.reason.description)
        }

        @Test
        fun contractPeriodEndDateIsBeforeStartDate_fail() {
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/rfq/entity_pn.json"))

            whenever(tenderProcessRepository.getByCpIdAndOcid(CPID, OCID)).thenReturn(tenderProcessEntity.asSuccess())
            val params = getParams()
            val paramsWithInvalidContractPeriod = params.copy(
                tender = params.tender.copy(
                    lots = listOf(
                        params.tender.lots.first()
                            .copy(
                                contractPeriod = ValidateRfqDataParams.Tender.Lot.ContractPeriod(
                                    startDate = DATE,
                                    endDate = DATE.minusSeconds(1)
                                )
                            )
                    )
                )
            )
            val actual = rfqService.validateRfqData(paramsWithInvalidContractPeriod) as ValidationResult.Error
            val errorCode = "VR.COM-46.1.4"
            val errorMessage = "Contract period start date must precede contract period end date."

            assertEquals(errorCode, actual.reason.code)
            assertEquals(errorMessage, actual.reason.description)
        }

        @Test
        fun contractPeriodEndDateEqualsStartDate_fail() {
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/rfq/entity_pn.json"))

            whenever(tenderProcessRepository.getByCpIdAndOcid(CPID, OCID)).thenReturn(tenderProcessEntity.asSuccess())
            val params = getParams()
            val paramsWithInvalidContractPeriod = params.copy(
                tender = params.tender.copy(
                    lots = listOf(
                        params.tender.lots.first()
                            .copy(
                                contractPeriod = ValidateRfqDataParams.Tender.Lot.ContractPeriod(
                                    startDate = DATE,
                                    endDate = DATE
                                )
                            )
                    )
                )
            )
            val actual = rfqService.validateRfqData(paramsWithInvalidContractPeriod) as ValidationResult.Error
            val errorCode = "VR.COM-46.1.4"
            val errorMessage = "Contract period start date must precede contract period end date."

            assertEquals(errorCode, actual.reason.code)
            assertEquals(errorMessage, actual.reason.description)
        }

        @Test
        fun contractPeriodStartDateIsBeforeTenderPeriodEndDate_fail() {
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/rfq/entity_pn.json"))

            whenever(tenderProcessRepository.getByCpIdAndOcid(CPID, OCID)).thenReturn(tenderProcessEntity.asSuccess())
            val params = getParams()
            val paramsWithInvalidContractPeriod = params.copy(
                tender = params.tender.copy(
                    lots = listOf(
                        params.tender.lots.first()
                            .copy(
                                contractPeriod = ValidateRfqDataParams.Tender.Lot.ContractPeriod(
                                    startDate = DATE,
                                    endDate = DATE.plusDays(1)
                                )
                            )
                    ),
                    tenderPeriod = ValidateRfqDataParams.Tender.TenderPeriod(endDate = DATE.plusDays(2))
                )
            )
            val actual = rfqService.validateRfqData(paramsWithInvalidContractPeriod) as ValidationResult.Error
            val errorCode = "VR.COM-46.1.5"
            val errorMessage = "Tender period end date must precede contract period start date."

            assertEquals(errorCode, actual.reason.code)
            assertEquals(errorMessage, actual.reason.description)
        }

        @Test
        fun contractPeriodStartDateEqualsTenderPeriodEndDate_fail() {
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/rfq/entity_pn.json"))

            whenever(tenderProcessRepository.getByCpIdAndOcid(CPID, OCID)).thenReturn(tenderProcessEntity.asSuccess())
            val params = getParams()
            val paramsWithInvalidContractPeriod = params.copy(
                tender = params.tender.copy(
                    lots = listOf(
                        params.tender.lots.first()
                            .copy(
                                contractPeriod = ValidateRfqDataParams.Tender.Lot.ContractPeriod(
                                    startDate = DATE,
                                    endDate = DATE.plusDays(1)
                                )
                            )
                    ),
                    tenderPeriod = ValidateRfqDataParams.Tender.TenderPeriod(endDate = DATE)
                )
            )
            val actual = rfqService.validateRfqData(paramsWithInvalidContractPeriod) as ValidationResult.Error
            val errorCode = "VR.COM-46.1.5"
            val errorMessage = "Tender period end date must precede contract period start date."

            assertEquals(errorCode, actual.reason.code)
            assertEquals(errorMessage, actual.reason.description)
        }

        @Test
        fun duplicateItemsReceived_fail() {
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/rfq/entity_pn.json"))

            whenever(tenderProcessRepository.getByCpIdAndOcid(CPID, OCID)).thenReturn(tenderProcessEntity.asSuccess())
            val params = getParams()
            val paramsWithDuplicatedItems = params.copy(tender = params.tender.copy(items = params.tender.items + params.tender.items))
            val actual = rfqService.validateRfqData(paramsWithDuplicatedItems) as ValidationResult.Error
            val errorCode = "VR.COM-46.1.6"
            val errorMessage = "Items contain duplicate id '${params.tender.items.first().id}'."

            assertEquals(errorCode, actual.reason.code)
            assertEquals(errorMessage, actual.reason.description)
        }

        @Test
        fun unknownRelatedLot_fail() {
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/rfq/entity_pn.json"))

            whenever(tenderProcessRepository.getByCpIdAndOcid(CPID, OCID)).thenReturn(tenderProcessEntity.asSuccess())
            val params = getParams()
            val invalidRelatedLot = "invalidRelatedLot"
            val paramsWithInvalidRelatedLot = params.copy(tender = params.tender.copy(items = listOf(params.tender.items.first().copy(relatedLot = invalidRelatedLot))))
            val actual = rfqService.validateRfqData(paramsWithInvalidRelatedLot) as ValidationResult.Error
            val errorCode = "VR.COM-46.1.7"
            val errorMessage = "Item '$ITEM_ID' contains unknown relatedLot '$invalidRelatedLot'."

            assertEquals(errorCode, actual.reason.code)
            assertEquals(errorMessage, actual.reason.description)
        }

        @Test
        fun `fail on invalid quantity`() {
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/rfq/entity_pn.json"))

            whenever(tenderProcessRepository.getByCpIdAndOcid(CPID, OCID)).thenReturn(tenderProcessEntity.asSuccess())
            val params = getParams()
            val invalidQuantity = BigDecimal.ZERO
            val paramsWithInvalidQuantity = params.copy(
                tender = params.tender.copy(
                    items = listOf(params.tender.items.first().copy(
                        quantity = invalidQuantity)
                    )
                )
            )
            val actual = rfqService.validateRfqData(paramsWithInvalidQuantity) as ValidationResult.Error
            val expectedErrorCode = ValidateRfqDataErrors.InvalidItemQuantity(emptyList()).code

            assertEquals(expectedErrorCode, actual.reason.code)
        }

        @Test
        fun `success on valid quantity`() {
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/rfq/entity_pn.json"))

            whenever(tenderProcessRepository.getByCpIdAndOcid(CPID, OCID)).thenReturn(tenderProcessEntity.asSuccess())
            val params = getParams()
            val invalidQuantity = BigDecimal.TEN
            val paramsWithInvalidQuantity = params.copy(
                tender = params.tender.copy(
                    items = listOf(params.tender.items.first().copy(
                        quantity = invalidQuantity)
                    )
                )
            )
            val actual = rfqService.validateRfqData(paramsWithInvalidQuantity)
            assertTrue(actual is ValidationResult.Ok)
        }

        @Test
        fun electronicAuctionsAreMissing_fail() {
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/rfq/entity_pn.json"))

            whenever(tenderProcessRepository.getByCpIdAndOcid(CPID, OCID)).thenReturn(tenderProcessEntity.asSuccess())
            val params = getParams()
            val paramsWithoutAuctions = params.copy(tender = params.tender.copy(electronicAuctions = null))
            val actual = rfqService.validateRfqData(paramsWithoutAuctions) as ValidationResult.Error
            val errorCode = "VR.COM-46.1.8"
            val errorMessage = "Electronic auctions are missing."

            assertEquals(errorCode, actual.reason.code)
            assertEquals(errorMessage, actual.reason.description)
        }

        @Test
        fun electronicAuctionsMustBeAbsent_fail() {
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/rfq/entity_pn.json"))

            whenever(tenderProcessRepository.getByCpIdAndOcid(CPID, OCID)).thenReturn(tenderProcessEntity.asSuccess())
            val params = getParams()
            val paramsWithAuctions = params.copy(tender = params.tender.copy(procurementMethodModalities = emptyList()))
            val actual = rfqService.validateRfqData(paramsWithAuctions) as ValidationResult.Error
            val errorCode = "VR.COM-46.1.9"
            val errorMessage = "Request contains redundant electronic auctions."

            assertEquals(errorCode, actual.reason.code)
            assertEquals(errorMessage, actual.reason.description)
        }



        private fun getParams() = ValidateRfqDataParams(
            relatedCpid = CPID,
            relatedOcid = OCID,
            tender = ValidateRfqDataParams.Tender(
                lots = listOf(
                    ValidateRfqDataParams.Tender.Lot(
                        id = LOT_ID.toString(),
                        title = "title",
                        contractPeriod = ValidateRfqDataParams.Tender.Lot.ContractPeriod(
                            startDate = DATE,
                            endDate = DATE.plusDays(2)
                        ),
                        value = ValidateRfqDataParams.Tender.Lot.Value(CURRENCY),
                        placeOfPerformance = mock(),
                        description = null,
                        internalId = null
                    )
                ),
                items = listOf(
                    ValidateRfqDataParams.Tender.Item(
                        id = ITEM_ID,
                        internalId = null,
                        description = "description",
                        unit = mock(),
                        quantity = mock(),
                        classification = mock(),
                        relatedLot = LOT_ID.toString()
                    )
                ),
                tenderPeriod = ValidateRfqDataParams.Tender.TenderPeriod(endDate = DATE.minusDays(1)),
                procurementMethodModalities = listOf(ProcurementMethodModalities.ELECTRONIC_AUCTION),
                electronicAuctions = ValidateRfqDataParams.Tender.ElectronicAuctions(emptyList())
            )
        )
    }
}