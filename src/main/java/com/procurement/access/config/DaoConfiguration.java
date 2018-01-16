package com.procurement.access.config;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PlainTextAuthProvider;
import com.datastax.driver.core.Session;
import com.procurement.access.config.properties.CassandraProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CassandraProperties.class)
@ComponentScan(basePackages = "com.procurement.access.dao")
public class DaoConfiguration {

    private final CassandraProperties properties;

    @Autowired
    public DaoConfiguration(final CassandraProperties cassandraProperties) {
        this.properties = cassandraProperties;
    }

    @Bean
    public Session session() {
        final Cluster cluster = getCluster();
        cluster.init();
        return cluster.connect(properties.getKeyspaceName());
    }

    Cluster getCluster() {
        return Cluster.builder()
                .addContactPoints(properties.getContactPoints())
                .withAuthProvider(new PlainTextAuthProvider(properties.getUsername(), properties.getPassword()))
                .build();
    }

}
