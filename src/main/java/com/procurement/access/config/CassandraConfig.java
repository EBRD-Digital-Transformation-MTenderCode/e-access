package com.procurement.access.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;


@Configuration
@ComponentScan(basePackages = "com.procurement.access.model.entity")
@EnableCassandraRepositories(basePackages = "com.procurement.access.repository")
public class CassandraConfig {

}