package com.procurement.access.infrastructure.handler.processing.responder

import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.persone.PersonId

data class ResponderProcessingResult(
    @field:JsonProperty("id") @param:JsonProperty("id") val id: PersonId,
    @field:JsonProperty("name") @param:JsonProperty("name") val name: String
)
