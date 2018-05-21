package com.procurement.access

import com.procurement.access.config.ApplicationConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient

@SpringBootApplication(scanBasePackageClasses = [ApplicationConfig::class])
@EnableEurekaClient
class AccessApplication

fun main(args: Array<String>) {
    runApplication<AccessApplication>(*args)
}

