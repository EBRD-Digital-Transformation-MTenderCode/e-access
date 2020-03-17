package com.procurement.access.application.service.tender.strategy.check

import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.owner.Owner
import com.procurement.access.domain.model.owner.tryCreateOwner
import com.procurement.access.domain.model.token.Token
import com.procurement.access.domain.model.token.tryCreateToken
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.asFailure
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

            val cpidResult = Cpid.tryCreate(cpid = cpid)
                .doOnError { pattern ->
                    return DataErrors.Validation.DataMismatchToPattern(
                        actualValue = cpid,
                        name = "cpid",
                        pattern = pattern
                    ).asFailure()
                }
                .get
            val ocidResult = Ocid.tryCreate(ocid = ocid)
                .doOnError { pattern ->
                    return DataErrors.Validation.DataMismatchToPattern(
                        actualValue = ocid,
                        name = "ocid",
                        pattern = pattern
                    ).asFailure()
                }
                .get
            val ownerResult = owner.tryCreateOwner()
                .doOnError { pattern ->
                    return DataErrors.Validation.DataMismatchToPattern(
                        actualValue = owner,
                        name = "owner",
                        pattern = pattern
                    ).asFailure()
                }
                .get
            val tokenResult = token.tryCreateToken()
                .doOnError { pattern ->
                    return DataErrors.Validation.DataMismatchToPattern(
                        actualValue = token,
                        name = "token",
                        pattern = pattern
                    ).asFailure()
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
