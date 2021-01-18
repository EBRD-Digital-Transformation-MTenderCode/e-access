package com.procurement.access.infrastructure.handler.v2.model.response

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.enums.CriteriaRelatesTo
import com.procurement.access.domain.model.enums.CriteriaSource
import com.procurement.access.domain.model.enums.RequirementDataType
import com.procurement.access.domain.model.enums.RequirementStatus
import java.time.LocalDateTime

class CreateCriteriaForProcuringEntityResult(values: List<Criterion>) : List<CreateCriteriaForProcuringEntityResult.Criterion> by values {
    data class Criterion(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
        @field:JsonProperty("title") @param:JsonProperty("title") val title: String,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("description") @param:JsonProperty("description") val description: String?,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @field:JsonProperty("requirementGroups") @param:JsonProperty("requirementGroups") val requirementGroups: List<RequirementGroup>,

        @field:JsonProperty("source") @param:JsonProperty("source") val source: CriteriaSource,

        @field:JsonProperty("relatesTo") @param:JsonProperty("relatesTo") val relatesTo: CriteriaRelatesTo,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("classification") @param:JsonProperty("classification") val classification: Classification?
    ) {

        data class Classification(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
            @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String
        )

        data class RequirementGroup(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @field:JsonProperty("description") @param:JsonProperty("description") val description: String?,

            @field:JsonProperty("requirements") @param:JsonProperty("requirements") val requirements: List<Requirement>
        ) {

            data class Requirement(
                @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                @field:JsonProperty("title") @param:JsonProperty("title") val title: String,

                @JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("description") @param:JsonProperty("description") val description: String?,

                @JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("status") @param:JsonProperty("status") val status: RequirementStatus?,

                @JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("datePublished") @param:JsonProperty("datePublished") val datePublished: LocalDateTime?,

                @field:JsonProperty("dataType") @param:JsonProperty("dataType") val dataType: RequirementDataType
            )
        }
    }
}
