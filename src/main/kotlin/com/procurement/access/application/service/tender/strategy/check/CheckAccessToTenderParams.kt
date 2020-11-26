package com.procurement.access.application.service.tender.strategy.check

import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseOcid
import com.procurement.access.application.model.parseOwner
import com.procurement.access.application.model.parseToken
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.owner.Owner
import com.procurement.access.domain.model.token.Token
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asSuccess

class CheckAccessToTenderParams private constructor(
    val cpid: Cpid,
    val ocid: Ocid,
    val owner: Owner,
    val token: Token
) {
    companion object {
        fun tryCreate(
            cpid: String,
            ocid: String,
            owner: String,
            token: String
        ): Result<CheckAccessToTenderParams, DataErrors> {

            val cpidResult = parseCpid(value = cpid)
                .onFailure { return it }
            val ocidResult = parseOcid(value = ocid)
                .onFailure { return it }
            val ownerResult = parseOwner(value = owner)
                .onFailure { return it }
            val tokenResult = parseToken(value = token)
                .onFailure { return it }

            return CheckAccessToTenderParams(
                cpid = cpidResult,
                ocid = ocidResult,
                owner = ownerResult,
                token = tokenResult
            )
                .asSuccess()
        }
    }
}
