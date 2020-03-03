package com.procurement.access.domain.fail

import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.ValidationResult

sealed class Fail {

    abstract class Error(val prefix: String) : Fail() {
        abstract val code: String
        abstract val description: String
        val message: String
            get() = "ERROR CODE: '$code', DESCRIPTION: '$description'."

        companion object {
            fun <T, E : Error> E.toResult(): Result<T, E> = Result.failure(this)

            fun <E : Error> E.toValidationResult(): ValidationResult<E> = ValidationResult.error(this)
        }
    }

    sealed class Incident : Fail() {
        abstract val code: String
        abstract val description: String

//        sealed class Internal : Incident() {
//            data class ResponseError(val httpCode: Int, val contentType: String, val body: String?) : Internal()
//        }

        data class External(val code: String, val description: String)
    }
}
