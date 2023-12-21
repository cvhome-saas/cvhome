package org.revo.streamer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
@SpringBootApplication
public class TestStreamerApplication {

/*
    @Bean
    @ServiceConnection
    static PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:13"));
    }
*/

    public static void main(String[] args) {
        SpringApplication.from(StreamerApplication::main).with(TestStreamerApplication.class).run(args);
    }
}
