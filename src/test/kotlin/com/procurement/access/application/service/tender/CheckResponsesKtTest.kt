package com.procurement.access.application.service.tender

import com.procurement.access.application.service.CheckResponsesData
import com.procurement.access.domain.model.enums.CriteriaRelatesToEnum
import com.procurement.access.domain.model.enums.RequirementDataType
import com.procurement.access.domain.model.requirement.response.RequirementRsValue
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.dto.cn.criteria.ExpectedValue
import com.procurement.access.infrastructure.dto.cn.criteria.Requirement
import com.procurement.access.infrastructure.entity.CNEntity
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

internal class CheckResponsesKtTest {

    @Nested
    inner class CheckAnswerByLotRequirementsTest {

        @Test
        @DisplayName("Success when response for bidded lot")
        fun responseForOnlyBiddedLot() {
            Assertions.assertDoesNotThrow {
                checkAnswerByLotRequirements(responseForOnlyBiddedLot, criteria, emptyList())
            }
        }

        @Test
        fun responseForBiddedLotAndTenderer() {
            Assertions.assertDoesNotThrow {
                checkAnswerByLotRequirements(responseForBiddedLotAndTenderer, criteria, emptyList())
            }
        }

        @Test
        fun responseForAllLotsAndTenderer() {
            val error = assertThrows<ErrorException> {
                checkAnswerByLotRequirements(responseForOnlyTenderer, criteria, emptyList())
            }
            assertEquals(ErrorType.INVALID_REQUIREMENT_VALUE, error.error)
            assertTrue(error.message!!.contains("missing", true))
        }

        @Test
        fun responseForNonBiddedLotsAndTenderer() {
            val error = assertThrows<ErrorException> {
                checkAnswerByLotRequirements(responseForNonBiddedLotsAndTenderer, criteria, emptyList())
            }
            assertEquals(ErrorType.INVALID_REQUIREMENT_VALUE, error.error)
            assertTrue(error.message!!.contains("redundant", true))
        }

        val responseForOnlyBiddedLot = CheckResponsesData(
            items = emptyList(),
            bid = CheckResponsesData.Bid(
                relatedLots = listOf("some-related-lot-1"),
                requirementResponses = listOf(
                    createResponse(requirementId = "req-lot-1")
                )
            )
        )

        val responseForBiddedLotAndTenderer = CheckResponsesData(
            items = emptyList(),
            bid = CheckResponsesData.Bid(
                relatedLots = listOf("some-related-lot-1"),
                requirementResponses = listOf(
                    createResponse(requirementId = "req-lot-1"),
                    createResponse(requirementId = "req-tenderer-3")
                )
            )
        )

        val responseForOnlyTenderer = CheckResponsesData(
            items = emptyList(),
            bid = CheckResponsesData.Bid(
                relatedLots = listOf("some-related-lot-1"),
                requirementResponses = listOf(
                    createResponse(requirementId = "req-tenderer-3")
                )
            )
        )
        val responseForNonBiddedLotsAndTenderer = CheckResponsesData(
            items = emptyList(),
            bid = CheckResponsesData.Bid(
                relatedLots = listOf("some-related-lot-1"),
                requirementResponses = listOf(
                    createResponse(requirementId = "req-tenderer-3"),
                    createResponse(requirementId = "req-lot-2")
                )
            )
        )

        val criteria = listOf(
            createCriteria(relatesTo = CriteriaRelatesToEnum.LOT, relatedLot = "some-related-lot-1", requirementId = "req-lot-1"),
            createCriteria(relatesTo = CriteriaRelatesToEnum.LOT, relatedLot = "some-related-lot-2", requirementId = "req-lot-2"),
            createCriteria(relatesTo = CriteriaRelatesToEnum.TENDERER, relatedLot = null, requirementId = "req-tenderer-3")
        )

        private fun createResponse(requirementId: String) =
            CheckResponsesData.Bid.RequirementResponse(
                id = UUID.randomUUID().toString(),
                title = "",
                description = null,
                period = null,
                value = RequirementRsValue.AsString(""),
                requirement = CheckResponsesData.Bid.RequirementResponse.Requirement(id = requirementId)
            )

        private fun createCriteria(relatesTo: CriteriaRelatesToEnum, relatedLot: String?, requirementId: String) =
            CNEntity.Tender.Criteria(
                id = UUID.randomUUID().toString(),
                title = "",
                source = null,
                description = null,
                relatesTo = relatesTo,
                relatedItem = relatedLot,
                requirementGroups = listOf(
                    CNEntity.Tender.Criteria.RequirementGroup(
                        id = UUID.randomUUID().toString(),
                        description = null,
                        requirements = listOf(
                            Requirement(
                                id = requirementId,
                                description = null,
                                title = "",
                                period = null,
                                dataType = RequirementDataType.STRING,
                                value = ExpectedValue.of("")
                            )
                        )
                    )
                )
            )


    }
}