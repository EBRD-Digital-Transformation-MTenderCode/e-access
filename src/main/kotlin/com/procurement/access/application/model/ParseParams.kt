package com.procurement.access.application.model

import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.date.tryParse
import com.procurement.access.domain.model.owner.Owner
import com.procurement.access.domain.model.owner.tryCreateOwner
import com.procurement.access.domain.model.token.Token
import com.procurement.access.domain.model.token.tryCreateToken
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.asFailure
import com.procurement.access.domain.util.asSuccess
import java.time.LocalDateTime

fun parseCpid(value: String): Result<Cpid, DataErrors.Validation.DataMismatchToPattern> =
    Cpid.tryCreate(value = value)
        .doOnError { expectedPattern ->
            return Result.failure(
                DataErrors.Validation.DataMismatchToPattern(
                    name = "cpid",
                    pattern = expectedPattern,
                    actualValue = value
                )
            )
        }
        .get
        .asSuccess()

fun parseOcid(value: String): Result<Ocid, DataErrors.Validation.DataMismatchToPattern> =
    Ocid.tryCreate(value = value)
        .doOnError { expectedPattern ->
            return Result.failure(
                DataErrors.Validation.DataMismatchToPattern(
                    name = "ocid",
                    pattern = expectedPattern,
                    actualValue = value
                )
            )
        }
        .get
        .asSuccess()

fun parseOwner(value: String): Result<Owner, DataErrors.Validation.DataFormatMismatch> =
    value.tryCreateOwner()
        .doOnError { pattern ->
            return DataErrors.Validation.DataFormatMismatch(
                actualValue = value,
                name = "owner",
                expectedFormat = pattern
            ).asFailure()
        }
        .get
        .asSuccess()

fun parseToken(value: String): Result<Token, DataErrors.Validation.DataFormatMismatch> =
    value.tryCreateToken()
        .doOnError { pattern ->
            return DataErrors.Validation.DataFormatMismatch(
                actualValue = value,
                name = "token",
                expectedFormat = pattern
            ).asFailure()
        }
        .get
        .asSuccess()

fun parseStartDate(value: String): Result<LocalDateTime, DataErrors.Validation.DataFormatMismatch> =
    value.tryParse()
        .doOnError { expectedFormat ->
            return Result.failure(
                DataErrors.Validation.DataFormatMismatch(
                    name = "startDate",
                    actualValue = value,
                    expectedFormat = expectedFormat
                )
            )
        }
        .get
        .asSuccess()
