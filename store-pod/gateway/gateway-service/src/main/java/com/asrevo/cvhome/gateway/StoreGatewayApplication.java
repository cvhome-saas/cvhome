package com.asrevo.cvhome.gateway;

import com.asrevo.cvhome.gateway.config.ServiceDomainProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(value = {ServiceDomainProperties.class})
public class StoreGatewayApplication {
    @lombok.Generated
    public static void main(String[] args) {
        SpringApplication.run(StoreGatewayApplication.class, args);
    }
}
