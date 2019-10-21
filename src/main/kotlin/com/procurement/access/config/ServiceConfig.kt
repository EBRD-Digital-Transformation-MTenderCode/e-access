package com.procurement.access.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(
    basePackages = [
        "com.procurement.access.service",
        "com.procurement.access.application.service"
    ]
)
@EnableConfigurationProperties(value = [OCDSProperties::class])
class ServiceConfig
