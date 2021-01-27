package com.procurement.access.infrastructure.handler.v1.model.request.document

import com.fasterxml.jackson.annotation.JsonProperty

data class RelatedDocumentRequest(
    @field:JsonProperty("id") @param:JsonProperty("id") val id: String
)