package com.ocds.tender.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ComponentScan(basePackages = "com.ocds.tender.service")
public class ServiceConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper jackson2ObjectMapper = new ObjectMapper();
        jackson2ObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return jackson2ObjectMapper;
    }
}
