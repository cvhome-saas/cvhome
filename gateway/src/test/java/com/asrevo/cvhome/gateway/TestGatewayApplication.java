package com.asrevo.cvhome.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
class TestGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.from(GatewayApplication::main).with(TestGatewayApplication.class).run(args);
    }

}
