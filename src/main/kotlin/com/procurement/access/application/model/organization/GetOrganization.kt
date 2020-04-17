package com.procurement.access.application.model.organization

import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseOcid
import com.procurement.access.application.model.parseOrganizationRole
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.OrganizationRole
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.Result.Companion.failure

class GetOrganization {

    class Params private constructor(
        val cpid: Cpid,
        val ocid: Ocid,
        val role: OrganizationRole
    ) {
        companion object {
            private val allowedRoles = OrganizationRole.allowedElements
                .filter { value ->
                    when (value) {
                        OrganizationRole.PROCURING_ENTITY -> true
                    }
                }.toSet()

            fun tryCreate(
                cpid: String,
                ocid: String,
                role: String
            ): Result<Params, DataErrors> {

                val cpidResult = parseCpid(value = cpid)
                    .doOnError { error -> return failure(error) }
                    .get

                val ocidResult = parseOcid(value = ocid)
                    .doOnError { error -> return failure(error) }
                    .get

                val roleParsed = parseOrganizationRole(
                    role = role,
                    allowedValues = allowedRoles,
                    attributeName = "role"
                )
                    .doOnError { error -> return failure(error) }
                    .get

                return Result.success(Params(cpid = cpidResult, ocid = ocidResult, role = roleParsed))
            }
        }
    }
}
