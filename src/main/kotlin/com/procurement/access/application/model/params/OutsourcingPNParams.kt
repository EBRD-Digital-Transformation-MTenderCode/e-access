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
    val ocid: Ocid,
    val cpidFA: String
) {

    companion object {
        fun tryCreate(cpid: String, ocid: String, cpidFA: String): Result<OutsourcingPNParams, DataErrors.Validation.DataMismatchToPattern> {
            val cpidResult = parseCpid(value = cpid)
                .orForwardFail { error -> return error }

            val ocidResult = parseOcid(value = ocid)
                .orForwardFail { error -> return error }

            return success(OutsourcingPNParams(cpid = cpidResult, ocid = ocidResult, cpidFA = cpidFA))
        }
    }
}