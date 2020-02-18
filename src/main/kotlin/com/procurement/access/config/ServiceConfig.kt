package com.procurement.access.config

import com.procurement.access.config.properties.OCDSProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(
    basePackages = [
        "com.procurement.access.service",
        "com.procurement.access.application.service",
        "com.procurement.access.infrastructure.handlers"
    ]
)
@EnableConfigurationProperties(value = [OCDSProperties::class])
class ServiceConfig
