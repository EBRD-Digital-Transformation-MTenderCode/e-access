package com.procurement.access.infrastructure.handler.v2.model.request


import com.fasterxml.jackson.annotation.JsonProperty

data class AddClientsToPartiesInAPRequest(
    @param:JsonProperty("cpid") @field:JsonProperty("cpid") val cpid: String,
    @param:JsonProperty("ocid") @field:JsonProperty("ocid") val ocid: String,
    @param:JsonProperty("relatedCpid") @field:JsonProperty("relatedCpid") val relatedCpid: String,
    @param:JsonProperty("relatedOcid") @field:JsonProperty("relatedOcid") val relatedOcid: String
)