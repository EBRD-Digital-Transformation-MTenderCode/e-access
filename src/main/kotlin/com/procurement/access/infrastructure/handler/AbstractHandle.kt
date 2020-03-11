package com.procurement.access.infrastructure.handler

import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.BadRequestErrors
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.util.Action
import com.procurement.access.infrastructure.web.dto.ApiResponse
import com.procurement.access.infrastructure.web.dto.ApiVersion
import com.procurement.access.model.dto.bpe.generateDataErrorResponse
import com.procurement.access.model.dto.bpe.generateErrorResponse
import com.procurement.access.model.dto.bpe.generateIncidentResponse
import java.util.*

abstract class AbstractHandler<ACTION : Action, R : Any> :
    Handler<ACTION, ApiResponse> {

    protected fun responseError(id: UUID, version: ApiVersion, fail: Fail): ApiResponse =
        when (fail) {
            is Fail.Error -> {
                when (fail) {
                    is DataErrors.Validation -> generateDataErrorResponse(id = id, version = version, fail = fail)
                    is DataErrors.Parsing -> {
                        val error = BadRequestErrors.Parsing("Internal Server Error")
                        generateErrorResponse(id = id, version = version, fail = error)
                    }
                    else -> generateErrorResponse(id = id, version = version, fail = fail)
                }
            }
            is Fail.Incident -> generateIncidentResponse(id = id, version = version, fail = fail)
        }
}
