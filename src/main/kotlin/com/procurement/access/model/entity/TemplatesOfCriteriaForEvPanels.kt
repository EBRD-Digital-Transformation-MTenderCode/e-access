package com.procurement.access.model.entity

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.access.domain.model.enums.CriteriaRelatesTo
import com.procurement.access.domain.model.enums.CriteriaSource
import com.procurement.access.domain.model.enums.RequirementDataType
import com.procurement.access.domain.model.requirement.RequirementValue
import com.procurement.access.infrastructure.bind.criteria.TemplateRequirementDeserializer
import com.procurement.access.infrastructure.bind.criteria.TemplateRequirementSerializer

class TemplatesOfCriteriaForEvPanels(values: List<Criterion> = emptyList()) :
    List<TemplatesOfCriteriaForEvPanels.Criterion> by values {

    data class Criterion(
        @field:JsonProperty("title") @param:JsonProperty("title") val title: String,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("source") @param:JsonProperty("source") val source: CriteriaSource? = null,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("relatesTo") @param:JsonProperty("relatesTo") val relatesTo: CriteriaRelatesTo?,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("description") @param:JsonProperty("description") val description: String?,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @field:JsonProperty("requirementGroups") @param:JsonProperty("requirementGroups") val requirementGroups: List<RequirementGroup>,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("classification") @param:JsonProperty("classification") val classification: Classification
    ) {

        data class Classification(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
            @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String
        )

        data class RequirementGroup(

            @JsonSerialize(using = TemplateRequirementSerializer::class)
            @JsonDeserialize(using = TemplateRequirementDeserializer::class)
            @field:JsonProperty("requirements") @param:JsonProperty("requirements") val requirements: List<Requirement>
        ) {

            class Requirement(
                val title: String,
                val dataType: RequirementDataType,
                val value: RequirementValue
            )
        }
    }
}

