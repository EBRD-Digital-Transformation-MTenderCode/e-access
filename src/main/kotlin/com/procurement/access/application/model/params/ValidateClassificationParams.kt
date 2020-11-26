package com.procurement.access.application.model.params

import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseOcid
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asSuccess

class ValidateClassificationParams private constructor(
    val cpid: Cpid,
    val ocid: Ocid,
    val tender: Tender
) {
    companion object {
        fun tryCreate(
            cpid: String,
            ocid: String,
            tender: Tender
        ): Result<ValidateClassificationParams, DataErrors> {
            val cpidParsed = parseCpid(value = cpid)
                .onFailure { error -> return error }

            val ocidParsed = parseOcid(value = ocid)
                .onFailure { error -> return error }

            return ValidateClassificationParams(
                cpid = cpidParsed,
                ocid = ocidParsed,
                tender = tender

            ).asSuccess()
        }
    }
    data class Tender(
         val classification: Classification
    ) {
        data class Classification(
             val id: String
        )
    }
}