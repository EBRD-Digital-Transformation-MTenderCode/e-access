package com.procurement.access.application.model.params

import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.lot.LotId

data class GetLotsValueParams(
    val cpid: Cpid,
    val ocid: Ocid,
    val tender: Tender
) {
    data class Tender(
        val lots: List<Lot>
    ) {
        data class Lot(
            val id: LotId
        )
    }
}