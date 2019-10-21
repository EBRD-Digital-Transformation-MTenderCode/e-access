package com.procurement.access.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "ocds")
class OCDSProperties {
    var prefix: String? = null
}

