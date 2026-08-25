package com.asrevo.cvhome.cua;

import org.springframework.boot.SpringApplication;

import com.asrevo.cvhome.testsupport.containers.PostgresTestConfiguration;

/**
 * Runs the service locally against throwaway containers: {@code ./gradlew :...:integrationTest} is the test
 * entry point; this main is for running it from the IDE with the same infrastructure.
 */
public final class TestCuaApplication {

    private TestCuaApplication() {
    }

    public static void main(String[] args) {
        SpringApplication.from(CuaApplication::main).with(PostgresTestConfiguration.class).run(args);
    }

}
