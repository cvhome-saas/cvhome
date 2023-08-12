package com.asrevo.cvhome.user;

import com.asrevo.cvhome.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

@SpringBootTest(properties = {"spring.sql.init.mode=always"})
@Testcontainers
class UserApplicationTests {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13");
    @Autowired
    private UserRepository userRepository;

    @Test
    void contextLoads() {
        StepVerifier.create(userRepository.count()).expectNext(1L).verifyComplete();
    }

}
