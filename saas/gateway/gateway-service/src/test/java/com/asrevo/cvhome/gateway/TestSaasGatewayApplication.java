package com.asrevo.cvhome.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
@SpringBootApplication
public class TestSaasGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.from(SaasGatewayApplication::main).with(TestSaasGatewayApplication.class).run(args);
    }

}
