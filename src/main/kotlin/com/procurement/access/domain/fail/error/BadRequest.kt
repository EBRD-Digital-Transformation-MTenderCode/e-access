package com.procurement.access.domain.fail.error

import com.procurement.access.application.service.Logger
import com.procurement.access.domain.fail.Fail

class BadRequest(override val description: String, val exception: Exception) : Fail.Error("RQ-") {

    override val code: String = "RQ-1"

    override fun logging(logger: Logger) {
        logger.error(message = message, exception = exception)
    }
}
