package com.procurement.access.application.model.params

import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseOcid
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.Result.Companion.success

class CheckEqualPNAndAPCurrencyParams private constructor(
    val cpid: Cpid,
    val ocid: Ocid,
    val cpidAP: Cpid,
    val ocidAP: Ocid
) {

    companion object {
        fun tryCreate(
            cpid: String,
            ocid: String,
            cpidAP: String,
            ocidAP: String
        ): Result<CheckEqualPNAndAPCurrencyParams, DataErrors.Validation.DataMismatchToPattern> {
            val parsedCpid = parseCpid(value = cpid)
                .orForwardFail { error -> return error }

            val parsedOcid = parseOcid(value = ocid)
                .orForwardFail { error -> return error }

            val parsedCpidAP = parseCpid(value = cpidAP)
                .orForwardFail { error -> return error }

            val parsedOcidAP = parseOcid(value = ocidAP)
                .orForwardFail { error -> return error }

            return success(
                CheckEqualPNAndAPCurrencyParams(
                    cpid = parsedCpid,
                    ocid = parsedOcid,
                    cpidAP = parsedCpidAP,
                    ocidAP = parsedOcidAP
                )
            )
        }
    }
}