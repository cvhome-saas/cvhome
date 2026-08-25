package com.asrevo.cvhome.content;

import org.springframework.boot.SpringApplication;

import com.asrevo.cvhome.testsupport.containers.MinioTestConfiguration;
import com.asrevo.cvhome.testsupport.containers.PostgresTestConfiguration;

/**
 * Runs the service locally against throwaway containers: {@code ./gradlew :...:integrationTest} is the test
 * entry point; this main is for running it from the IDE with the same infrastructure.
 */
public final class TestContentApplication {

    private TestContentApplication() {
    }

    public static void main(String[] args) {
        SpringApplication.from(ContentApplication::main).with(PostgresTestConfiguration.class, MinioTestConfiguration.class).run(args);
    }

}
