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

    companion object {
        private val IDENTIFIER_UUID: UUID = UUID.randomUUID()
    }

    @Test
    @DisplayName("Answered twice on requirement")
    fun answeredTwice() {
        val requirement = RequirementResponse.Requirement
            .tryCreate(id = IDENTIFIER_UUID.toString())
            .get

        val sampleValue = RequirementRsValue.AsBoolean(true)
        val FIRST_CANDIDATE_ID = "candidate-1"
        val SECOND_CANDIDATE_ID = "candidate-2"

        val requirementResponses = listOf(
            RequirementResponse.tryCreate(
                id = UUID.randomUUID().toString(),
                relatedCandidate = RequirementResponse.RelatedCandidate(id = FIRST_CANDIDATE_ID, name = ""),
                value = sampleValue,
                requirement = requirement
            ).get,
            RequirementResponse.tryCreate(
                id = UUID.randomUUID().toString(),
                relatedCandidate = RequirementResponse.RelatedCandidate(id = FIRST_CANDIDATE_ID, name = ""),
                value = sampleValue,
                requirement = requirement
            ).get,
            RequirementResponse.tryCreate(
                id = UUID.randomUUID().toString(),
                relatedCandidate = RequirementResponse.RelatedCandidate(id = SECOND_CANDIDATE_ID, name = ""),
                value = sampleValue,
                requirement = requirement
            ).get
        )

        val result = validateOneAnswerOnRequirementByCandidate(requirementResponses)

        assertTrue(result.isFail)
        val fail = result.error
        assertEquals(FIRST_CANDIDATE_ID, fail.candidateId)
        assertEquals(requirement.id, fail.requirementId)
    }

    @Test
    @DisplayName("One answer per requirement")
    fun oneAnswerPerRequirement() {
        val requirement = RequirementResponse.Requirement
            .tryCreate(id = IDENTIFIER_UUID.toString())
            .get

        val sampleValue = RequirementRsValue.AsBoolean(true)
        val FIRST_CANDIDATE_ID = "candidate-1"
        val SECOND_CANDIDATE_ID = "candidate-2"
        val THIRD_CANDIDATE_ID = "candidate-3"

        val requirementResponses = listOf(
            RequirementResponse.tryCreate(
                id = UUID.randomUUID().toString(),
                relatedCandidate = RequirementResponse.RelatedCandidate(id = FIRST_CANDIDATE_ID, name = ""),
                value = sampleValue,
                requirement = requirement
            ).get,
            RequirementResponse.tryCreate(
                id = UUID.randomUUID().toString(),
                relatedCandidate = RequirementResponse.RelatedCandidate(id = SECOND_CANDIDATE_ID, name = ""),
                value = sampleValue,
                requirement = requirement
            ).get,
            RequirementResponse.tryCreate(
                id = UUID.randomUUID().toString(),
                relatedCandidate = RequirementResponse.RelatedCandidate(id = THIRD_CANDIDATE_ID, name = ""),
                value = sampleValue,
                requirement = requirement
            ).get
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
}