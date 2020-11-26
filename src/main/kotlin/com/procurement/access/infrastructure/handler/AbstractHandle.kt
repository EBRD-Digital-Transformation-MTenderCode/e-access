package com.procurement.access.infrastructure.handler

import com.procurement.access.application.service.Logger
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.util.Action
import com.procurement.access.infrastructure.api.ApiVersion
import com.procurement.access.infrastructure.web.dto.ApiResponse
import com.procurement.access.model.dto.bpe.generateDataErrorResponse
import com.procurement.access.model.dto.bpe.generateErrorResponse
import com.procurement.access.model.dto.bpe.generateIncidentResponse
import com.procurement.access.model.dto.bpe.generateValidationErrorResponse
import java.util.*

abstract class AbstractHandler<ACTION : Action, R : Any>(
    private val logger: Logger
) : Handler<ACTION, ApiResponse> {

    protected fun responseError(id: UUID, version: ApiVersion, fail: Fail): ApiResponse {
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
