package com.procurement.access.application.model.params

import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseOcid
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.Result.Companion.success

class OutsourcingPNParams(
    val cpid: Cpid,
    val ocid: Ocid.SingleStage,
    val cpidFA: Cpid
) {

    companion object {
        fun tryCreate(cpid: String, ocid: String, cpidFA: String): Result<OutsourcingPNParams, DataErrors.Validation.DataMismatchToPattern> {
            val cpidResult = parseCpid(value = cpid)
                .onFailure { error -> return error }

            val cpidFAResult = parseCpid(value = cpid)
                .onFailure { error -> return error }

            val ocidResult = parseOcid(value = ocid)
                .onFailure { error -> return error }

            return success(OutsourcingPNParams(cpid = cpidResult, ocid = ocidResult, cpidFA = cpidFAResult))
        }
    }
}