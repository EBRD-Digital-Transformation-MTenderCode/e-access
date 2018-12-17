package com.procurement.access.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(WebConfig::class, ServiceConfig::class, DaoConfiguration::class, ObjectMapperConfig::class)
class ApplicationConfig