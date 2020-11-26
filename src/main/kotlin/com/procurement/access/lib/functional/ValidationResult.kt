package com.procurement.access.lib.functional

fun <E> E.asValidationFailure(): ValidationResult<E> = ValidationResult.error(this)

sealed class ValidationResult<out E> {

    companion object {
        fun <E> ok(): ValidationResult<E> = Ok
        fun <E> error(value: E): ValidationResult<E> = Error(value)
    }

    abstract val isOk: Boolean
    abstract val isError: Boolean

    val asOption: Option<E>
        get() = when (this) {
            is Error -> Option.pure(reason)
            is Ok -> Option.none()
        }

    fun <R> map(transform: (E) -> R): ValidationResult<R> = when (this) {
        is Ok -> this
        is Error -> Error(transform(reason))
    }

    fun <R> flatMap(function: (E) -> ValidationResult<R>): ValidationResult<R> = when (this) {
        is Ok -> this
        is Error -> function(reason)
    }

    object Ok : ValidationResult<Nothing>() {
        override val isOk: Boolean = true
        override val isError: Boolean = !isOk
    }

    class Error<out E>(val reason: E) : ValidationResult<E>() {
        override val isOk: Boolean = false
        override val isError: Boolean = !isOk
    }
}
