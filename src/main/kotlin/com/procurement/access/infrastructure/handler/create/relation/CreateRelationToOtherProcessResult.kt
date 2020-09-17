package com.procurement.access.infrastructure.handler.create.relation

import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.enums.RelatedProcessScheme
import com.procurement.access.domain.model.enums.RelatedProcessType
import com.procurement.access.domain.model.process.RelatedProcessId
import com.procurement.access.domain.util.Result
import com.procurement.access.infrastructure.entity.process.RelatedProcess as RelatedProcessDomain

data class CreateRelationToOtherProcessResult(
    @field:JsonProperty("relatedProcesses") @param:JsonProperty("relatedProcesses") val relatedProcesses: List<RelatedProcess>
) {
    data class RelatedProcess(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: RelatedProcessId,

        @field:JsonProperty("relationship") @param:JsonProperty("relationship") val relationship: List<RelatedProcessType>,
        @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: RelatedProcessScheme,
        @field:JsonProperty("identifier") @param:JsonProperty("identifier") val identifier: String,
        @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String
    )

    companion object {
        fun fromDomain(relatedProcess: RelatedProcessDomain): Result<RelatedProcess, DataErrors> =
            Result.success(
                RelatedProcess(
                    id = relatedProcess.id,
                    relationship = relatedProcess.relationship,
                    scheme = relatedProcess.scheme,
                    identifier = relatedProcess.identifier,
                    uri = relatedProcess.uri
                )
            )
    }
}
