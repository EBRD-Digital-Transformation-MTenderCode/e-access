package com.procurement.access.domain.fail.error

import com.procurement.access.application.service.Logger
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.model.owner.Owner
import com.procurement.access.domain.model.token.Token

sealed class ValidationErrors(numberError: String, override val description: String) : Fail.Error(prefix = "VE-") {

    override val code: String = prefix + numberError

    override fun logging(logger: Logger) {
        logger.error(message = message)
    }

    class InvalidOwner(val owner: Owner, val cpid: String) : ValidationErrors(
        numberError = "01",
        description = "Invalid owner '$owner' by cpid '$cpid'"
    )

    class InvalidToken(val token: Token, val cpid: String) : ValidationErrors(
        numberError = "02",
        description = "Invalid token '$token' by cpid '$cpid'"
    )
}
