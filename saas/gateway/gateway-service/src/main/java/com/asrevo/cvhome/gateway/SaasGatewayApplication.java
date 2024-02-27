package com.asrevo.cvhome.gateway;

import com.asrevo.cvhome.gateway.config.FargateProperties;
import com.asrevo.cvhome.gateway.config.ServiceDomainProperties;
import com.asrevo.cvhome.gateway.config.ssl.SslProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(value = {SslProperties.class, FargateProperties.class, ServiceDomainProperties.class})
public class SaasGatewayApplication {
    @lombok.Generated
    public static void main(String[] args) {
        SpringApplication.run(SaasGatewayApplication.class, args);
    }
}
