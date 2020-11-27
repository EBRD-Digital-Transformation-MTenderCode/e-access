package com.procurement.access

import com.procurement.access.infrastructure.configuration.ApplicationConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackageClasses = [ApplicationConfig::class])
class AccessApplication

fun main(args: Array<String>) {
    runApplication<AccessApplication>(*args)
}

