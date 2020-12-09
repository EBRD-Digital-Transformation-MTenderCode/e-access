package com.procurement.access.application.model.params

import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.domain.model.enums.ProcurementMethod
import com.procurement.access.domain.model.lot.LotId

data class CheckLotsStateParams(
    val cpid: Cpid,
    val ocid: Ocid,
    val pmd: ProcurementMethod,
    val country: String,
    val operationType: OperationType,
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