package com.procurement.access.application.model.params

import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.lot.LotId

data class GetDataForContractParams(
    val relatedCpid: Cpid,
    val relatedOcid: Ocid.SingleStage,
    val awards: List<Award>
) {
    data class Award(
        val relatedLots: List<LotId>
    )
}