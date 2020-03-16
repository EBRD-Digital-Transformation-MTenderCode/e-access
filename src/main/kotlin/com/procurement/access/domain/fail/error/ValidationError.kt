package com.procurement.access.domain.fail.error

import com.procurement.access.application.service.Logger
import com.procurement.access.domain.fail.Fail

sealed class ValidationError(numberError: String, override val description: String) : Fail.Error(prefix = "VE-") {

    override val code: String = prefix + numberError

    override fun logging(logger: Logger) {
        logger.error(message = message)
    }

    class InvalidOwner : ValidationError(
        numberError = "01",
        description = ""
    )

    class InvalidToken : ValidationError(
        numberError = "02",
        description = ""
    )
}
