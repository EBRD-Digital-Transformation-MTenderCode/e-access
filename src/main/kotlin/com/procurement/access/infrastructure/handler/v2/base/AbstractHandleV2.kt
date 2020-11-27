package com.procurement.access.infrastructure.handler.v2.base

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.Logger
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.BadRequest
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.infrastructure.api.ApiVersion
import com.procurement.access.infrastructure.api.command.id.CommandId
import com.procurement.access.infrastructure.api.v2.ApiResponseV2
import com.procurement.access.infrastructure.api.v2.generateDataErrorResponse
import com.procurement.access.infrastructure.api.v2.generateErrorResponse
import com.procurement.access.infrastructure.api.v2.generateIncidentResponse
import com.procurement.access.infrastructure.api.v2.generateValidationErrorResponse
import com.procurement.access.infrastructure.extension.tryGetAttribute
import com.procurement.access.infrastructure.handler.Handler
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.Result.Failure
import com.procurement.access.lib.functional.Result.Success
import com.procurement.access.lib.functional.asFailure
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.utils.tryToObject

abstract class AbstractHandler<R : Any>(
    private val logger: Logger
) : Handler<ApiResponseV2> {

    final override val version: ApiVersion
        get() = ApiVersion(2, 0, 0)

    inline fun <reified T : Any> JsonNode.params() = params(T::class.java)

    fun <T : Any> JsonNode.params(target: Class<T>): Result<T, Fail.Error> {
        val name = "params"
        return this.tryGetAttribute(name)
            .flatMap {
                when (val result = it.tryToObject(target)) {
                    is Success -> result
                    is Failure -> BadRequest("Error parsing '$name'", result.reason.exception)
                        .asFailure<T, Fail.Error>()
                }
            }
    }

    fun responseError(id: CommandId, version: ApiVersion, fail: Fail): ApiResponseV2 {
        fail.logging(logger)
        return when (fail) {
            is Fail.Error -> {
                when (fail) {
                    is DataErrors.Validation -> generateDataErrorResponse(id = id, version = version, fail = fail)
                    is ValidationErrors -> generateValidationErrorResponse(id = id, version = version, fail = fail)
                    else -> generateErrorResponse(id = id, version = version, fail = fail)
                }
            }
            is Fail.Incident -> generateIncidentResponse(id = id, version = version, fail = fail)
        }
    }
}
