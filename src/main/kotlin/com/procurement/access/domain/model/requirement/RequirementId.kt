package com.procurement.access.domain.model.requirement

import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.lib.functional.Result
import java.util.*

typealias RequirementId = UUID

fun String.tryToRequirementId(): Result<RequirementId, DataErrors> = try {
    Result.success(RequirementId.fromString(this))
} catch (expected: Exception) {
    Result.failure(
        DataErrors.Validation.DataFormatMismatch(
            name = "requirementId",
            expectedFormat = "uuid",
            actualValue = this
        )
    )
}
