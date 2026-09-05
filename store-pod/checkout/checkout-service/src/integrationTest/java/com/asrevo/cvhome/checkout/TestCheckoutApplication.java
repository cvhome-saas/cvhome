package com.asrevo.cvhome.checkout;

import org.springframework.boot.SpringApplication;

import com.asrevo.cvhome.testsupport.containers.PostgresTestConfiguration;

/**
 * Runs the service locally against a throwaway Postgres: {@code ./gradlew :...:integrationTest} is the test entry
 * point; this main is for running it from the IDE with the same infrastructure.
 */
public final class TestCheckoutApplication {

    private TestCheckoutApplication() {
    }

    public static void main(String[] args) {
        SpringApplication.from(CheckoutApplication::main).with(PostgresTestConfiguration.class).run(args);
    }
}
