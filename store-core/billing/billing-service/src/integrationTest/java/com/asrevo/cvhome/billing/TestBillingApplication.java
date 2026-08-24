package com.asrevo.cvhome.billing;

import org.springframework.boot.SpringApplication;

import com.asrevo.cvhome.testsupport.containers.PostgresTestConfiguration;

/**
 * Runs the service locally against throwaway containers: {@code ./gradlew :...:integrationTest} is the test
 * entry point; this main is for running it from the IDE with the same infrastructure.
 */
public final class TestBillingApplication {

    private TestBillingApplication() {
    }

    public static void main(String[] args) {
        SpringApplication.from(BillingApplication::main).with(PostgresTestConfiguration.class).run(args);
    }

}
