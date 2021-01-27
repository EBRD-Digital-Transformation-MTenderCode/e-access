package com.procurement.access.infrastructure.api.v1

import com.procurement.access.infrastructure.api.ApiVersion
import com.procurement.access.infrastructure.api.command.id.CommandId

fun ApiResponseV1.Failure.Companion.internalServerError(
    version: ApiVersion,
    id: CommandId,
    description: String
): ApiResponseV1.Failure = ApiResponseV1.Failure(
    version = version,
    id = id,
    errors = listOf(
        ApiResponseV1.Failure.Error(
            code = "400.03.00",
            description = description
        )
    )
)

fun ApiResponseV1.Failure.Companion.businessError(
    version: ApiVersion,
    id: CommandId,
    code: String,
    description: String
): ApiResponseV1.Failure = ApiResponseV1.Failure(
    version = version,
    id = id,
    errors = listOf(
        ApiResponseV1.Failure.Error(
            code = "400.03.$code",
            description = description
        )
    )
)
