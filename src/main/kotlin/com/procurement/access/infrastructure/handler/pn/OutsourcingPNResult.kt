package com.procurement.access.infrastructure.handler.pn

import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.enums.RelatedProcessScheme
import com.procurement.access.domain.model.enums.RelatedProcessType
import com.procurement.access.domain.model.process.RelatedProcessId

data class OutsourcingPNResult(
    @field:JsonProperty("relatedProcesses") @param:JsonProperty("relatedProcesses") val relatedProcesses: List<RelatedProcess>
) {
    data class RelatedProcess(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: RelatedProcessId,

        @field:JsonProperty("relationship") @param:JsonProperty("relationship") val relationship: List<RelatedProcessType>,
        @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: RelatedProcessScheme,
        @field:JsonProperty("identifier") @param:JsonProperty("identifier") val identifier: String,
        @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String
    )
}
