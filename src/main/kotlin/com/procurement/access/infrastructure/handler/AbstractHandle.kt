package com.procurement.access.infrastructure.handler

import com.procurement.access.config.GlobalProperties
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.util.Action
import com.procurement.access.infrastructure.web.dto.ApiDataErrorResponse
import com.procurement.access.infrastructure.web.dto.ApiErrorResponse
import com.procurement.access.infrastructure.web.dto.ApiIncidentResponse
import com.procurement.access.infrastructure.web.dto.ApiResponse
import com.procurement.access.infrastructure.web.dto.ApiVersion
import com.procurement.access.model.dto.bpe.generateIncident
import java.util.*

abstract class AbstractHandler<ACTION : Action, R : Any> :
    Handler<ACTION, ApiResponse> {

    protected fun responseError(id: UUID, version: ApiVersion, fail: Fail): ApiResponse =
        when (fail) {
            is Fail.Error -> {
                when (fail) {
                    is DataErrors.Validation -> {
                        ApiDataErrorResponse(
                            version = version,
                            id = id,
                            result = listOf(
                                ApiDataErrorResponse.Error(
                                    code = "${fail.code}/${GlobalProperties.service.id}",
                                    description = fail.description,
                                    attributeName = fail.name
                                )
                            )
                        )
                    }
                    else -> {
                        ApiErrorResponse(
                            version = version,
                            id = id,
                            result = listOf(
                                ApiErrorResponse.Error(
                                    code = "${fail.code}/${GlobalProperties.service.id}",
                                    description = fail.description
                                )
                            )
                        )
                    }
                }
            }
            is Fail.Incident -> {
                when (fail) {
                    is Fail.Incident.Parsing -> {
                        val incident = Fail.Incident.DatabaseIncident()
                        ApiIncidentResponse(
                            id = id,
                            version = version,
                            result = generateIncident(fail = incident)
                        )
                    }
                    else -> ApiIncidentResponse(
                        id = id,
                        version = version,
                        result = generateIncident(fail = fail)
                    )
                }
            }
        }
}
