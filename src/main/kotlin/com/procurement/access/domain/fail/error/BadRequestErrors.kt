package com.procurement.access.domain.fail.error

import com.procurement.access.domain.fail.Fail

sealed class BadRequestErrors(
    numberError: String,
    override val description: String
) : Fail.Error("BR-") {

    override val code: String = prefix + numberError

    class EntityNotFound(entityName: String, by: String) : BadRequestErrors(
        numberError = "01",
        description = "Entity '$entityName' not found $by"
    )

    class Parsing(message: String, val request: String) : BadRequestErrors(
        numberError = "02",
        description = message
    )
}
