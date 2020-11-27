package com.procurement.access.infrastructure.handler

import com.procurement.access.application.service.Logger
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.ApiResponseV2
import com.procurement.access.infrastructure.handler.v2.CommandDescriptor
import com.procurement.access.lib.functional.ValidationResult
import com.procurement.access.utils.toJson

abstract class AbstractValidationHandler(
    private val logger: Logger
) : AbstractHandler<ApiResponseV2>(logger = logger) {

    override fun handle(descriptor: CommandDescriptor): ApiResponseV2 {
        return when (val result = execute(descriptor)) {
            is ValidationResult.Ok -> ApiResponseV2.Success(version = version, id = descriptor.id)
                .also {
                    logger.info("'${action.key}' has been executed. Result: '${toJson(it)}'")
                }
            is ValidationResult.Error -> responseError(id = descriptor.id, version = version, fail = result.reason)
        }
    }

    abstract fun execute(descriptor: CommandDescriptor): ValidationResult<Fail>
}
