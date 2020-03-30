package com.procurement.access.domain.fail.error

import com.procurement.access.application.service.Logger
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.owner.Owner
import com.procurement.access.domain.model.token.Token

sealed class ValidationErrors(numberError: String, override val description: String) : Fail.Error(prefix = "VR-") {

    override val code: String = prefix + numberError

    override fun logging(logger: Logger) {
        logger.error(message = message)
    }

    class InvalidOwner(val owner: Owner, val cpid: Cpid) : ValidationErrors(
        numberError = "10.1.1.2",
        description = "Invalid owner '$owner' by cpid '${cpid}'"
    )

    class InvalidToken(val token: Token, val cpid: Cpid) : ValidationErrors(
        numberError = "10.1.1.1",
        description = "Invalid token '$token' by cpid '$cpid'"
    )

    class LotsNotFound(val lotsId: Collection<String>) : ValidationErrors(
        numberError = "10.1.3.1",
        description = "Lots '$lotsId' do not found."
    )

    class InvalidBusinessFunctionType(id: String) : ValidationErrors(
        numberError = "10.5.5.2",
        description = "Business function '${id}' has invalid type."
    )

}
