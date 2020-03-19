package com.procurement.access.config

import com.procurement.access.application.service.Logger
import com.procurement.access.infrastructure.service.CustomLogger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LoggerConfiguration {
    @Bean
    fun logger(): Logger = CustomLogger()
}
