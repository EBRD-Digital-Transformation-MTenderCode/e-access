package com.procurement.access.service

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.access.application.model.params.GetLotsValueParams
import com.procurement.access.application.model.params.ValidateLotsDataParams
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.infrastructure.generator.TenderProcessEntityGenerator
import com.procurement.access.infrastructure.handler.v1.model.response.GetLotsValueResult
import com.procurement.access.json.loadJson
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.Result.Companion.success
import com.procurement.access.lib.functional.ValidationResult
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

internal class LotsServiceTest {

    companion object {
        private val CPID = Cpid.tryCreateOrNull("ocds-t1s2t3-MD-1565251033096")!!
        private val OCID = Ocid.tryCreateOrNull("ocds-b3wdp1-MD-1581509539187-EV-1581509653044")!!
        private val STORED_LOT_ID = UUID.fromString("0124de77-8143-49dd-8fca-eda3f682f013")
        private val LOT_ID_1 = UUID.fromString("dccd933c-10d1-463f-97f2-8966dfc211c8")
        private val LOT_ID_2 = UUID.fromString("03af0741-32d0-41a1-a953-42b43278eacd")

        private const val FORMAT_PATTERN = "uuuu-MM-dd'T'HH:mm:ss'Z'"
        private val FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(FORMAT_PATTERN)
            .withResolverStyle(ResolverStyle.STRICT)
        private val DATE = LocalDateTime.parse("2020-02-10T08:49:55Z", FORMATTER)
    }

    private lateinit var lotsService: LotsService
    private lateinit var tenderProcessDao: TenderProcessDao
    private lateinit var tenderProcessRepository: TenderProcessRepository
    private lateinit var rulesService: RulesService

