package com.procurement.access.exception

class ErrorException(val error: ErrorType, message: String? = null) :
    RuntimeException(
        if (message != null)
            error.message + message
        else
            error.message
    )
