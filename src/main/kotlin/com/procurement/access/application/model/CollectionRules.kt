package com.procurement.access.application.model

import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.util.ValidationResult
import com.procurement.access.domain.util.ValidationRule


fun <T : Collection<Any>?> notEmptyRule(attributeName: String): ValidationRule<T, DataErrors.Validation> =
    ValidationRule { received: T ->
        if (received != null && received.isEmpty())
            ValidationResult.error(DataErrors.Validation.EmptyArray(attributeName))
        else
            ValidationResult.ok()
    }