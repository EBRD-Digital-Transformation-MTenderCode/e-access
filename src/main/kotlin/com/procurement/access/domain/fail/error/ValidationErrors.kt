package com.procurement.access.domain.fail.error

import com.procurement.access.application.service.Logger
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.owner.Owner
import com.procurement.access.domain.model.token.Token

sealed class ValidationErrors(
    numberError: String,
    override val description: String,
    val entityId: String? = null
) : Fail.Error(prefix = "VR-") {

    override val code: String = prefix + numberError

    override fun logging(logger: Logger) {
        logger.error(message = message)
    }

    class InvalidOwner(val owner: Owner, val cpid: Cpid) : ValidationErrors(
        numberError = "10.1.1.2",
        description = "Invalid owner '$owner' by cpid '${cpid}'."
    )

    class InvalidToken(val token: Token, val cpid: Cpid) : ValidationErrors(
        numberError = "10.1.1.1",
        description = "Invalid token '$token' by cpid '$cpid'."
    )

    class LotsNotFound(val lotsId: Collection<String>) : ValidationErrors(
        numberError = "10.1.3.1",
        description = "Lots '$lotsId' do not found."
    )

    class TenderNotFound(val cpid: Cpid, val ocid: Ocid) : ValidationErrors(
        numberError = "10.1.1.3",
        description = "Tender not found by cpid '$cpid' and '$ocid'."
    )

    class InvalidBusinessFunctionType(id: String, allowedValues: List<String>) : ValidationErrors(
        numberError = "10.1.5.2",
        description = "Business function '${id}' has invalid type. Allowed values: ${allowedValues}"
    )

    class InvalidDocumentType(id: String, allowedValues: List<String>) : ValidationErrors(
        numberError = "10.1.5.1",
        description = "Document '${id}' has invalid type. Allowed values: ${allowedValues}"
    )

    class TenderNotFoundOnGetTenderState(val cpid: Cpid, val ocid: Ocid) : ValidationErrors(
        numberError = "10.1.7.1",
        description = "Tender not found by cpid '$cpid' and ocid '$ocid'."
    )

}
