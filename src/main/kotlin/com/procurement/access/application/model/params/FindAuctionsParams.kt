package com.procurement.access.application.model.params

import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseOcid
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asSuccess

class FindAuctionsParams private constructor(val cpid: Cpid, val ocid: Ocid) {
    companion object {

        fun tryCreate(cpid: String, ocid: String): Result<FindAuctionsParams, DataErrors> {
            val cpidParsed = parseCpid(value = cpid)
                .orForwardFail { error -> return error }

            val ocidParsed = parseOcid(value = ocid)
                .orForwardFail { error -> return error }

            return FindAuctionsParams(cpid = cpidParsed, ocid = ocidParsed).asSuccess()
        }
    }
}
