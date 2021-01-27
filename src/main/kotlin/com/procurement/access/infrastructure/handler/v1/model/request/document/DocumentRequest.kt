package com.procurement.access.infrastructure.handler.v1.model.request.document

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.enums.DocumentType

data class DocumentRequest(
    @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
    @field:JsonProperty("documentType") @param:JsonProperty("documentType") val documentType: DocumentType,
    @field:JsonProperty("title") @param:JsonProperty("title") val title: String,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @field:JsonProperty("description") @param:JsonProperty("description") val description: String?,

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @field:JsonProperty("relatedLots") @param:JsonProperty("relatedLots") val relatedLots: List<String>?
)
