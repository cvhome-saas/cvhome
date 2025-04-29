package com.asrevo.cvhome.gateway;

import org.springframework.boot.SpringApplication;

public class TestStorePodGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.from(StorePodGatewayApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
