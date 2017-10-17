package com.ocds.access.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;


@Configuration
@ComponentScan(basePackages = "com.ocds.access.model.entity")
@EnableCassandraRepositories(basePackages = "com.ocds.access.repository")
public class DatabaseMappingConfig {

}