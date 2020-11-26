package com.procurement.access.service.validation.strategy

import com.fasterxml.jackson.databind.JsonNode
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.api.v1.CommandTypeV1
import com.procurement.access.infrastructure.dto.award.CheckAwardRequest
import com.procurement.access.infrastructure.dto.award.CheckAwardResponse
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.generator.CommandMessageGenerator
import com.procurement.access.infrastructure.generator.ContextGenerator
import com.procurement.access.infrastructure.generator.ContextGenerator.OWNER
import com.procurement.access.infrastructure.generator.ContextGenerator.TOKEN
import com.procurement.access.infrastructure.generator.TenderProcessEntityGenerator
import com.procurement.access.json.JSON
import com.procurement.access.json.JsonValidator
import com.procurement.access.json.getArray
import com.procurement.access.json.getObject
import com.procurement.access.json.loadJson
import com.procurement.access.json.setAttribute
import com.procurement.access.json.testingBindingAndMapping
import com.procurement.access.json.toJson
import com.procurement.access.json.toNode
import com.procurement.access.model.dto.bpe.CommandMessage
import com.procurement.access.service.validation.strategy.award.CheckAwardStrategy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CheckAwardStrategyTest {

    private lateinit var tenderProcessDao: TenderProcessDao
    private lateinit var strategy: CheckAwardStrategy

    @BeforeEach
    fun init() {
        tenderProcessDao = mock()
        strategy = CheckAwardStrategy(tenderProcessDao)
    }

    /**
     * eAccess executes next steps:
     *
     * 1. Finds saved Tender and related Owner & Token values in DB by values of CPID && Stage parameters
     *    from the context of Request
     * 2. Validates Owner value by rule VR-3.11.6
     * 3. Validates Token value by rule VR-3.11.5
     * 4. Validates ID value by rule VR-3.11.10
     * 5. Finds tender.lot object in tender (found on step 1) by value of ID (lot.ID) from the context of Request
     * 6. Validates the value of lot.status in lot object found before by rule VR-3.11.8
     * 7. Validates the values of award.value.amount & award.value.currency from Award got in Request by rule VR-3.11.9
     */
    @Nested
    inner class ValidationRules {

        @Test
        fun success() {
            val dataRequest = request()
            val cm = commandMessage(data = dataRequest)

            val dataEntity = tenderProcessEntityData()
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = dataEntity)
            whenever(
                tenderProcessDao.getByCpIdAndStage(
                    cpId = eq(ContextGenerator.CPID),
                    stage = eq(ContextGenerator.STAGE)
                )
            ).thenReturn(tenderProcessEntity)

            val actual = strategy.check(cm).toJson()
            val expected = response()

            JsonValidator.equalsJsons(expectedJson = expected, actualJson = actual)
        }

        @Test
        @DisplayName("Check error when tender by cpid and stage not found.")
        fun tenderNotFound() {
            val dataRequest = request()
            val cm = commandMessage(data = dataRequest)

            whenever(
                tenderProcessDao.getByCpIdAndStage(
                    cpId = eq(ContextGenerator.CPID),
                    stage = eq(ContextGenerator.PREV_STAGE)
                )
            ).thenReturn(null)

            val exception = assertThrows<ErrorException> {
                strategy.check(cm)
            }

            assertEquals(ErrorType.DATA_NOT_FOUND, exception.error)
        }

        /**
         * VR-3.11.5
         */
        @Test
        @DisplayName("VR-3.11.5")
        fun vr_3_11_5() {
            val dataRequest = request()
            val cm = commandMessage(token = "UNKNOWN", data = dataRequest)

            val dataEntity = tenderProcessEntityData()
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = dataEntity)
            whenever(
                tenderProcessDao.getByCpIdAndStage(
                    cpId = eq(ContextGenerator.CPID),
                    stage = eq(ContextGenerator.STAGE)
                )
            ).thenReturn(tenderProcessEntity)

            val exception = assertThrows<ErrorException> {
                strategy.check(cm)
            }
            assertEquals(ErrorType.INVALID_TOKEN, exception.error, exception.message)
        }

        /**
         * VR-3.11.6
         */
        @Test
        @DisplayName("VR-3.11.6")
        fun vr_3_11_6() {
            val dataRequest = request()
            val cm = commandMessage(owner = "UNKNOWN", data = dataRequest)

            val dataEntity = tenderProcessEntityData()
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = dataEntity)
            whenever(
                tenderProcessDao.getByCpIdAndStage(
                    cpId = eq(ContextGenerator.CPID),
                    stage = eq(ContextGenerator.STAGE)
                )
            ).thenReturn(tenderProcessEntity)

            val exception = assertThrows<ErrorException> {
                strategy.check(cm)
            }
            assertEquals(ErrorType.INVALID_OWNER, exception.error, exception.message)
        }

        /**
         * VR-3.11.8
         */
        @Test
        @DisplayName("VR-3.11.8")
        fun vr_3_11_8() {
            val dataRequest = request()
            val cm = commandMessage(data = dataRequest)

            val dataEntity = tenderProcessEntityData()
                .toNode()
                .apply {
                    getObject("tender")
                        .getArray("lots")
                        .getObject(0)
                        .setAttribute("status", LotStatus.UNSUCCESSFUL.key)
                }
                .toJson()
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = dataEntity)
            whenever(
                tenderProcessDao.getByCpIdAndStage(
                    cpId = eq(ContextGenerator.CPID),
                    stage = eq(ContextGenerator.STAGE)
                )
            ).thenReturn(tenderProcessEntity)

            val exception = assertThrows<ErrorException> {
                strategy.check(cm)
            }
            assertEquals(ErrorType.AWARD_ON_LOT_IN_INVALID_STATUS, exception.error, exception.message)
        }

        @DisplayName("VR-3.11.9 ")
        @Nested
        inner class VR_3_11_9 {

            @Test
            @DisplayName("VR-3.11.9 Check amount value in award")
            fun vr_3_11_9_1() {
                val dataRequest = request().apply {
                    getObject("award", "value")
                        .setAttribute("amount", Long.MAX_VALUE.toDouble())
                }

                val cm = commandMessage(data = dataRequest)

                val dataEntity = tenderProcessEntityData()
                val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = dataEntity)
                whenever(
                    tenderProcessDao.getByCpIdAndStage(
                        cpId = eq(ContextGenerator.CPID),
                        stage = eq(ContextGenerator.STAGE)
                    )
                ).thenReturn(tenderProcessEntity)

                val exception = assertThrows<ErrorException> {
                    strategy.check(cm)
                }
                assertEquals(ErrorType.AWARD_HAS_INVALID_AMOUNT_VALUE, exception.error, exception.message)
            }

            @Test
            @DisplayName("VR-3.11.9 Check currency value in award")
            fun vr_3_11_9_2() {
                val dataRequest = request().apply {
                    getObject("award", "value")
                        .setAttribute("currency", "OTHER")
                }

                val cm = commandMessage(data = dataRequest)

                val dataEntity = tenderProcessEntityData()
                val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = dataEntity)
                whenever(
                    tenderProcessDao.getByCpIdAndStage(
                        cpId = eq(ContextGenerator.CPID),
                        stage = eq(ContextGenerator.STAGE)
                    )
                ).thenReturn(tenderProcessEntity)

                val exception = assertThrows<ErrorException> {
                    strategy.check(cm)
                }
                assertEquals(ErrorType.AWARD_HAS_INVALID_CURRENCY_VALUE, exception.error, exception.message)
            }
        }

        /**
         * VR-3.11.10
         */
        @Test
        @DisplayName("VR-3.11.10")
        fun vr_3_11_10() {
            val dataRequest = request()
            val cm = commandMessage(lotId = "UNKNOWN", data = dataRequest)

            val dataEntity = tenderProcessEntityData()
            val tenderProcessEntity = TenderProcessEntityGenerator.generate(data = dataEntity)
            whenever(
                tenderProcessDao.getByCpIdAndStage(
                    cpId = eq(ContextGenerator.CPID),
                    stage = eq(ContextGenerator.STAGE)
                )
            ).thenReturn(tenderProcessEntity)

            val exception = assertThrows<ErrorException> {
                strategy.check(cm)
            }
            assertEquals(ErrorType.AWARD_RELATED_TO_UNKNOWN_LOT, exception.error, exception.message)
        }
    }

    private fun commandMessage(
        token: String = TOKEN.toString(),
        owner: String = OWNER,
        lotId: String = "lot-1",
        data: JsonNode
    ): CommandMessage {
        val context = ContextGenerator.generate(
            token = token,
            owner = owner,
            id = lotId
        )
        return CommandMessageGenerator.generate(
            command = CommandTypeV1.CHECK_AWARD,
            context = context,
            data = data
        )
    }

    private fun request(): JsonNode {
        val pathToJsonFile = "json/service/check/award/request/lp/request_check_award.json".also {
            testingBindingAndMapping<CheckAwardRequest>(it)
        }
        return loadJson(pathToJsonFile).toNode()
    }

    private fun tenderProcessEntityData(): JSON {
        val pathToJsonFile = "json/service/check/award/entity/lp/entity_cn.json".also {
            testingBindingAndMapping<CNEntity>(it)
        }
        return loadJson(pathToJsonFile)
    }

    private fun response(): JSON {
        return CheckAwardResponse().toJson()
    }
}