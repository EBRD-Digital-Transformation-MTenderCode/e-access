package com.procurement.access.domain.model.lot

import com.procurement.access.domain.fail.Fail
import com.procurement.access.lib.functional.Result
import java.util.*

typealias LotId = UUID

fun String.tryCreateLotId(): Result<LotId, Fail.Incident.Parsing> = try {
    Result.success(LotId.fromString(this))
} catch (expected: Exception) {
    Result.failure(Fail.Incident.Parsing(className = "LotId", exception = expected))
}
