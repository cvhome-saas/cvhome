package com.asrevo.cvhome.catalog;

import org.springframework.boot.SpringApplication;

import com.asrevo.cvhome.testsupport.containers.MinioTestConfiguration;
import com.asrevo.cvhome.testsupport.containers.PostgresTestConfiguration;

/**
 * Runs the service locally against throwaway containers: {@code ./gradlew :...:integrationTest} is the test
 * entry point; this main is for running it from the IDE with the same infrastructure.
 */
public final class TestCatalogApplication {

    private TestCatalogApplication() {
    }

    public static void main(String[] args) {
        SpringApplication.from(CatalogApplication::main).with(PostgresTestConfiguration.class, MinioTestConfiguration.class).run(args);
    }

}
