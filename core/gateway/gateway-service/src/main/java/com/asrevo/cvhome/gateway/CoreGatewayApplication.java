package com.asrevo.cvhome.gateway;

import com.asrevo.cvhome.gateway.config.FargateProperties;
import com.asrevo.cvhome.s2s.config.CvhomeSharedConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableConfigurationProperties(value = {FargateProperties.class})
@Import(CvhomeSharedConfig.class)
public class CoreGatewayApplication {
    @lombok.Generated
    public static void main(String[] args) {
        SpringApplication.run(CoreGatewayApplication.class, args);
    }
}
