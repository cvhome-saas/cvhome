package com.asrevo.cvhome.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.asrevo.cvhome.s2s.config.CvhomeSharedConfig;

@SpringBootApplication
@Import(CvhomeSharedConfig.class)
public class StoreCoreGatewayApplication {

    private StoreCoreGatewayApplication() {
    }

    @lombok.Generated
    public static void main(String[] args) {
        SpringApplication.run(StoreCoreGatewayApplication.class, args);
    }

}
