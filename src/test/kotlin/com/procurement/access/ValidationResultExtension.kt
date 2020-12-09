package com.procurement.access

import com.procurement.access.lib.functional.ValidationResult

fun <E> ValidationResult<E>.failure(): E = when (this) {
    is ValidationResult.Ok -> throw IllegalArgumentException("ValidationResult is not failure.")
    is ValidationResult.Error -> this.reason
}
