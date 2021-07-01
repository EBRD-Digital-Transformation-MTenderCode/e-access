package com.procurement.access.service

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.access.application.model.params.DivideLotParams
import com.procurement.access.application.model.params.GetLotsValueParams
import com.procurement.access.application.model.params.ValidateLotsDataForDivisionParams
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.domain.model.enums.Scheme
import com.procurement.access.infrastructure.generator.TenderProcessEntityGenerator
import com.procurement.access.infrastructure.handler.v1.model.response.GetLotsValueResult
import com.procurement.access.infrastructure.handler.v2.model.response.DivideLotResult
import com.procurement.access.json.loadJson
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.Result.Companion.success
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

internal class LotsServiceTest {

    companion object {
        private val CPID = Cpid.tryCreateOrNull("ocds-t1s2t3-MD-1565251033096")!!
        private val OCID = Ocid.SingleStage.tryCreateOrNull("ocds-b3wdp1-MD-1581509539187-EV-1581509653044")!!
        private val STORED_LOT_ID = UUID.fromString("0124de77-8143-49dd-8fca-eda3f682f013")
        private val LOT_ID_1 = UUID.fromString("dccd933c-10d1-463f-97f2-8966dfc211c8")
        private val LOT_ID_2 = UUID.fromString("03af0741-32d0-41a1-a953-42b43278eacd")
        private val ITEM_ID_1 = "item_id_1"
        private val ITEM_ID_2 = "item_id_2"

        private const val FORMAT_PATTERN = "uuuu-MM-dd'T'HH:mm:ss'Z'"
        private val FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern(FORMAT_PATTERN)
            .withResolverStyle(ResolverStyle.STRICT)
        private val DATE = LocalDateTime.parse("2020-02-10T08:49:55Z", FORMATTER)
    }

    private lateinit var lotsService: LotsService
    private lateinit var tenderProcessDao: TenderProcessDao
    private lateinit var tenderProcessRepository: TenderProcessRepository
    private lateinit var rulesService: RulesService
    private lateinit var generationService: GenerationService

    @BeforeEach
    fun init() {
        tenderProcessDao = mock()
        tenderProcessRepository = mock()
        rulesService = mock()
        generationService = mock()

        lotsService = LotsService(tenderProcessDao, tenderProcessRepository, generationService, rulesService)
    }

