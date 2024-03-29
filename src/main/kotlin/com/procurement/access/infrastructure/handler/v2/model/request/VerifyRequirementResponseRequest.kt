package com.procurement.access.infrastructure.handler.v2.model.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.access.domain.model.requirement.response.RequirementRsValue
import com.procurement.access.infrastructure.bind.criteria.RequirementResponseValueDeserializer
import com.procurement.access.infrastructure.bind.criteria.RequirementResponseValueSerializer

class VerifyRequirementResponseRequest {

    data class Params(
        @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: String,
        @field:JsonProperty("ocid") @param:JsonProperty("ocid") val ocid: String,
        @field:JsonProperty("requirementResponseId") @param:JsonProperty("requirementResponseId") val requirementResponseId: String,

        @JsonDeserialize(using = RequirementResponseValueDeserializer::class)
        @JsonSerialize(using = RequirementResponseValueSerializer::class)
        @field:JsonProperty("value") @param:JsonProperty("value") val value: RequirementRsValue,

        @field:JsonProperty("responder") @param:JsonProperty("responder") val responder: Responder,
        @field:JsonProperty("requirementId") @param:JsonProperty("requirementId") val requirementId: String
    ) {
        data class Responder(
            @field:JsonProperty("title") @param:JsonProperty("title") val title: String,
            @field:JsonProperty("name") @param:JsonProperty("name") val name: String,
            @field:JsonProperty("identifier") @param:JsonProperty("identifier") val identifier: Identifier,
            @field:JsonProperty("businessFunctions") @param:JsonProperty("businessFunctions") val businessFunctions: List<BusinessFunction>
        ) {
            data class Identifier(
                @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String,
                @field:JsonProperty("id") @param:JsonProperty("id") val id: String,

                @JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String?
            )

            data class BusinessFunction(
                @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                @field:JsonProperty("type") @param:JsonProperty("type") val type: String,
                @field:JsonProperty("jobTitle") @param:JsonProperty("jobTitle") val jobTitle: String,
                @field:JsonProperty("period") @param:JsonProperty("period") val period: Period,

                @JsonInclude(JsonInclude.Include.NON_EMPTY)
                @field:JsonProperty("documents") @param:JsonProperty("documents") val documents: List<Document>?
            ) {
                data class Period(
                    @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: String
                )

                data class Document(
                    @field:JsonProperty("documentType") @param:JsonProperty("documentType") val documentType: String,
                    @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                    @field:JsonProperty("title") @param:JsonProperty("title") val title: String,

                    @JsonInclude(JsonInclude.Include.NON_NULL)
                    @field:JsonProperty("description") @param:JsonProperty("description") val description: String?
                )
            }
        }
    }
}
