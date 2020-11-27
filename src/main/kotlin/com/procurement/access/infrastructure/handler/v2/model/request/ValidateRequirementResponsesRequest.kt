package com.procurement.access.infrastructure.handler.v2.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.access.domain.model.requirement.response.RequirementRsValue
import com.procurement.access.infrastructure.bind.criteria.RequirementValueDeserializer
import com.procurement.access.infrastructure.bind.criteria.RequirementValueSerializer

data class ValidateRequirementResponsesRequest(
    @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: String,
    @field:JsonProperty("ocid") @param:JsonProperty("ocid") val ocid: String,
    @field:JsonProperty("requirementResponses") @param:JsonProperty("requirementResponses") val requirementResponses: List<RequirementResponse>,
    @field:JsonProperty("organizationIds") @param:JsonProperty("organizationIds") val organizationIds: List<String>,
    @field:JsonProperty("operationType") @param:JsonProperty("operationType") val operationType: String
) {
    data class RequirementResponse(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,

        @JsonDeserialize(using = RequirementValueDeserializer::class)
        @JsonSerialize(using = RequirementValueSerializer::class)
        @field:JsonProperty("value") @param:JsonProperty("value") val value: RequirementRsValue,

        @field:JsonProperty("requirement") @param:JsonProperty("requirement") val requirement: Requirement,

        @field:JsonProperty("relatedCandidate") @param:JsonProperty("relatedCandidate") val relatedCandidate: RelatedCandidate
    ) {

        data class Requirement(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String
        )

        data class RelatedCandidate(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
            @field:JsonProperty("name") @param:JsonProperty("name") val name: String
        )
    }
}
