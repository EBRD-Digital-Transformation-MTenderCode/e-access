package com.procurement.access.infrastructure.handler

import com.procurement.access.application.service.Logger
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.ApiResponseV2
import com.procurement.access.infrastructure.handler.v2.CommandDescriptor
import com.procurement.access.lib.functional.Result
import com.procurement.access.utils.toJson

abstract class AbstractQueryHandler<R : Any?>(
    private val logger: Logger
) : AbstractHandler<ApiResponseV2>(logger = logger) {

    override fun handle(descriptor: CommandDescriptor): ApiResponseV2 {
        return when (val result = execute(descriptor)) {
            is Result.Success -> {
                if (logger.isDebugEnabled)
                    logger.debug("${action.key} has been executed. Result: ${toJson(result.get)}")
                return ApiResponseV2.Success(version = version, id = descriptor.id, result = result.value)
            }
            is Result.Failure -> responseError(fail = result.reason, version = version, id = descriptor.id)
        }
    }

    abstract fun execute(descriptor: CommandDescriptor): Result<R, Fail>
}
