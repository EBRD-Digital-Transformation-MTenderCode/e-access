package com.procurement.access.config.properties

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
