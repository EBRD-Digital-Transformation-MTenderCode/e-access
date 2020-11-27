package com.procurement.access.infrastructure.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.procurement.access.infrastructure.configuration.properties.OCDSProperties
import com.procurement.access.infrastructure.configuration.properties.UriProperties
import com.procurement.access.service.validation.JsonValidationService
import com.procurement.access.service.validation.MedeiaValidationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(
    basePackages = [
        "com.procurement.access.service",
        "com.procurement.access.application.service",
        "com.procurement.access.infrastructure.handler"
    ]
)
@EnableConfigurationProperties(value = [OCDSProperties::class, UriProperties::class])
class ServiceConfig {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Bean
    fun jsonValidationService(): JsonValidationService = MedeiaValidationService(objectMapper)
}
