package com.ocds.tender.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@ComponentScan(basePackages = "com.ocds.tender.model.entity")
@EnableCassandraRepositories(basePackages = "com.ocds.tender.repository")
public class DatabaseConfig {
}