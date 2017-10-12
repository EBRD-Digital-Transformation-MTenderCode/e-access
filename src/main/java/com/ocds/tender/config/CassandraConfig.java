package com.ocds.tender.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cassandra.CassandraProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean;

@Configuration
@EnableConfigurationProperties({CassandraProperties.class})
public class CassandraConfig extends AbstractCassandraConfiguration {

    private CassandraProperties properties;

    @Autowired
    public CassandraConfig(CassandraProperties properties) {
        this.properties = properties;
    }

    @Override
    protected String getKeyspaceName() {
        return "ocds";
    }

    @Bean
    public CassandraClusterFactoryBean cluster() {
        CassandraClusterFactoryBean cluster = new CassandraClusterFactoryBean();
        cluster.setUsername(properties.getUsername());
        cluster.setPassword(properties.getPassword());
        cluster.setContactPoints(properties.getContactPoints());
        cluster.setPort(properties.getPort());
        return cluster;
    }
}
