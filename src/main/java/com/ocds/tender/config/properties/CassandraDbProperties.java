package com.ocds.tender.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Getter
@Setter
//@ConfigurationProperties(prefix = "cassandra")
public class CassandraDbProperties {
    private String contactPoints;
    private int port ;
    private String keyspaceName;
    private String username;
    private String password;
}
