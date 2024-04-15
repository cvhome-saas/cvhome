package com.asrevo.cvhome.store;

import com.asrevo.cvhome.s2s.config.ServletTestCustomSecurityConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
@Import(ServletTestCustomSecurityConfig.class)
public class TestStoreApplication {

    @Bean
    @ServiceConnection
    static PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:13"));
    }

    public static void main(String[] args) {
        SpringApplication.from(StoreApplication::main).with(TestStoreApplication.class).run(args);
    }

    @Bean
    public CommandLineRunner runner(PostgreSQLContainer<?> postgresContainer, MinIOContainer minIOContainer) {
        return args -> {
            System.out.println(minIOContainer.getUiURL());
            System.out.println(minIOContainer.getUserName());
            System.out.println(minIOContainer.getPassword());

            System.out.println(postgresContainer.getJdbcUrl());
            System.out.println(postgresContainer.getUsername());
            System.out.println(postgresContainer.getPassword());
        };
    }
}
