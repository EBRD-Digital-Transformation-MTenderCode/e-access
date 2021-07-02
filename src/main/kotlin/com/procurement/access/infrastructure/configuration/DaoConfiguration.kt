package com.procurement.access.infrastructure.configuration

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.PlainTextAuthProvider
import com.datastax.driver.core.Session
import com.procurement.access.infrastructure.configuration.properties.CassandraProperties
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(CassandraProperties::class)
@ComponentScan(
    basePackages = [
        "com.procurement.access.dao",
        "com.procurement.access.infrastructure.repository"
    ]
)
class DaoConfiguration constructor(private val cassandraProperties: CassandraProperties) {

    internal val cluster: Cluster
        get() = Cluster.builder()
            .addContactPoints(*cassandraProperties.getContactPoints())
            .withoutJMXReporting()
            .withAuthProvider(PlainTextAuthProvider(cassandraProperties.username, cassandraProperties.password))
            .build()
            .init()

    @Bean
    @Qualifier("ocds")
    fun ocdsSession(): Session {
        return cluster.connect(cassandraProperties.oldKeyspaceName)
    }

    @Bean
    @Qualifier("access")
    fun contractingSession(): Session {
        return cluster.connect(cassandraProperties.keyspaceName)
    }
}
