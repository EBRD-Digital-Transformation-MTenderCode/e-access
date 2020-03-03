package com.procurement.access.domain.util

sealed class ValidationResult<out E> {

    companion object {
        fun <E> ok(): ValidationResult<E> = Ok
        fun <E> error(value: E): ValidationResult<E> = Error(value)
    }

    abstract val error: E
    abstract val isOk: Boolean
    abstract val isError: Boolean

    val asOption: Option<E>
        get() = when (this) {
            is Error -> Option.pure(error)
            is Ok -> Option.none()
        }

    fun <R> map(transform: (E) -> R): ValidationResult<R> = when (this) {
        is Ok -> this
        is Error -> Error(transform(this.error))
    }

    fun <R> bind(function: (E) -> ValidationResult<R>): ValidationResult<R> = when (this) {
        is Ok -> this
        is Error -> function(this.error)
    }

    object Ok : ValidationResult<Nothing>() {
        override val error: Nothing
            get() = throw NoSuchElementException("Validation result does not contain error.")
        override val isOk: Boolean = true
        override val isError: Boolean = !isOk
    }

    class Error<out E>(value: E) : ValidationResult<E>() {
        override val error: E = value
        override val isOk: Boolean = false
        override val isError: Boolean = !isOk
    }
}
