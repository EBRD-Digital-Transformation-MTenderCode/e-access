package com.procurement.access.application.model.criteria

import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseOcid
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.util.Result

class CreateCriteriaForProcuringEntity {

    class Params private constructor(
        val cpid: Cpid,
        val ocid: Ocid,
        val criteria: List<Criterion>
    ) {
        companion object {

            fun tryCreate(cpid: String, ocid: String, criteria: List<Criterion>): Result<Params, DataErrors> {

                val cpidResult = parseCpid(value = cpid)
                    .orForwardFail { error -> return error }

                val ocidResult = parseOcid(value = ocid)
                    .orForwardFail { error -> return error }

                return Result.success(Params(cpid = cpidResult, ocid = ocidResult, criteria = criteria))
            }
        }

        class Criterion(
            val id: String,
            val description: String?,
            val title: String,
            val requirementGroups: List<RequirementGroup>
        )

        class RequirementGroup(val id: String, val description: String?, val requirements: List<Requirement>)
        class Requirement(val id: String, val description: String?, val title: String)
    }
}
