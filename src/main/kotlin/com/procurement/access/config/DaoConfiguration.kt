package com.procurement.access.config

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.PlainTextAuthProvider
import com.datastax.driver.core.Session
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(CassandraProperties::class)
@ComponentScan(basePackages = ["com.procurement.access.dao"])
class DaoConfiguration constructor(private val cassandraProperties: CassandraProperties) {

    internal val cluster: Cluster
        get() = Cluster.builder()
                .addContactPoints(*cassandraProperties.getContactPoints())
                .withAuthProvider(PlainTextAuthProvider(cassandraProperties.username, cassandraProperties.password))
                .build()

    @Bean
    fun session(): Session {
        val cluster = cluster
        cluster.init()
        return cluster.connect(cassandraProperties.keyspaceName)
    }
}

