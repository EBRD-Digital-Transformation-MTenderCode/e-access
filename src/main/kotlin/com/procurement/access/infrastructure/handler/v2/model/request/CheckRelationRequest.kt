package com.procurement.access.infrastructure.handler.v2.model.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

data class CheckRelationRequest(
    @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: String,
    @field:JsonProperty("ocid") @param:JsonProperty("ocid") val ocid: String,
    @field:JsonProperty("relatedCpid") @param:JsonProperty("relatedCpid") val relatedCpid: String,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @field:JsonProperty("relatedOcid") @param:JsonProperty("relatedOcid") val relatedOcid: String?,

    @field:JsonProperty("operationType") @param:JsonProperty("operationType") val operationType: String,
    @field:JsonProperty("existenceRelation") @param:JsonProperty("existenceRelation") val existenceRelation: Boolean
)