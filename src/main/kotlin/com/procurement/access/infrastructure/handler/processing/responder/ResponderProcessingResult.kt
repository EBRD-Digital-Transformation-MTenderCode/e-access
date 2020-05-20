package com.procurement.access.infrastructure.handler.processing.responder

import com.fasterxml.jackson.annotation.JsonProperty

data class ResponderProcessingResult(
    @field:JsonProperty("name") @param:JsonProperty("name") val name: String,
    @field:JsonProperty("identifier") @param:JsonProperty("identifier") val identifier: Identifier
) {
    data class Identifier(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
        @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String
    )
}
