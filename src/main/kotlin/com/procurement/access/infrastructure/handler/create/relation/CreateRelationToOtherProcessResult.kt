package com.procurement.access.infrastructure.handler.create.relation

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.enums.RelatedProcessScheme
import com.procurement.access.domain.model.enums.RelatedProcessType
import com.procurement.access.domain.model.process.RelatedProcessId
import com.procurement.access.domain.util.Result

import com.procurement.access.infrastructure.entity.process.RelatedProcess as RelatedProcessDomain

data class CreateRelationToOtherProcessResult(
    @field:JsonProperty("relatedProcesses") @param:JsonProperty("relatedProcesses") val relatedProcesses: List<RelatedProcess>,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @field:JsonProperty("tender") @param:JsonProperty("tender") val tender: Tender?
) {
    data class RelatedProcess(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: RelatedProcessId,

        @field:JsonProperty("relationship") @param:JsonProperty("relationship") val relationship: List<RelatedProcessType>,
        @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: RelatedProcessScheme,
        @field:JsonProperty("identifier") @param:JsonProperty("identifier") val identifier: String,
        @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String
    )

    data class Tender(
        @field:JsonProperty("procedureOutsourcing") @param:JsonProperty("procedureOutsourcing") val procedureOutsourcing: ProcedureOutsourcing
    ) {
        data class ProcedureOutsourcing(
            @field:JsonProperty("procedureOutsourced") @param:JsonProperty("procedureOutsourced") val procedureOutsourced: Boolean
        )
    }

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
