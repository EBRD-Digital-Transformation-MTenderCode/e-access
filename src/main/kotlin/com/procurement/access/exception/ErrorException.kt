package com.procurement.access.exception

class ErrorException(error: ErrorType, message: String? = null) : RuntimeException(message) {

    var code: String = error.code
    var msg: String

    init {
        when (message) {
            null -> this.msg = error.message
            else -> this.msg = error.message + message
        }
    }

}
