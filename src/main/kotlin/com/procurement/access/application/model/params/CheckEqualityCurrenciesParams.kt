package com.procurement.access.application.model.params

import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseOcid
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.Result.Companion.success

class CheckEqualityCurrenciesParams private constructor(
    val cpid: Cpid,
    val ocid: Ocid,
    val relatedCpid: Cpid,
    val relatedOcid: Ocid
) {

    companion object {
        fun tryCreate(
            cpid: String,
            ocid: String,
            relatedCpid: String,
            relatedOcid: String
        ): Result<CheckEqualityCurrenciesParams, DataErrors.Validation.DataMismatchToPattern> {
            val parsedCpid = parseCpid(value = cpid)
                .orForwardFail { error -> return error }

            val parsedOcid = parseOcid(value = ocid)
                .orForwardFail { error -> return error }

            val parsedCpidAP = parseCpid(value = relatedCpid)
                .orForwardFail { error -> return error }

            val parsedOcidAP = parseOcid(value = relatedOcid)
                .orForwardFail { error -> return error }

            return success(
                CheckEqualityCurrenciesParams(
                    cpid = parsedCpid,
                    ocid = parsedOcid,
                    relatedCpid = parsedCpidAP,
                    relatedOcid = parsedOcidAP
                )
            )
        }
    }
}