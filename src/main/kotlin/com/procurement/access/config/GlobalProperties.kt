package com.procurement.access.config

import com.procurement.access.infrastructure.web.dto.ApiVersion

object GlobalProperties {
    const val serviceId = "9"

    object App {
        val apiVersion = ApiVersion(major = 1, minor = 0, patch = 0)
    }
}
