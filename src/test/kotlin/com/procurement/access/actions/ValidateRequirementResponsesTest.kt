package com.procurement.access.actions

import com.procurement.access.application.service.requirement.ValidateRequirementResponsesParams.RequirementResponse
import com.procurement.access.domain.model.requirement.response.RequirementRsValue
import com.procurement.access.service.validateOneAnswerOnRequirementByCandidate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ValidateRequirementResponsesTest {

    @Test
    @DisplayName("Answered twice on requirement")
    fun answeredTwice() {

        val FIRST_CANDIDATE_ID = "candidate-1"
        val SECOND_CANDIDATE_ID = "candidate-2"

        val requirementResponses = listOf(
            RequirementResponseFactory.create(relatedCandidate = FIRST_CANDIDATE_ID),
            RequirementResponseFactory.create(relatedCandidate = FIRST_CANDIDATE_ID),
            RequirementResponseFactory.create(relatedCandidate = SECOND_CANDIDATE_ID)
        )

        val result = validateOneAnswerOnRequirementByCandidate(requirementResponses)

        assertTrue(result.isFail)
        val fail = result.error
        assertEquals(FIRST_CANDIDATE_ID, fail.candidateId)
        assertEquals(requirementResponses[0].requirement.id, fail.requirementId)
    }

    @Test
    @DisplayName("One answer per requirement")
    fun oneAnswerPerRequirement() {

        val FIRST_CANDIDATE_ID = "candidate-1"
        val SECOND_CANDIDATE_ID = "candidate-2"
        val THIRD_CANDIDATE_ID = "candidate-3"

        val requirementResponses = listOf(
            RequirementResponseFactory.create(relatedCandidate = FIRST_CANDIDATE_ID),
            RequirementResponseFactory.create(relatedCandidate = SECOND_CANDIDATE_ID),
            RequirementResponseFactory.create(relatedCandidate = THIRD_CANDIDATE_ID)
        )

        val result = validateOneAnswerOnRequirementByCandidate(requirementResponses)
        assertTrue(result.isSuccess)
    }

    @Test
    @DisplayName("Empty list of requirement responses")
    fun noRequirementResponses() {

        val requirementResponses = emptyList<RequirementResponse>()

        val result = validateOneAnswerOnRequirementByCandidate(requirementResponses)
        assertTrue(result.isSuccess)
    }

    class RequirementResponseFactory {
        companion object {
            private val IDENTIFIER_UUID: UUID = UUID.randomUUID()

            val sampleRequirement = RequirementResponse.Requirement
                .tryCreate(id = IDENTIFIER_UUID.toString())
                .get

            val sampleValue = RequirementRsValue.AsBoolean(true)

            fun create(
                id: UUID = UUID.randomUUID(),
                relatedCandidate: String,
                value: RequirementRsValue = sampleValue,
                requirement: RequirementResponse.Requirement = sampleRequirement
            ): RequirementResponse {
                return RequirementResponse.tryCreate(
                    id = id.toString(),
                    relatedCandidate = RequirementResponse.RelatedCandidate(id = relatedCandidate, name = ""),
                    value = value,
                    requirement = requirement
                ).get
            }
        }
    }
}