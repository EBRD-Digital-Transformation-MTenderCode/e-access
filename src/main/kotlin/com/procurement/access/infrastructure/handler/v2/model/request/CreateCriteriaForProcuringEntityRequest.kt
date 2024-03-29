package com.procurement.access.infrastructure.handler.v2.model.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

data class CreateCriteriaForProcuringEntityRequest(
    @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: String,
    @field:JsonProperty("ocid") @param:JsonProperty("ocid") val ocid: String,
    @field:JsonProperty("date") @param:JsonProperty("date") val date: String,
    @field:JsonProperty("operationType") @param:JsonProperty("operationType") val operationType: String,
    @field:JsonProperty("criteria") @param:JsonProperty("criteria") val criteria: List<Criterion>
) {
    data class Criterion(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
        @field:JsonProperty("title") @param:JsonProperty("title") val title: String,

        @field:JsonProperty("classification") @param:JsonProperty("classification") val classification: Classification,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("description") @param:JsonProperty("description") val description: String?,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @field:JsonProperty("requirementGroups") @param:JsonProperty("requirementGroups") val requirementGroups: List<RequirementGroup>
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
                @field:JsonProperty("description") @param:JsonProperty("description") val description: String?
            )
        }
    }
}
