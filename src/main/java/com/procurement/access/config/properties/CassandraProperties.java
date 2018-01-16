package com.procurement.access.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cassandra")
public class CassandraProperties {

    private String contactPoints;

    private String keyspaceName;

    private String username;

    private String password;

    public String[] getContactPoints() {
        return this.contactPoints.split(",");
    }

}
