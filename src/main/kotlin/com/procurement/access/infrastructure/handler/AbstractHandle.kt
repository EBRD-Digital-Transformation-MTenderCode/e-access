package com.procurement.access.infrastructure.handler

import com.procurement.access.application.service.Logger
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.util.Action
import com.procurement.access.infrastructure.api.ApiVersion
import com.procurement.access.infrastructure.api.command.id.CommandId
import com.procurement.access.infrastructure.api.v2.ApiResponseV2
import com.procurement.access.model.dto.bpe.generateDataErrorResponse
import com.procurement.access.model.dto.bpe.generateErrorResponse
import com.procurement.access.model.dto.bpe.generateIncidentResponse
import com.procurement.access.model.dto.bpe.generateValidationErrorResponse

abstract class AbstractHandler<ACTION : Action, R : Any>(
    private val logger: Logger
) : Handler<ACTION, ApiResponseV2> {

    protected fun responseError(id: CommandId, version: ApiVersion, fail: Fail): ApiResponseV2 {
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
