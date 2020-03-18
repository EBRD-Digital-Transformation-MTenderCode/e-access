package com.procurement.access.infrastructure.handler.processing.responder

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.document.DocumentId
import com.procurement.access.domain.model.enums.BusinessFunctionDocumentType
import com.procurement.access.domain.model.enums.BusinessFunctionType
import java.time.LocalDateTime

data class ResponderProcessingRequest(
    @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: String,
    @field:JsonProperty("ocid") @param:JsonProperty("ocid") val ocid: String,
    @field:JsonProperty("responder") @param:JsonProperty("responder") val responder: Responder
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
            @field:JsonProperty("type") @param:JsonProperty("type") val type: BusinessFunctionType,
            @field:JsonProperty("jobTitle") @param:JsonProperty("jobTitle") val jobTitle: String,
            @field:JsonProperty("period") @param:JsonProperty("period") val period: Period,

            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            @field:JsonProperty("documents") @param:JsonProperty("documents") val documents: List<Document>?
        ) {
            data class Period(
                @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime
            )

            data class Document(
                @field:JsonProperty("id") @param:JsonProperty("id") val id: DocumentId,
                @field:JsonProperty("documentType") @param:JsonProperty("documentType") val documentType: BusinessFunctionDocumentType,
                @field:JsonProperty("title") @param:JsonProperty("title") val title: String,

                @JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("description") @param:JsonProperty("description") val description: String?
            )
        }
    }
}
