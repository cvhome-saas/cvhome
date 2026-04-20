package com.asrevo.cvhome.gateway;

import org.springframework.boot.SpringApplication;

public class TestStoreCoreGatewayApplication {

    private TestStoreCoreGatewayApplication() {
    }

    public static void main(String[] args) {
        SpringApplication.from(StoreCoreGatewayApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
