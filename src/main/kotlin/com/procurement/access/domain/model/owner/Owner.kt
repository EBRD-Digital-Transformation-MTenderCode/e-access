package com.procurement.access.domain.model.owner

import com.procurement.access.lib.functional.Result
import java.util.*

typealias Owner = UUID

fun String.tryCreateOwner(): Result<Owner, String> = try {
    Result.success(Owner.fromString(this))
} catch (ignored: Exception) {
    Result.failure("uuid")
}
