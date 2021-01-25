package com.procurement.access.infrastructure.handler.v1.model.response

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.access.domain.model.enums.CriteriaRelatesTo
import com.procurement.access.domain.model.enums.CriteriaSource
import com.procurement.access.domain.model.requirement.Requirement
import com.procurement.access.infrastructure.bind.criteria.RequirementDeserializer
import com.procurement.access.infrastructure.bind.criteria.RequirementSerializer

data class GetCriteriaForTendererResponse(
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @field:JsonProperty("criteria") @param:JsonProperty("criteria") val criteria: List<Criterion>
) {
    data class Criterion(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
        @field:JsonProperty("title") @param:JsonProperty("title") val title: String,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("classification") @param:JsonProperty("classification") val classification: Classification?,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("source") @param:JsonProperty("source") val source: CriteriaSource? = null,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("description") @param:JsonProperty("description") val description: String?,
        @field:JsonProperty("requirementGroups") @param:JsonProperty("requirementGroups") val requirementGroups: List<RequirementGroup>,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("relatesTo") @param:JsonProperty("relatesTo") val relatesTo: CriteriaRelatesTo?,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("relatedItem") @param:JsonProperty("relatedItem") val relatedItem: String?
    ) {

        data class Classification(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
            @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String
        )

        data class RequirementGroup(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @field:JsonProperty("description") @param:JsonProperty("description") val description: String?,

            @JsonDeserialize(using = RequirementDeserializer::class)
            @JsonSerialize(using = RequirementSerializer::class)
            @field:JsonProperty("requirements") @param:JsonProperty("requirements") val requirements: List<Requirement>
        )
    }
}