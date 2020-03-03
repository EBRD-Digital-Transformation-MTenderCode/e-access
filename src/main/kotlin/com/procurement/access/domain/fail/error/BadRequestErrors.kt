package com.procurement.access.domain.fail.error

import com.procurement.access.domain.fail.Fail

sealed class BadRequestErrors(
    numberError: String,
    override val description: String,
    val details: List<Detail>
) :
    Fail.Error("BR-") {

    override val code: String = prefix + numberError

    class EntityNotFound(details: List<Detail>) : BadRequestErrors(
        numberError = "01",
        description = "Entity not found",
        details = details
    )

    class ParseToObject(details: List<Detail>): BadRequestErrors(
        numberError = "02",
        description = "Error binding json to an object",
        details = details
    )

    class Detail(val name: String)
}