package com.procurement.access.infrastructure.configuration.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "uri")
class UriProperties {
    var tender: String = ""
}
