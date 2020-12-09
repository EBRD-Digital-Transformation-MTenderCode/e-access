package com.procurement.access.domain.fail.error

sealed class DataTimeError {

    abstract val reason: Exception

    data class InvalidFormat(val value: String, val pattern: String, override val reason: Exception) : DataTimeError()

    data class InvalidDateTime(val value: String, override val reason: Exception) : DataTimeError()
}
