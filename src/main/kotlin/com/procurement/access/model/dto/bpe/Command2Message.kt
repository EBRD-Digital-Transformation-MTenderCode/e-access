package com.procurement.access.model.dto.bpe

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeType
import com.fasterxml.jackson.databind.node.NullNode
import com.procurement.access.config.GlobalProperties
import com.procurement.access.domain.EnumElementProvider
import com.procurement.access.domain.EnumElementProvider.Companion.keysAsStrings
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.BadRequestErrors
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.infrastructure.api.ApiVersion
import com.procurement.access.infrastructure.api.command.id.CommandId
import com.procurement.access.infrastructure.api.v2.ApiResponseV2
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.api.v2.IncidentId
import com.procurement.access.lib.extension.toList
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.Result.Companion.failure
import com.procurement.access.lib.functional.Result.Companion.success
import com.procurement.access.lib.functional.asFailure
import com.procurement.access.lib.functional.asSuccess
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.utils.tryToObject
import java.time.LocalDateTime
import java.util.*

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

val NaN: UUID
    get() = UUID(0, 0)

fun JsonNode.getId(): Result<CommandId, DataErrors> = tryGetStringAttribute("id").map { CommandId(it) }

fun JsonNode.getVersion(): Result<ApiVersion, DataErrors> {
    val name = "version"
    return tryGetStringAttribute(name)
        .flatMap { version ->
            ApiVersion.orNull(version)
                ?.asSuccess<ApiVersion, DataErrors>()
                ?: DataErrors.Validation.DataFormatMismatch(
                    name = name,
                    expectedFormat = ApiVersion.pattern,
                    actualValue = version
                ).asFailure()
        }
}

fun JsonNode.getAction(): Result<CommandTypeV2, DataErrors> {
    return this.tryGetEnumAttribute(name = "action", enumProvider = CommandTypeV2)
}

private fun JsonNode.tryGetStringAttribute(name: String): Result<String, DataErrors> {
    return this.tryGetAttribute(name = name, type = JsonNodeType.STRING)
        .map {
            it.asText()
        }
}

private fun <T> JsonNode.tryGetEnumAttribute(name: String, enumProvider: EnumElementProvider<T>)
    : Result<T, DataErrors> where T : Enum<T>,
                                  T : EnumElementProvider.Key =
    this.tryGetStringAttribute(name)
        .flatMap { enum ->
            enumProvider.orNull(enum)
                ?.asSuccess<T, DataErrors>()
                ?: failure(
                    DataErrors.Validation.UnknownValue(
                        name = name,
                        expectedValues = enumProvider.allowedElements.keysAsStrings(),
                        actualValue = enum
                    )
                )
        }

private fun JsonNode.tryGetAttribute(name: String, type: JsonNodeType): Result<JsonNode, DataErrors> =
    getAttribute(name = name)
        .flatMap { node ->
            if (node.nodeType == type)
                success(node)
            else
                failure(
                    DataErrors.Validation.DataTypeMismatch(
                        name = name,
                        expectedType = type.name,
                        actualType = node.nodeType.name
                    )
                )
        }

private fun asUUID(value: String): Result<UUID, DataErrors> =
    try {
        Result.success<UUID>(UUID.fromString(value))
    } catch (exception: IllegalArgumentException) {
        Result.failure(
            DataErrors.Validation.DataFormatMismatch(
                name = "id",
                expectedFormat = "uuid",
                actualValue = value
            )
        )
    }

fun <T : Any> JsonNode.tryParamsToObject(clazz: Class<T>): Result<T, BadRequestErrors.Parsing> =
    this.tryToObject(clazz)
        .mapFailure {
            BadRequestErrors.Parsing(
                message = "Can not parse 'params'.",
                request = this.toString(),
                exception = it.exception)
        }


fun JsonNode.getAttribute(name: String): Result<JsonNode, DataErrors> {
    return if (has(name)) {
        val attr = get(name)
        if (attr !is NullNode)
            success(attr)
        else
            failure(DataErrors.Validation.DataTypeMismatch(name = name, actualType = "null", expectedType = "not null"))
    } else
        failure(DataErrors.Validation.MissingRequiredAttribute(name = name))
}

fun JsonNode.tryGetParams(): Result<JsonNode, DataErrors> =
    getAttribute("params")
