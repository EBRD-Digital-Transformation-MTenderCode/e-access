package com.procurement.access.domain.fail.error

import com.procurement.access.application.service.Logger
import com.procurement.access.domain.fail.Fail

abstract class CommandValidationErrors(
    numberError: String,
    prefix: String = "VR-COM",
    override val description: String
) : Fail.Error(prefix = prefix) {

    override val code: String = prefix + numberError

    override fun logging(logger: Logger) {
        logger.error(message = message)
    }
}