    @BeforeEach
    fun init() {
        tenderProcessDao = mock()
        tenderProcessRepository = mock()
        rulesService = mock()
        lotsService = LotsService(tenderProcessDao, tenderProcessRepository, rulesService)
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

    @Nested
    inner class ValidateLotsData {

        @Test
        fun success() {
            val params = getParams()
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity.json"))
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsData(params)

            assertTrue(actual is ValidationResult.Ok)
        }

        @Test
        fun tenderNotFound() {
            val params = getParams()
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(null))
            val actual = lotsService.validateLotsData(params) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.1"
            val expectedMessage = "Tender not found by cpid='${params.cpid}' and ocid='${params.ocid}'."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun receivedTwoLotsThatAreAlreadyStored() {
            val params = getParams()
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity_that_mathces_two_received_lots.json"))
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsData(params) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.2"
            val expectedMessage = "Stored tender must contain only one lot that matches received. Matching lots: '0124de77-8143-49dd-8fca-eda3f682f013, dccd933c-10d1-463f-97f2-8966dfc211c8'."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun noMatchingLotsStored() {
            val params = getParams()
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity_with_no_matching_lots.json"))
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsData(params) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.2"
            val expectedMessage = "Stored tender must contain only one lot that matches received. But contains none."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun oneNewLotReceived() {
            val params = getParams()
            val paramsWithOneStoredAndOneNewLot = params.copy(tender = params.tender.copy(lots = params.tender.lots.subList(0,2)))

            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity.json"))
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsData(paramsWithOneStoredAndOneNewLot) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.3"
            val expectedMessage = "Received tender must contain two or more new lots."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun titleOfNewLotIsNull() {
            val params = getParams()
            val paramsWithNullTitle = params.copy(tender = params.tender.copy(lots = params.tender.lots.map { lot -> lot.copy(title = null) }))
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity.json"))
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsData(paramsWithNullTitle) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.12"
            val expectedMessage = "Lot '$LOT_ID_1' must contain title."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun descriptionOfNewLotIsNull() {
            val params = getParams()
            val paramsWithNullTitle = params.copy(tender = params.tender.copy(lots = params.tender.lots.map { lot -> lot.copy(description = null) }))
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity.json"))
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsData(paramsWithNullTitle) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.13"
            val expectedMessage = "Lot '$LOT_ID_1' must contain description."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun valueOfNewLotIsNull() {
            val params = getParams()
            val paramsWithNullTitle = params.copy(tender = params.tender.copy(lots = params.tender.lots.map { lot -> lot.copy(value = null) }))
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity.json"))
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsData(paramsWithNullTitle) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.14"
            val expectedMessage = "Lot '$LOT_ID_1' must contain value."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun contractPeriodOfNewLotIsNull() {
            val params = getParams()
            val paramsWithNullTitle = params.copy(tender = params.tender.copy(lots = params.tender.lots.map { lot -> lot.copy(contractPeriod = null) }))
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity.json"))
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsData(paramsWithNullTitle) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.15"
            val expectedMessage = "Lot '$LOT_ID_1' must contain contractPeriod."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun placeOfPerformanceOfNewLotIsNull() {
            val params = getParams()
            val paramsWithNullTitle = params.copy(tender = params.tender.copy(lots = params.tender.lots.map { lot -> lot.copy(placeOfPerformance = null) }))
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity.json"))
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsData(paramsWithNullTitle) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.16"
            val expectedMessage = "Lot '$LOT_ID_1' must contain placeOfPerformance."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun currencyDoesNotMatch() {
            val params = getParams()
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity_with_unmatching_currency.json"))
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsData(params) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.4"
            val expectedMessage = "Currency of lot '$LOT_ID_1' does not match currency of divided lot '$STORED_LOT_ID'."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun amountDoesNotMatch() {
            val params = getParams()
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity_with_unmatching_amount_sum.json"))
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsData(params) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.5"
            val expectedMessage = "Sum of new lots amounts does equal amount of divided lot '$STORED_LOT_ID'."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun contractPeriodStartDoesNotMatch() {
            val params = getParams()
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity_with_unmatching_contract_period_start.json"))
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsData(params) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.6"
            val expectedMessage = "Contract period start date of lot '$LOT_ID_1' does not match start date of divided lot '$STORED_LOT_ID'."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun contractPeriodEndDoesNotMatch() {
            val params = getParams()
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity_with_unmatching_contract_period_end.json"))
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsData(params) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.7"
            val expectedMessage = "Contract period end date of lot '$LOT_ID_1' does not match end date of divided lot '$STORED_LOT_ID'."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun newLotDoesNotHaveRelatedItem() {
            val params = getParams()
            val paramsWithoutItemForOneNewLot = params.copy(tender = params.tender.copy(items = params.tender.items.subList(0,2)))

            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity.json"))
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsData(paramsWithoutItemForOneNewLot) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.8"
            val expectedMessage = "No related items found for lot(s) '$LOT_ID_2'."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }


        @Test
        fun dividedLotDoesNotHaveRelatedItem() {
            val params = getParams()
            val paramsWithoutItemForDividedLot = params.copy(tender = params.tender.copy(items = params.tender.items.subList(1,3)))

            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity.json"))
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsData(paramsWithoutItemForDividedLot) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.9"
            val expectedMessage = "Received divided lot '$STORED_LOT_ID' is missing item(s) 'id'."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun dividedLotReceivedRedundantItem() {
            val params = getParams()
            val paramsWithRedundantReceivedItem = params.copy(
                tender = params.tender.copy(
                    items = params.tender.items + ValidateLotsDataParams.Tender.Item(
                        id = "redundatnt",
                        relatedLot = STORED_LOT_ID
                    )
                )
            )

            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity.json"))
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsData(paramsWithRedundantReceivedItem) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.10"
            val expectedMessage = "Received divided lot '$STORED_LOT_ID' contains unknown items 'redundatnt'."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }


        @Test
        fun paramsContainItemOfUnknownLot() {
            val params = getParams()
            val paramsWithUnrelatedItem = params.copy(
                tender = params.tender.copy(
                    items = params.tender.items + ValidateLotsDataParams.Tender.Item(
                        id = "item",
                        relatedLot = UUID.randomUUID()
                    )
                )
            )

            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity.json"))
            whenever(tenderProcessRepository.getByCpIdAndStage(cpid = params.cpid, stage = params.ocid.stage))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsData(paramsWithUnrelatedItem) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.11"
            val expectedMessage = "Item(s) 'item' not linked to any lots."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }


        private fun getParams(): ValidateLotsDataParams {
            return ValidateLotsDataParams(
                cpid = CPID,
                ocid = OCID,
                tender = ValidateLotsDataParams.Tender(
                    lots = listOf(
                        ValidateLotsDataParams.Tender.Lot(
                            id = STORED_LOT_ID,
                            internalId = null,
                            title = null,
                            description = null,
                            placeOfPerformance = null,
                            contractPeriod = null,
                            value = null
                        ),
                        ValidateLotsDataParams.Tender.Lot(
                            id = LOT_ID_1,
                            internalId = null,
                            title = "title",
                            description = "description",
                            placeOfPerformance = ValidateLotsDataParams.Tender.Lot.PlaceOfPerformance(
                                description = null,
                                address = ValidateLotsDataParams.Tender.Lot.PlaceOfPerformance.Address(
                                    postalCode = null,
                                    streetAddress = "streetAddress",
                                    addressDetails = ValidateLotsDataParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                        country = ValidateLotsDataParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                            id = "",
                                            description = "",
                                            scheme = ""
                                        ),
                                        region = ValidateLotsDataParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                            id = "",
                                            description = "",
                                            scheme = ""
                                        ),
                                        locality = ValidateLotsDataParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
                                            id = "",
                                            description = "",
                                            scheme = ""
                                        )
                                    )
                                )
                            ),
                            contractPeriod = ValidateLotsDataParams.Tender.Lot.ContractPeriod(
                                startDate = DATE,
                                endDate = DATE.plusDays(1)
                            ),
                            value = ValidateLotsDataParams.Tender.Lot.Value(
                                amount = BigDecimal(2),
                                currency = "currency"
                            )
                        ),
                        ValidateLotsDataParams.Tender.Lot(
                            id = LOT_ID_2,
                            internalId = null,
                            title = "title",
                            description = "description",
                            placeOfPerformance = ValidateLotsDataParams.Tender.Lot.PlaceOfPerformance(
                                description = null,
                                address = ValidateLotsDataParams.Tender.Lot.PlaceOfPerformance.Address(
                                    postalCode = null,
                                    streetAddress = "streetAddress",
                                    addressDetails = ValidateLotsDataParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                        country = ValidateLotsDataParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                            id = "",
                                            description = "",
                                            scheme = ""
                                        ),
                                        region = ValidateLotsDataParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                            id = "",
                                            description = "",
                                            scheme = ""
                                        ),
                                        locality = ValidateLotsDataParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
                                            id = "",
                                            description = "",
                                            scheme = ""
                                        )
                                    )
                                )
                            ),
                            contractPeriod = ValidateLotsDataParams.Tender.Lot.ContractPeriod(
                                startDate = DATE,
                                endDate = DATE.plusDays(1)
                            ),
                            value = ValidateLotsDataParams.Tender.Lot.Value(
                                amount = BigDecimal(4.020),
                                currency = "currency"
                            )
                        )
                    ),
                    items = listOf(
                        ValidateLotsDataParams.Tender.Item(
                            id = "id",
                            relatedLot = STORED_LOT_ID
                        ),
                        ValidateLotsDataParams.Tender.Item(
                            id = "id1",
                            relatedLot = LOT_ID_1
                        ),
                        ValidateLotsDataParams.Tender.Item(
                            id = "id2",
                            relatedLot = LOT_ID_2
                        )
                    )
                )
            )
        }
    }
}