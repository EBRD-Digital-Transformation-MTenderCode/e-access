package com.procurement.access.infrastructure.handler.check.auction

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

data class CheckExistenceSignAuctionRequest(
    @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: String,
    @field:JsonProperty("ocid") @param:JsonProperty("ocid") val ocid: String,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @field:JsonProperty("tender") @param:JsonProperty("tender") val tender: Tender?
) {
    data class Tender(
        @field:JsonProperty("procurementMethodModalities") @param:JsonProperty("procurementMethodModalities") val procurementMethodModalities: List<String>
    )
}
