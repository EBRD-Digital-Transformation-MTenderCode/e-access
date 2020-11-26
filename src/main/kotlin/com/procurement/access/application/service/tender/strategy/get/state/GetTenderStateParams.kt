package com.procurement.access.application.service.tender.strategy.get.state

import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseOcid
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asSuccess

class GetTenderStateParams private constructor(
    val cpid: Cpid, val ocid: Ocid
) {
    companion object {
        fun tryCreate(
            cpid: String, ocid: String
        ): Result<GetTenderStateParams, DataErrors> {
            val cpidResult = parseCpid(value = cpid)
                .onFailure { error -> return error }
            val ocidResult = parseOcid(value = ocid)
                .onFailure { error -> return error }

            return GetTenderStateParams(
                cpid = cpidResult,
                ocid = ocidResult
            ).asSuccess()
        }
    }
}