package com.asrevo.cvhome.catalog;

import com.asrevo.cvhome.catalog.config.MinioS3Config;
import com.asrevo.cvhome.s2s.config.ServletTestCustomSecurityConfig;
import com.asrevo.cvhome.catalog.config.MinIOContainer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
@Import({ServletTestCustomSecurityConfig.class, MinioS3Config.class})
public class TestcontainersConfiguration {
    @Bean
    @ServiceConnection
    static PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"));
    }

    @Bean
    public CommandLineRunner runner(PostgreSQLContainer<?> postgresContainer, MinIOContainer minIOContainer) {
        return args -> {
            System.out.println(minIOContainer.getUiURL());
            System.out.println(minIOContainer.getAccessKey());
            System.out.println(minIOContainer.getSecretKey());

            System.out.println(postgresContainer.getJdbcUrl());
            System.out.println(postgresContainer.getUsername());
            System.out.println(postgresContainer.getPassword());
        };
    }
}
