package com.procurement.access.infrastructure.configuration.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "ocds")
class OCDSProperties(
    var prefixes: Prefixes? = null
) {
    class Prefixes(
        var main: String? = null,
        var test: String? = null
    )
}
