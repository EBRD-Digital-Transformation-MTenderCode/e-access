package com.procurement.access.infrastructure.api.v2

import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.infrastructure.api.ApiVersion
import com.procurement.access.infrastructure.api.command.id.CommandId
import com.procurement.access.infrastructure.configuration.properties.GlobalProperties
import com.procurement.access.lib.extension.toList
import java.time.LocalDateTime

fun errorResponse(fail: Fail, id: CommandId, version: ApiVersion): ApiResponseV2 =
    when (fail) {
        is DataErrors.Validation -> generateDataErrorResponse(id = id, version = version, fail = fail)
        is Fail.Error -> generateErrorResponse(id = id, version = version, fail = fail)
        is Fail.Incident -> generateIncidentResponse(id = id, version = version, fail = fail)
    }

fun generateDataErrorResponse(id: CommandId, version: ApiVersion, fail: DataErrors.Validation): ApiResponseV2.Error =
    ApiResponseV2.Error(
        version = version,
        id = id,
        result = listOf(
            ApiResponseV2.Error.Result(
                code = "${fail.code}/${GlobalProperties.service.id}",
                description = fail.description,
                details = ApiResponseV2.Error.Result.Detail.tryCreateOrNull(name = fail.name).toList()
            )
        )
    )

fun generateValidationErrorResponse(id: CommandId, version: ApiVersion, fail: ValidationErrors): ApiResponseV2.Error =
    ApiResponseV2.Error(
        version = version,
        id = id,
        result = listOf(
            ApiResponseV2.Error.Result(
                code = "${fail.code}/${GlobalProperties.service.id}",
                description = fail.description,
                details = ApiResponseV2.Error.Result.Detail.tryCreateOrNull(id = fail.entityId).toList()
            )
        )
    )

fun generateErrorResponse(id: CommandId, version: ApiVersion, fail: Fail): ApiResponseV2.Error =
    ApiResponseV2.Error(
        version = version,
        id = id,
        result = listOf(
            ApiResponseV2.Error.Result(
                code = "${fail.code}/${GlobalProperties.service.id}",
                description = fail.description
            )
        )
    )

fun generateIncidentResponse(id: CommandId, version: ApiVersion, fail: Fail.Incident): ApiResponseV2.Incident =
    ApiResponseV2.Incident(
        id = id,
        version = version,
        result = ApiResponseV2.Incident.Result(
            id = IncidentId.generate(),
            date = LocalDateTime.now(),
            level = fail.level,
            details = listOf(
                ApiResponseV2.Incident.Result.Detail(
                    code = "${fail.code}/${GlobalProperties.service.id}",
                    description = fail.description,
                    metadata = null
                )
            ),
            service = ApiResponseV2.Incident.Result.Service(
                id = GlobalProperties.service.id,
                version = GlobalProperties.service.version,
                name = GlobalProperties.service.name
            )
        )
    )