    @Nested
    inner class GetLotsValue {

        @Test
        fun success() {
            val params = getLotsValueParams()
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/lots/value/tender_lots_value.json"))
            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid))
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
            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid))
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
            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid))
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
    inner class ValidateLotsDataForDivision {

        @Test
        fun success() {
            val params = getParams()
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity.json"))
            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsDataForDivision(params)

            assertTrue(actual is ValidationResult.Ok)
        }

        @Test
        fun tenderNotFound() {
            val params = getParams()
            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(success(null))
            val actual = lotsService.validateLotsDataForDivision(params) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.1"
            val expectedMessage = "Tender not found by cpid='${params.cpid}' and ocid='${params.ocid}'."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun receivedTwoLotsThatAreAlreadyStored() {
            val params = getParams()
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity_that_mathces_two_received_lots.json"))
            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsDataForDivision(params) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.2"
            val expectedMessage = "Stored tender must contain only one lot that matches received. Matching lots: '0124de77-8143-49dd-8fca-eda3f682f013, dccd933c-10d1-463f-97f2-8966dfc211c8'."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun noMatchingLotsStored() {
            val params = getParams()
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity_with_no_matching_lots.json"))
            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsDataForDivision(params) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.2"
            val expectedMessage = "Stored tender must contain only one lot that matches received. But contains none."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun oneNewLotReceived() {
            val params = getParams()
            val paramsWithOneStoredAndOneNewLot = params.copy(
                tender = params.tender.copy(
                    lots = params.tender.lots.subList(
                        0,
                        2
                    )
                )
            )

            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity.json"))
            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsDataForDivision(paramsWithOneStoredAndOneNewLot) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.3"
            val expectedMessage = "Received tender must contain two or more new lots."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun titleOfNewLotIsNull() {
            val params = getParams()
            val paramsWithNullTitle = params.copy(tender = params.tender.copy(lots = params.tender.lots.map { lot ->
                lot.copy(
                    title = null
                )
            }))
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity.json"))
            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsDataForDivision(paramsWithNullTitle) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.12"
            val expectedMessage = "Lot '$LOT_ID_1' must contain title."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun descriptionOfNewLotIsNull() {
            val params = getParams()
            val paramsWithNullTitle = params.copy(tender = params.tender.copy(lots = params.tender.lots.map { lot ->
                lot.copy(
                    description = null
                )
            }))
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity.json"))
            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsDataForDivision(paramsWithNullTitle) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.13"
            val expectedMessage = "Lot '$LOT_ID_1' must contain description."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun valueOfNewLotIsNull() {
            val params = getParams()
            val paramsWithNullTitle = params.copy(tender = params.tender.copy(lots = params.tender.lots.map { lot ->
                lot.copy(
                    value = null
                )
            }))
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity.json"))
            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsDataForDivision(paramsWithNullTitle) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.14"
            val expectedMessage = "Lot '$LOT_ID_1' must contain value."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun contractPeriodOfNewLotIsNull() {
            val params = getParams()
            val paramsWithNullTitle = params.copy(tender = params.tender.copy(lots = params.tender.lots.map { lot ->
                lot.copy(
                    contractPeriod = null
                )
            }))
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity.json"))
            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsDataForDivision(paramsWithNullTitle) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.15"
            val expectedMessage = "Lot '$LOT_ID_1' must contain contractPeriod."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun placeOfPerformanceOfNewLotIsNull() {
            val params = getParams()
            val paramsWithNullTitle = params.copy(tender = params.tender.copy(lots = params.tender.lots.map { lot ->
                lot.copy(
                    placeOfPerformance = null
                )
            }))
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity.json"))
            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsDataForDivision(paramsWithNullTitle) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.16"
            val expectedMessage = "Lot '$LOT_ID_1' must contain placeOfPerformance."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun currencyDoesNotMatch() {
            val params = getParams()
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity_with_unmatching_currency.json"))
            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsDataForDivision(params) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.4"
            val expectedMessage = "Currency of lot '$LOT_ID_1' does not match currency of divided lot '$STORED_LOT_ID'."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun amountDoesNotMatch() {
            val params = getParams()
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity_with_unmatching_amount_sum.json"))
            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsDataForDivision(params) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.5"
            val expectedMessage = "Sum of new lots amounts does equal amount of divided lot '$STORED_LOT_ID'."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun contractPeriodStartDoesNotMatch() {
            val params = getParams()
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity_with_unmatching_contract_period_start.json"))
            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsDataForDivision(params) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.6"
            val expectedMessage = "Contract period start date of lot '$LOT_ID_1' does not match start date of divided lot '$STORED_LOT_ID'."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun contractPeriodEndDoesNotMatch() {
            val params = getParams()
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity_with_unmatching_contract_period_end.json"))
            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsDataForDivision(params) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.7"
            val expectedMessage = "Contract period end date of lot '$LOT_ID_1' does not match end date of divided lot '$STORED_LOT_ID'."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun newLotDoesNotHaveRelatedItem() {
            val params = getParams()
            val paramsWithoutItemForOneNewLot = params.copy(
                tender = params.tender.copy(
                    items = params.tender.items.map { it.copy(relatedLot = LOT_ID_1.toString()) }
                )
            )

            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity.json"))
            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsDataForDivision(paramsWithoutItemForOneNewLot) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.8"
            val expectedMessage = "No related items found for lot(s) '$LOT_ID_2'."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun oneItemIsMissingInReceivedData() {
            val params = getParams()
            val paramsWithoutItemForDividedLot = params.copy(
                tender = params.tender.copy(
                    items = listOf(params.tender.items[0])
                )
            )

            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity.json"))
            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsDataForDivision(paramsWithoutItemForDividedLot) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.9"
            val expectedMessage = "Received divided lot '$STORED_LOT_ID' is missing item(s) '${params.tender.items[1].id}'."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun receivedItemNotFound() {
            val params = getParams()
            val paramsWithRedundantReceivedItem = params.copy(
                tender = params.tender.copy(
                    items = params.tender.items + ValidateLotsDataForDivisionParams.Tender.Item(
                        id = "redundant",
                        relatedLot = LOT_ID_1.toString()
                    )
                )
            )

            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity.json"))
            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsDataForDivision(paramsWithRedundantReceivedItem) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.10"
            val expectedMessage = "Received divided lot '$STORED_LOT_ID' contains unknown items 'redundant'."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun itemNotLinkedToAnyNewLot() {
            val params = getParams()
            val paramsWithUnrelatedItem = params.copy(
                tender = params.tender.copy(
                    items = params.tender.items + ValidateLotsDataForDivisionParams.Tender.Item(
                        id = "id3",
                        relatedLot = UUID.randomUUID().toString()
                    )
                )
            )

            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity_with_additional_item.json"))
            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsDataForDivision(paramsWithUnrelatedItem) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.11"
            val expectedMessage = "Item(s) 'id3' not linked to any lots."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun containsOptionsWithHasOptionIsFalse_fail() {
            val params = getParams()
            val paramsWithOptions = params.copy(
                tender = params.tender.copy(
                    lots = params.tender.lots.map {
                        it.copy(
                            options = listOf(
                                ValidateLotsDataForDivisionParams.Tender.Lot.Option(description = "string", period = null)
                            ),
                            hasOptions = false
                        )
            }))

            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity.json"))
            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsDataForDivision(paramsWithOptions) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.18"
            val expectedMessage = "Lot '${params.tender.lots[0].id}' contains redundant list of options."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun containsRenewalWithHasRenewalIsFalse_fail() {
            val params = getParams()
            val paramsWithOptions = params.copy(
                tender = params.tender.copy(
                    lots = params.tender.lots.map {
                        it.copy(
                            renewal = ValidateLotsDataForDivisionParams.Tender.Lot.Renewal(
                                description = "string",
                                period = null,
                                maximumRenewals = null,
                                minimumRenewals = null
                            ),
                            hasRenewal = false
                        )
                    }))

            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity.json"))
            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsDataForDivision(paramsWithOptions) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.20"
            val expectedMessage = "Lot '${params.tender.lots[0].id}' contains redundant renewal."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        @Test
        fun containsRecurrenceWithHasRenewalIsFalse_fail() {
            val params = getParams()
            val paramsWithOptions = params.copy(
                tender = params.tender.copy(
                    lots = params.tender.lots.map {
                        it.copy(
                            recurrence = ValidateLotsDataForDivisionParams.Tender.Lot.Recurrence(
                                description = "string",
                                dates = null
                            ),
                            hasRecurrence = false
                        )
                    }))

            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/validate/lot/tender_entity.json"))
            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(success(tenderProcessEntity))
            val actual = lotsService.validateLotsDataForDivision(paramsWithOptions) as ValidationResult.Error

            val expectedErrorCode = "VR.COM-1.39.19"
            val expectedMessage = "Lot '${params.tender.lots[0].id}' contains redundant recurrence."

            assertEquals(expectedErrorCode, actual.reason.code)
            assertEquals(expectedMessage, actual.reason.description)
        }

        private fun getParams(): ValidateLotsDataForDivisionParams {
            return ValidateLotsDataForDivisionParams(
                cpid = CPID,
                ocid = OCID,
                tender = ValidateLotsDataForDivisionParams.Tender(
                    lots = listOf(
                        ValidateLotsDataForDivisionParams.Tender.Lot(
                            id = STORED_LOT_ID.toString(),
                            internalId = null,
                            title = null,
                            description = null,
                            placeOfPerformance = null,
                            contractPeriod = null,
                            value = null,
                            hasRenewal = null,
                            hasOptions = null,
                            hasRecurrence = null,
                            options = emptyList(),
                            renewal = null,
                            recurrence = null
                        ),
                        ValidateLotsDataForDivisionParams.Tender.Lot(
                            id = LOT_ID_1.toString(),
                            internalId = null,
                            title = "title",
                            description = "description",
                            placeOfPerformance = ValidateLotsDataForDivisionParams.Tender.Lot.PlaceOfPerformance(
                                description = null,
                                address = ValidateLotsDataForDivisionParams.Tender.Lot.PlaceOfPerformance.Address(
                                    postalCode = null,
                                    streetAddress = "streetAddress",
                                    addressDetails = ValidateLotsDataForDivisionParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                        country = ValidateLotsDataForDivisionParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                            id = "",
                                            description = "",
                                            scheme = ""
                                        ),
                                        region = ValidateLotsDataForDivisionParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                            id = "",
                                            description = "",
                                            scheme = ""
                                        ),
                                        locality = ValidateLotsDataForDivisionParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
                                            id = "",
                                            description = "",
                                            scheme = ""
                                        )
                                    )
                                )
                            ),
                            contractPeriod = ValidateLotsDataForDivisionParams.Tender.Lot.ContractPeriod(
                                startDate = DATE,
                                endDate = DATE.plusDays(1)
                            ),
                            value = ValidateLotsDataForDivisionParams.Tender.Lot.Value(
                                amount = BigDecimal(2),
                                currency = "currency"
                            ),
                            hasRenewal = null,
                            hasOptions = null,
                            hasRecurrence = null,
                            options = emptyList(),
                            renewal = null,
                            recurrence = null
                        ),
                        ValidateLotsDataForDivisionParams.Tender.Lot(
                            id = LOT_ID_2.toString(),
                            internalId = null,
                            title = "title",
                            description = "description",
                            placeOfPerformance = ValidateLotsDataForDivisionParams.Tender.Lot.PlaceOfPerformance(
                                description = null,
                                address = ValidateLotsDataForDivisionParams.Tender.Lot.PlaceOfPerformance.Address(
                                    postalCode = null,
                                    streetAddress = "streetAddress",
                                    addressDetails = ValidateLotsDataForDivisionParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                        country = ValidateLotsDataForDivisionParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                            id = "",
                                            description = "",
                                            scheme = ""
                                        ),
                                        region = ValidateLotsDataForDivisionParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                            id = "",
                                            description = "",
                                            scheme = ""
                                        ),
                                        locality = ValidateLotsDataForDivisionParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
                                            id = "",
                                            description = "",
                                            scheme = ""
                                        )
                                    )
                                )
                            ),
                            contractPeriod = ValidateLotsDataForDivisionParams.Tender.Lot.ContractPeriod(
                                startDate = DATE,
                                endDate = DATE.plusDays(1)
                            ),
                            value = ValidateLotsDataForDivisionParams.Tender.Lot.Value(
                                amount = BigDecimal(4.020),
                                currency = "currency"
                            ),
                            hasRenewal = null,
                            hasOptions = null,
                            hasRecurrence = null,
                            options = emptyList(),
                            renewal = null,
                            recurrence = null
                        )
                    ),
                    items = listOf(
                        ValidateLotsDataForDivisionParams.Tender.Item(
                            id = "id1",
                            relatedLot = LOT_ID_1.toString()
                        ),
                        ValidateLotsDataForDivisionParams.Tender.Item(
                            id = "id2",
                            relatedLot = LOT_ID_2.toString()
                        )
                    )
                )
            )
        }
    }

    @Nested
    inner class DivideLot {

        @Test
        fun success() {
            val params = getParams()
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = loadJson("json/service/divide/lot/tender_entity.json"))
            whenever(tenderProcessRepository.getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid))
                .thenReturn(success(tenderProcessEntity))
            whenever(generationService.lotId())
                .thenReturn(LOT_ID_1)
                .thenReturn(LOT_ID_2)
            whenever(tenderProcessRepository.save(any())).thenReturn(true.asSuccess())

            val actual = lotsService.divideLot(params)

            val expectedLotsResult = generateExpectedLots()
            val expectedItemsResult = generateExpectedItems()

            assertEquals(expectedLotsResult, actual.get.tender.lots.toSet())
            assertEquals(expectedItemsResult, actual.get.tender.items.toSet())
        }

        fun generateExpectedLots() = setOf(
            DivideLotResult.Tender.Lot(
                id = LOT_ID_1.toString(),
                status = LotStatus.ACTIVE,
                statusDetails = LotStatusDetails.EMPTY,
                internalId = "internalId",
                title = "title",
                description = "description",
                value = DivideLotResult.Tender.Lot.Value(amount = BigDecimal.ONE, currency = "currency"),
                contractPeriod = DivideLotResult.Tender.Lot.ContractPeriod(
                    startDate = DATE,
                    endDate = DATE.plusDays(1)
                ),
                placeOfPerformance = DivideLotResult.Tender.Lot.PlaceOfPerformance(
                    description = "description",
                    address = DivideLotResult.Tender.Lot.PlaceOfPerformance.Address(
                        streetAddress = "streetAddress",
                        postalCode = "postalCode",
                        addressDetails = DivideLotResult.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                            country = DivideLotResult.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                id = "id",
                                description = "description",
                                scheme = "scheme",
                                uri = "uri"
                            ),
                            region = DivideLotResult.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                id = "id",
                                description = "description",
                                scheme = "scheme",
                                uri = "uri"
                            ),
                            locality = DivideLotResult.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
                                id = "id",
                                description = "description",
                                scheme = "scheme",
                                uri = "uri"
                            )
                        )
                    )
                ),
                hasRenewal = true,
                hasOptions = true,
                hasRecurrence = true,
                options = listOf(DivideLotResult.Tender.Lot.Option(
                    description = "string",
                    period = DivideLotResult.Tender.Lot.Option.Period(
                        durationInDays = 1,
                        maxExtentDate = DATE.minusDays(1),
                        endDate = DATE.minusDays(2),
                        startDate = DATE.minusDays(3)
                    )
                )),
                renewal = DivideLotResult.Tender.Lot.Renewal(
                    description = "string",
                    period = DivideLotResult.Tender.Lot.Renewal.Period(
                        durationInDays = 10,
                        maxExtentDate = DATE.minusDays(10),
                        endDate = DATE.minusDays(20),
                        startDate = DATE.minusDays(30)
                    ),
                    maximumRenewals = 1,
                    minimumRenewals = 1
                ),
                recurrence = DivideLotResult.Tender.Lot.Recurrence(
                    dates = listOf(DivideLotResult.Tender.Lot.Recurrence.Date(DATE)),
                    description = "string"
                )
            ),
            DivideLotResult.Tender.Lot(
                id = LOT_ID_2.toString(),
                status = LotStatus.ACTIVE,
                statusDetails = LotStatusDetails.EMPTY,
                internalId = "internalId",
                title = "title",
                description = "description",
                value = DivideLotResult.Tender.Lot.Value(
                    amount = BigDecimal.TEN,
                    currency = "currency"
                ),
                contractPeriod = DivideLotResult.Tender.Lot.ContractPeriod(
                    startDate = LocalDateTime.parse("2020-02-10T08:49:55Z", FORMATTER),
                    endDate = LocalDateTime.parse("2020-02-11T08:49:55Z", FORMATTER)
                ),
                placeOfPerformance = DivideLotResult.Tender.Lot.PlaceOfPerformance(
                    description = "description",
                    address = DivideLotResult.Tender.Lot.PlaceOfPerformance.Address(
                        streetAddress = "streetAddress",
                        postalCode = "postalCode",
                        addressDetails = DivideLotResult.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                            country = DivideLotResult.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                id = "id",
                                description = "description",
                                scheme = "scheme",
                                uri = "uri"
                            ),
                            region = DivideLotResult.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                id = "id",
                                description = "description",
                                scheme = "scheme",
                                uri = "uri"
                            ),
                            locality = DivideLotResult.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
                                id = "id",
                                description = "description",
                                scheme = "scheme",
                                uri = "uri"
                            )
                        )
                    )
                ),
                hasRenewal = false,
                hasOptions = false,
                hasRecurrence = false,
                options = emptyList(),
                renewal = null,
                recurrence = null
            ),
            DivideLotResult.Tender.Lot(
                id = STORED_LOT_ID.toString(),
                status = LotStatus.CANCELLED,
                statusDetails = LotStatusDetails.EMPTY,
                internalId = "string",
                title = "string",
                description = "divided_lot",
                value = DivideLotResult.Tender.Lot.Value(
                    amount = BigDecimal("6.02"),
                    currency = "string"
                ),
                contractPeriod = DivideLotResult.Tender.Lot.ContractPeriod(
                    startDate = LocalDateTime.parse("2020-10-29T09:41:07Z", FORMATTER),
                    endDate = LocalDateTime.parse("2020-10-29T09:41:07Z", FORMATTER)
                ),
                placeOfPerformance = DivideLotResult.Tender.Lot.PlaceOfPerformance(
                    description = null,
                    address = DivideLotResult.Tender.Lot.PlaceOfPerformance.Address(
                        streetAddress = "string",
                        postalCode = "string",
                        addressDetails = DivideLotResult.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                            country = DivideLotResult.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                id = "string",
                                description = "string",
                                scheme = "string",
                                uri = "string"
                            ),
                            region = DivideLotResult.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                id = "string",
                                description = "string",
                                scheme = "string",
                                uri = "string"
                            ),
                            locality = DivideLotResult.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
                                id = "string",
                                description = "string",
                                scheme = "string",
                                uri = "string"
                            )
                        )
                    )
                ),
                hasRenewal = null,
                hasOptions = null,
                hasRecurrence = null,
                options = emptyList(),
                renewal = null,
                recurrence = null
            )

        )

        fun generateExpectedItems() = setOf(
            DivideLotResult.Tender.Item(
                id = ITEM_ID_1,
                relatedLot = LOT_ID_1.toString(),
                internalId = "string",
                description = "string",
                quantity = BigDecimal.ZERO,
                classification =
                DivideLotResult.Tender.Item.Classification(
                    id = "string",
                    description = "string",
                    scheme = Scheme.CPV
                ),
                unit =
                DivideLotResult.Tender.Item.Unit(
                    id = "string",
                    name = "string"
                ),
                additionalClassifications = listOf(
                    DivideLotResult.Tender.Item.AdditionalClassification(
                        id = "string",
                        scheme = Scheme.CPV,
                        description = "string"
                    )
                )
            ),
            DivideLotResult.Tender.Item(
                id = ITEM_ID_2,
                relatedLot = LOT_ID_2.toString(),
                internalId = "string",
                description = "string",
                quantity = BigDecimal.ZERO,
                classification =
                DivideLotResult.Tender.Item.Classification(
                    id = "string",
                    description = "string",
                    scheme = Scheme.CPV
                ),
                unit =
                DivideLotResult.Tender.Item.Unit(
                    id = "string",
                    name = "string"
                ),
                additionalClassifications = listOf(
                    DivideLotResult.Tender.Item.AdditionalClassification(
                        id = "string",
                        scheme = Scheme.CPV,
                        description = "string"
                    )
                )
            )
        )

        private fun getParams() = DivideLotParams(
            cpid = CPID,
            ocid = OCID,
            tender = DivideLotParams.Tender(
                lots = listOf(
                    DivideLotParams.Tender.Lot(
                        id = STORED_LOT_ID.toString(),
                        description = null,
                        internalId = null,
                        placeOfPerformance = null,
                        contractPeriod = null,
                        value = null,
                        title = null,
                        hasRenewal = null,
                        hasOptions = null,
                        hasRecurrence = null,
                        options = emptyList(),
                        renewal = null,
                        recurrence = null
                    ),
                    DivideLotParams.Tender.Lot(
                        id = "ae865fd4-288e-4862-9ec2-6d9f1f1a59be",
                        description = "description",
                        internalId = "internalId",
                        placeOfPerformance = DivideLotParams.Tender.Lot.PlaceOfPerformance(
                            description = "description",
                            address = DivideLotParams.Tender.Lot.PlaceOfPerformance.Address(
                                streetAddress = "streetAddress",
                                postalCode = "postalCode",
                                addressDetails = DivideLotParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                    country = DivideLotParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                        id = "id",
                                        description = "description",
                                        scheme = "scheme",
                                        uri = "uri"
                                    ),
                                    region = DivideLotParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                        id = "id",
                                        description = "description",
                                        scheme = "scheme",
                                        uri = "uri"
                                    ),
                                    locality = DivideLotParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
                                        id = "id",
                                        description = "description",
                                        scheme = "scheme",
                                        uri = "uri"
                                    )
                                )
                            )
                        ),
                        contractPeriod = DivideLotParams.Tender.Lot.ContractPeriod(
                            startDate = DATE,
                            endDate = DATE.plusDays(1)
                        ),
                        value = DivideLotParams.Tender.Lot.Value(amount = BigDecimal.ONE, currency = "currency"),
                        title = "title",
                        hasRenewal = true,
                        hasOptions = true,
                        hasRecurrence = true,
                        options = listOf(DivideLotParams.Tender.Lot.Option(
                            description = "string",
                            period = DivideLotParams.Tender.Lot.Option.Period(
                                durationInDays = 1,
                                maxExtentDate = DATE.minusDays(1),
                                endDate = DATE.minusDays(2),
                                startDate = DATE.minusDays(3)
                            )
                        )),
                        renewal = DivideLotParams.Tender.Lot.Renewal(
                            description = "string",
                            period = DivideLotParams.Tender.Lot.Renewal.Period(
                                durationInDays = 10,
                                maxExtentDate = DATE.minusDays(10),
                                endDate = DATE.minusDays(20),
                                startDate = DATE.minusDays(30)
                            ),
                            maximumRenewals = 1,
                            minimumRenewals = 1
                        ),
                        recurrence = DivideLotParams.Tender.Lot.Recurrence(
                            dates = listOf(DivideLotParams.Tender.Lot.Recurrence.Date(DATE)),
                            description = "string"
                        )
                    ),
                    DivideLotParams.Tender.Lot(
                        id = "beb7c28c-6bb6-444d-b43d-a7cf2454b935",
                        description = "description",
                        internalId = "internalId",
                        placeOfPerformance = DivideLotParams.Tender.Lot.PlaceOfPerformance(
                            description = "description",
                            address = DivideLotParams.Tender.Lot.PlaceOfPerformance.Address(
                                streetAddress = "streetAddress",
                                postalCode = "postalCode",
                                addressDetails = DivideLotParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                    country = DivideLotParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                        id = "id",
                                        description = "description",
                                        scheme = "scheme",
                                        uri = "uri"
                                    ),
                                    region = DivideLotParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                        id = "id",
                                        description = "description",
                                        scheme = "scheme",
                                        uri = "uri"
                                    ),
                                    locality = DivideLotParams.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
                                        id = "id",
                                        description = "description",
                                        scheme = "scheme",
                                        uri = "uri"
                                    )
                                )
                            )
                        ),
                        contractPeriod = DivideLotParams.Tender.Lot.ContractPeriod(
                            startDate = DATE,
                            endDate = DATE.plusDays(1)
                        ),
                        value = DivideLotParams.Tender.Lot.Value(amount = BigDecimal.TEN, currency = "currency"),
                        title = "title",
                        hasRenewal = null,
                        hasOptions = null,
                        hasRecurrence = null,
                        options = emptyList(),
                        renewal = null,
                        recurrence = null
                    )
                ),
                items = listOf(
                    DivideLotParams.Tender.Item(
                        id = ITEM_ID_1,
                        relatedLot = "ae865fd4-288e-4862-9ec2-6d9f1f1a59be"
                    ),
                    DivideLotParams.Tender.Item(
                        id = ITEM_ID_2,
                        relatedLot = "beb7c28c-6bb6-444d-b43d-a7cf2454b935"
                    )
                )
            )
        )
    }
}