package com.procurement.access.application.model.criteria

import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseEnum
import com.procurement.access.application.model.parseOcid
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.CriteriaSource
import com.procurement.access.domain.util.Result

class FindCriteria {

    class Params private constructor(val cpid: Cpid, val ocid: Ocid, val source: CriteriaSource) {
        companion object {
            private val allowedSources = CriteriaSource.allowedElements
                .filter { source ->
                    when (source) {
                        CriteriaSource.TENDERER,
                        CriteriaSource.PROCURING_ENTITY -> true
                        CriteriaSource.BUYER            -> false
                    }
                }
                .toSet()

            fun tryCreate(cpid: String, ocid: String, source: String): Result<Params, DataErrors> {
                val cpidResult = parseCpid(value = cpid)
                    .orForwardFail { error -> return error }

                val ocidResult = parseOcid(value = ocid)
                    .orForwardFail { error -> return error }

                val parsedSource = parseEnum(
                    value = source,
                    attributeName = "source",
                    allowedEnums = allowedSources,
                    target = CriteriaSource.Companion
                )
                    .orForwardFail { error -> return error }

                return Result.success(Params(cpid = cpidResult, ocid = ocidResult, source = parsedSource))
            }
        }
    }
}
