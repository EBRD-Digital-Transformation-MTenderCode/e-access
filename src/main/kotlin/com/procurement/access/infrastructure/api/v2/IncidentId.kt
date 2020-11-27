package com.procurement.access.infrastructure.api.v2

import java.util.*

class IncidentId private constructor(val underlying: String) {

    companion object {
        fun generate(): IncidentId = IncidentId(UUID.randomUUID().toString())
    }
}
