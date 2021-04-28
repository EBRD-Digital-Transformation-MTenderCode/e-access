package com.procurement.access.application.model

import com.procurement.access.domain.EnumElementProvider
import com.procurement.access.domain.EnumElementProvider.Companion.keysAsStrings
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.fail.error.DataTimeError
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.BusinessFunctionDocumentType
import com.procurement.access.domain.model.enums.BusinessFunctionType
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.domain.model.enums.ProcurementMethod
import com.procurement.access.domain.model.enums.ProcurementMethodModalities
import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.domain.model.lot.tryCreateLotId
import com.procurement.access.domain.model.owner.Owner
import com.procurement.access.domain.model.owner.tryCreateOwner
import com.procurement.access.domain.model.persone.PersonId
import com.procurement.access.domain.model.token.Token
import com.procurement.access.domain.model.token.tryCreateToken
import com.procurement.access.domain.util.extension.toLocalDateTime
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asSuccess
import java.time.LocalDateTime

fun parseCpid(value: String): Result<Cpid, DataErrors.Validation.DataMismatchToPattern> =
    Cpid.tryCreate(value = value)
        .mapFailure { expectedPattern ->
            DataErrors.Validation.DataMismatchToPattern(
                name = "cpid",
                pattern = expectedPattern,
                actualValue = value
            )
        }

fun parseOcid(value: String): Result<Ocid.SingleStage, DataErrors.Validation.DataMismatchToPattern> =
    Ocid.SingleStage.tryCreate(value = value)
        .mapFailure { expectedPattern ->
            DataErrors.Validation.DataMismatchToPattern(
                name = "ocid",
                pattern = expectedPattern,
                actualValue = value
            )
        }

fun parseOwner(value: String): Result<Owner, DataErrors.Validation.DataFormatMismatch> =
    value.tryCreateOwner()
        .mapFailure { pattern ->
            DataErrors.Validation.DataFormatMismatch(
                actualValue = value,
                name = "owner",
                expectedFormat = pattern
            )
        }

fun parseToken(value: String): Result<Token, DataErrors.Validation.DataFormatMismatch> =
    value.tryCreateToken()
        .mapFailure { pattern ->
            DataErrors.Validation.DataFormatMismatch(
                actualValue = value,
                name = "token",
                expectedFormat = pattern
            )
        }

fun parseStartDate(value: String): Result<LocalDateTime, DataErrors.Validation> =
    parseDate(value, "startDate")

fun parseDate(value: String, name: String): Result<LocalDateTime, DataErrors.Validation>{
    return value.toLocalDateTime()
        .mapFailure { fail ->
            when (fail) {
                is DataTimeError.InvalidFormat -> DataErrors.Validation.DataFormatMismatch(
                    name = name,
                    actualValue = value,
                    expectedFormat = fail.pattern
                )

                is DataTimeError.InvalidDateTime ->
                    DataErrors.Validation.InvalidDateTime(name = name, actualValue = value)
            }
        }
}

fun parseLotId(value: String, attributeName: String): Result<LotId, DataErrors.Validation.DataFormatMismatch> =
    value.tryCreateLotId()
        .mapFailure {
            DataErrors.Validation.DataFormatMismatch(
                name = attributeName,
                actualValue = value,
                expectedFormat = "uuid"
            )

        }

fun parsePersonId(value: String, attributeName: String): Result<PersonId, DataErrors.Validation.DataFormatMismatch> =
    PersonId.tryCreate(value)
        .mapFailure {
            DataErrors.Validation.DataFormatMismatch(
                name = attributeName,
                actualValue = value,
                expectedFormat = "string"
            )

        }

fun parseProcurementMethodModalities(
    value: String, allowedEnums: Set<ProcurementMethodModalities>, attributeName: String
): Result<ProcurementMethodModalities, DataErrors> =
    parseEnum(value = value, allowedEnums = allowedEnums, attributeName = attributeName, target = ProcurementMethodModalities)

fun parseOperationType(
    value: String, allowedEnums: Set<OperationType>
): Result<OperationType, DataErrors> =
    parseEnum(value = value, allowedEnums = allowedEnums, attributeName = "operationType", target = OperationType)

fun parseBusinessFunctionType(
    value: String, allowedEnums: Set<BusinessFunctionType>, attributeName: String
): Result<BusinessFunctionType, DataErrors> =
    parseEnum(value = value, allowedEnums = allowedEnums, attributeName = attributeName, target = BusinessFunctionType)

fun parseBusinessFunctionDocumentType(
    value: String, allowedEnums: Set<BusinessFunctionDocumentType>, attributeName: String
): Result<BusinessFunctionDocumentType, DataErrors> =
    parseEnum(value = value, allowedEnums = allowedEnums, attributeName = attributeName, target = BusinessFunctionDocumentType)

fun parsePmd(value: String, allowedEnums: Set<ProcurementMethod>): Result<ProcurementMethod, DataErrors> {
    fun getFailureResult() = Result.failure(
        DataErrors.Validation.UnknownValue(
            name = "pmd",
            expectedValues = allowedEnums.map { it.name },
            actualValue = value
        )
    )

    return ProcurementMethod.tryCreate(value)
        .onFailure { return getFailureResult() }
        .takeIf { it in allowedEnums }
        ?.asSuccess()
        ?: getFailureResult()
}

fun <T> parseEnum(value: String, allowedEnums: Set<T>, attributeName: String, target: EnumElementProvider<T>)
    : Result<T, DataErrors.Validation.UnknownValue> where T : Enum<T>,
                                                          T : EnumElementProvider.Key =
    target.orNull(value)
        ?.takeIf { it in allowedEnums }
        ?.asSuccess()
        ?: Result.failure(
            DataErrors.Validation.UnknownValue(
                name = attributeName,
                expectedValues = allowedEnums.keysAsStrings(),
                actualValue = value
            )
        )
