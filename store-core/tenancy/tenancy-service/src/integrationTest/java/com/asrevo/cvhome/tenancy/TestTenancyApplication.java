package com.asrevo.cvhome.tenancy;

import org.springframework.boot.SpringApplication;

import com.asrevo.cvhome.testsupport.containers.PostgresTestConfiguration;

/**
 * Runs the service locally against throwaway containers: {@code ./gradlew :...:integrationTest} is the test
 * entry point; this main is for running it from the IDE with the same infrastructure.
 */
public final class TestTenancyApplication {

    private TestTenancyApplication() {
    }

    public static void main(String[] args) {
        SpringApplication.from(TenancyApplication::main).with(PostgresTestConfiguration.class).run(args);
    }

}
