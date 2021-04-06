package com.procurement.access.infrastructure.handler.v2.model.response


import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.enums.RelatedProcessType

data class CreateRelationToContractProcessStageResult(
    @param:JsonProperty("relatedProcesses") @field:JsonProperty("relatedProcesses") val relatedProcesses: List<RelatedProcess>
) {
    data class RelatedProcess(
        @param:JsonProperty("id") @field:JsonProperty("id") val id: String,
        @param:JsonProperty("relationship") @field:JsonProperty("relationship") val relationship: List<RelatedProcessType>,
        @param:JsonProperty("scheme") @field:JsonProperty("scheme") val scheme: String,
        @param:JsonProperty("identifier") @field:JsonProperty("identifier") val identifier: String,
        @param:JsonProperty("uri") @field:JsonProperty("uri") val uri: String
    )
}