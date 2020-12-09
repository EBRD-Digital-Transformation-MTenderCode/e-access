package com.procurement.access.application.service.tender

import com.procurement.access.application.service.CheckResponsesData
import com.procurement.access.domain.model.enums.CriteriaRelatesToEnum
import com.procurement.access.domain.model.enums.RequirementDataType
import com.procurement.access.domain.model.requirement.ExpectedValue
import com.procurement.access.domain.model.requirement.Requirement
import com.procurement.access.domain.model.requirement.response.RequirementRsValue
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
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
        @DisplayName("Success when response for bidded lot and tenderer")
        fun responseForBiddedLotAndTenderer() {
            Assertions.assertDoesNotThrow {
                checkAnswerByLotRequirements(responseForBiddedLotAndTenderer, criteria, emptyList())
            }
        }

        @Test
        @DisplayName("Error when response for tenderer only")
        fun responseForOnlyTenderer() {
            val error = assertThrows<ErrorException> {
                checkAnswerByLotRequirements(responseForOnlyTenderer, criteria, emptyList())
            }
            assertEquals(ErrorType.INVALID_REQUIREMENT_VALUE, error.error)
            assertTrue(error.message!!.contains("missing", true))
        }

        @Test
        @DisplayName("Error when response for non bidded lot and tenderer")
        fun responseForNonBiddedLotsAndTenderer() {
            val error = assertThrows<ErrorException> {
                checkAnswerByLotRequirements(responseForNonBiddedLotsAndTenderer, criteria, emptyList())
            }
            assertEquals(ErrorType.INVALID_REQUIREMENT_VALUE, error.error)
            assertTrue(error.message!!.contains("redundant", true))
        }

        private val TARGET_LOT = "some-related-lot-1"
        private val TARGET_REQUIREMENT = "req-lot-1"
        private val ANOTHER_LOT = "some-related-lot-2"
        private val ANOTHER_REQUIREMENT = "req-lot-2"
        private val TENDERER_REQUIREMENT = "req-tenderer-3"

        val responseForOnlyBiddedLot = CheckResponsesData(
            items = emptyList(),
            bid = CheckResponsesData.Bid(
                relatedLots = listOf(TARGET_LOT),
                requirementResponses = listOf(
                    createResponse(requirementId = TARGET_REQUIREMENT)
                )
            )
        )

        val responseForBiddedLotAndTenderer = CheckResponsesData(
            items = emptyList(),
            bid = CheckResponsesData.Bid(
                relatedLots = listOf(TARGET_LOT),
                requirementResponses = listOf(
                    createResponse(requirementId = TARGET_REQUIREMENT),
                    createResponse(requirementId = TENDERER_REQUIREMENT)
                )
            )
        )

        val responseForOnlyTenderer = CheckResponsesData(
            items = emptyList(),
            bid = CheckResponsesData.Bid(
                relatedLots = listOf(TARGET_LOT),
                requirementResponses = listOf(
                    createResponse(requirementId = TENDERER_REQUIREMENT)
                )
            )
        )
        val responseForNonBiddedLotsAndTenderer = CheckResponsesData(
            items = emptyList(),
            bid = CheckResponsesData.Bid(
                relatedLots = listOf(TARGET_LOT),
                requirementResponses = listOf(
                    createResponse(requirementId = TENDERER_REQUIREMENT),
                    createResponse(requirementId = ANOTHER_REQUIREMENT)
                )
            )
        )

        val criteria = listOf(
            createCriteria(relatesTo = CriteriaRelatesToEnum.LOT, relatedLot = TARGET_LOT, requirementId = TARGET_REQUIREMENT),
            createCriteria(relatesTo = CriteriaRelatesToEnum.LOT, relatedLot = ANOTHER_LOT, requirementId = ANOTHER_REQUIREMENT),
            createCriteria(relatesTo = CriteriaRelatesToEnum.TENDERER, relatedLot = null, requirementId = TENDERER_REQUIREMENT)
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