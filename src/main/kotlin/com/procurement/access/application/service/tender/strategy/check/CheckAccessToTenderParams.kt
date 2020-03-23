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
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.asSuccess

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
                .doOnError { error ->
                    return Result.failure(error)
                }
                .get
            val ocidResult = parseOcid(value = ocid)
                .doOnError { error ->
                    return Result.failure(error)
                }
                .get
            val ownerResult = parseOwner(value = owner)
                .doOnError { error ->
                    return Result.failure(error)
                }
                .get
            val tokenResult = parseToken(value = token)
                .doOnError { error ->
                    return Result.failure(error)
                }
                .get

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
