package com.procurement.access.application.model.params

import com.procurement.access.application.model.parseCpid
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.asSuccess

data class CheckExistenceFAParams private constructor(
    val cpid: Cpid
) {
    companion object {
        fun tryCreate(cpid: String): Result<CheckExistenceFAParams, DataErrors> {
            val cpidParsed = parseCpid(value = cpid)
                .orForwardFail { error -> return error }

            return CheckExistenceFAParams(cpid = cpidParsed).asSuccess()
        }
    }
}