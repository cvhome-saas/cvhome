package com.asrevo.cvhome.wallet;

import com.asrevo.cvhome.s2s.config.ReactiveTestCustomSecurityConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
@Import(ReactiveTestCustomSecurityConfig.class)
public class TestWalletApplication {

    @Bean
    @ServiceConnection
    static PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:13"));
    }

    public static void main(String[] args) {
        SpringApplication.from(WalletApplication::main).with(TestWalletApplication.class).run(args);
    }

    @Bean
    CommandLineRunner runner(PostgreSQLContainer<?> postgresContainer) {
        return args -> {
            System.out.println(postgresContainer.getJdbcUrl());
            System.out.println(postgresContainer.getUsername());
            System.out.println(postgresContainer.getPassword());
        };
    }

}
