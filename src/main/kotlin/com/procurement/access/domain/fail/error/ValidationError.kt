package com.procurement.access.domain.fail.error

import com.procurement.access.domain.fail.Fail

sealed class ValidationError(numberError: String, override val description: String) : Fail.Error("VR-") {
    override val code: String = prefix + numberError

    class InvalidBusinessFunctionType(id: String) : ValidationError(
        numberError = "1",
        description = "Business function '${id}' has invalid type."
    )
}
