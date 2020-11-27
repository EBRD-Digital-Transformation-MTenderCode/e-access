package com.procurement.access.infrastructure.handler.v1.model.request

import com.fasterxml.jackson.annotation.JsonProperty

class ItemReferenceRequest(
    @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
    @field:JsonProperty("relatedLot") @param:JsonProperty("relatedLot") val relatedLot: String
)
