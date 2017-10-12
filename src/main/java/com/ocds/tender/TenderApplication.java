package com.ocds.tender;

import com.ocds.tender.config.ApplicationConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;

@SpringBootApplication(scanBasePackageClasses = {ApplicationConfig.class})
@EnableAutoConfiguration(exclude = {CassandraDataAutoConfiguration.class})
public class TenderApplication {

    public static void main(String[] args) {
        SpringApplication.run(TenderApplication.class, args);
    }
}
