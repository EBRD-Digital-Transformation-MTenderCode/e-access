package com.procurement.access.application.service.tender.strategy.get.items

import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseOcid
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asSuccess

class GetItemsByLotIdsParams private constructor(
    val cpid: Cpid,
    val ocid: Ocid.SingleStage,
    val tender: Tender
) {
    companion object {
        fun tryCreate(cpid: String, ocid: String, tender: Tender): Result<GetItemsByLotIdsParams, DataErrors> {
            val cpidResult = parseCpid(value = cpid)
                .onFailure { error -> return error }

            val ocidResult = parseOcid(value = ocid)
                .onFailure { error -> return error }

            return GetItemsByLotIdsParams(cpid = cpidResult, ocid = ocidResult, tender = tender).asSuccess()
        }
    }

    data class Tender(val lots: List<Lot>) {
        data class Lot(val id: String)
    }
}

