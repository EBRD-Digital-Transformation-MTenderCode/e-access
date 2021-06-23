package com.procurement.access.application.model.params


import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.lot.LotId

data class GetDataForContractParams(
    @param:JsonProperty("relatedCpid") @field:JsonProperty("relatedCpid") val relatedCpid: Cpid,
    @param:JsonProperty("relatedOcid") @field:JsonProperty("relatedOcid") val relatedOcid: Ocid,
    @param:JsonProperty("awards") @field:JsonProperty("awards") val awards: List<Award>
) {
    data class Award(
        @param:JsonProperty("relatedLots") @field:JsonProperty("relatedLots") val relatedLots: List<LotId>
    )
}