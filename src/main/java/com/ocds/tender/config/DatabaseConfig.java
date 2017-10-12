package com.ocds.tender.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@ComponentScan(basePackages = "com.ocds.tender.model.entity")
@EnableCassandraRepositories(basePackages = "com.ocds.tender.repository")
public class DatabaseConfig extends AbstractCassandraConfiguration {

    @Override
    protected String getKeyspaceName() {
        return "ocds";
    }

    @Bean
    public CassandraClusterFactoryBean cluster() {
        CassandraClusterFactoryBean cluster = new CassandraClusterFactoryBean();
        cluster.setUsername("cassandra");
        cluster.setPassword("cassandra");
        cluster.setContactPoints("localhost");
        cluster.setPort(9042);
        return cluster;
    }

}