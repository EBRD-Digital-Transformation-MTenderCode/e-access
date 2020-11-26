package com.procurement.access.application.model.organization

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseEnum
import com.procurement.access.application.model.parseOcid
import com.procurement.access.domain.EnumElementProvider
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.lib.functional.Result

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
                    .onFailure { error -> return error }

                val ocidResult = parseOcid(value = ocid)
                    .onFailure { error -> return error }

                val roleParsed = parseEnum(
                    value = role,
                    allowedEnums = allowedRoles,
                    attributeName = "role",
                    target = OrganizationRole
                )
                    .onFailure { error -> return error }

                return Result.success(Params(cpid = cpidResult, ocid = ocidResult, role = roleParsed))
            }
        }

        enum class OrganizationRole(@JsonValue override val key: String) : EnumElementProvider.Key {
            PROCURING_ENTITY("procuringEntity");

            override fun toString(): String = key

            companion object : EnumElementProvider<OrganizationRole>(info = info()) {

                @JvmStatic
                @JsonCreator
                fun creator(name: String) = OrganizationRole.orThrow(name)
            }
        }
    }
}
