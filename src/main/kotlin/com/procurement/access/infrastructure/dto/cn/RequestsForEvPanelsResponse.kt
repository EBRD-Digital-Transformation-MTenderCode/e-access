package com.procurement.access.infrastructure.dto.cn

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.access.domain.model.enums.CriteriaRelatesToEnum
import com.procurement.access.domain.model.enums.CriteriaSource
import com.procurement.access.infrastructure.bind.criteria.RequirementDeserializer
import com.procurement.access.infrastructure.bind.criteria.RequirementSerializer
import com.procurement.access.infrastructure.dto.cn.criteria.Requirement

data class RequestsForEvPanelsResponse(
    @field:JsonProperty("criteria") @param:JsonProperty("criteria") val criteria: Criteria
) {
    data class Criteria(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
        @field:JsonProperty("title") @param:JsonProperty("title") val title: String,
        @field:JsonProperty("source") @param:JsonProperty("source") val source: CriteriaSource,
        @field:JsonProperty("relatesTo") @param:JsonProperty("relatesTo") val relatesTo: CriteriaRelatesToEnum,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("description") @param:JsonProperty("description") val description: String?,

        @field:JsonProperty("requirementGroups") @param:JsonProperty("requirementGroups") val requirementGroups: List<RequirementGroup>
    ) {
        data class RequirementGroup(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,

            @JsonDeserialize(using = RequirementDeserializer::class)
            @JsonSerialize(using = RequirementSerializer::class)
            @field:JsonProperty("requirements") @param:JsonProperty("requirements") val requirements: List<Requirement>
        )
    }
}