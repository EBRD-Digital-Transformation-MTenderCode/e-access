package com.procurement.access.infrastructure.entity.process

import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.enums.RelatedProcessScheme
import com.procurement.access.domain.model.enums.RelatedProcessType
import com.procurement.access.domain.model.process.RelatedProcessId
import com.procurement.access.domain.model.process.RelatedProcessIdentifier

data class RelatedProcess(
    @field:JsonProperty("id") @param:JsonProperty("id") val id: RelatedProcessId,
    @field:JsonProperty("relationship") @param:JsonProperty("relationship") val relationship: List<RelatedProcessType>,
    @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: RelatedProcessScheme,
    @field:JsonProperty("identifier") @param:JsonProperty("identifier") val identifier: RelatedProcessIdentifier,
    @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String
)