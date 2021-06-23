package com.procurement.access.infrastructure.handler.v2.model.request


import com.fasterxml.jackson.annotation.JsonProperty

data class GetDataForContractRequest(
    @param:JsonProperty("relatedCpid") @field:JsonProperty("relatedCpid") val relatedCpid: String,
    @param:JsonProperty("relatedOcid") @field:JsonProperty("relatedOcid") val relatedOcid: String,
    @param:JsonProperty("awards") @field:JsonProperty("awards") val awards: List<Award>
) {
    data class Award(
        @param:JsonProperty("relatedLots") @field:JsonProperty("relatedLots") val relatedLots: List<String>
    )
}