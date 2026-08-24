package com.asrevo.cvhome.testsupport.containers;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * A throwaway Postgres for one Spring context. {@code @ServiceConnection} wires the datasource, so the service's own
 * {@code schema.sql} / {@code init-sql} run against it exactly as they do locally.
 */
@TestConfiguration(proxyBeanMethods = false)
public class PostgresTestConfiguration {

    public static final String IMAGE = "postgres:15-alpine";

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse(IMAGE));
    }

}
