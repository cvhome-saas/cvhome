package com.asrevo.cvhome.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestStoreCoreGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.from(StoreCoreGatewayApplication::main).with(TestStoreCoreGatewayApplication.class).run(args);
    }

}
