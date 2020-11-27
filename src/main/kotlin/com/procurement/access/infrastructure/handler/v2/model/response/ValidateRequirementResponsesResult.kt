package com.procurement.access.infrastructure.handler.v2.model.response

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.access.domain.model.requirement.RequirementId
import com.procurement.access.domain.model.requirement.response.RequirementResponseId
import com.procurement.access.domain.model.requirement.response.RequirementRsValue
import com.procurement.access.infrastructure.bind.criteria.RequirementValueDeserializer
import com.procurement.access.infrastructure.bind.criteria.RequirementValueSerializer

class ValidateRequirementResponsesResult(values: List<RequirementResponse>) : List<ValidateRequirementResponsesResult.RequirementResponse> by values {
    data class RequirementResponse(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: RequirementResponseId,

        @JsonDeserialize(using = RequirementValueDeserializer::class)
        @JsonSerialize(using = RequirementValueSerializer::class)
        @field:JsonProperty("value") @param:JsonProperty("value") val value: RequirementRsValue,

        @field:JsonProperty("requirement") @param:JsonProperty("requirement") val requirement: Requirement,

        @field:JsonProperty("relatedCandidate") @param:JsonProperty("relatedCandidate") val relatedCandidate: RelatedCandidate
    ) {

        data class Requirement(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: RequirementId
        )

        data class RelatedCandidate(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
            @field:JsonProperty("name") @param:JsonProperty("name") val name: String
        )
    }
}