package com.asrevo.cvhome.gateway;

import org.springframework.boot.SpringApplication;

import com.asrevo.cvhome.testsupport.security.ReactiveTestSecurityConfiguration;

/**
 * Runs the service locally against throwaway containers: {@code ./gradlew :...:integrationTest} is the test
 * entry point; this main is for running it from the IDE with the same infrastructure.
 */
public final class TestStoreCoreGatewayApplication {

    private TestStoreCoreGatewayApplication() {
    }

    public static void main(String[] args) {
        SpringApplication.from(StoreCoreGatewayApplication::main).with(ReactiveTestSecurityConfiguration.class).run(args);
    }

}
