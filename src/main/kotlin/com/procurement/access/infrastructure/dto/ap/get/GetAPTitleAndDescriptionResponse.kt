package com.procurement.access.infrastructure.dto.ap.get

import com.fasterxml.jackson.annotation.JsonProperty

data class GetAPTitleAndDescriptionResponse(
    @field:JsonProperty("title") @param:JsonProperty("title") val title: String,
    @field:JsonProperty("description") @param:JsonProperty("description") val description: String
)