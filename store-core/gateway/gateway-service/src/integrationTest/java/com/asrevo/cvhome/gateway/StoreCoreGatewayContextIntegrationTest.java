package com.asrevo.cvhome.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.asrevo.cvhome.testsupport.security.ReactiveTestSecurityConfiguration;

@SpringBootTest
@Import(ReactiveTestSecurityConfiguration.class)
class StoreCoreGatewayContextIntegrationTest {

    @Test
    void contextLoads() {
    }

}
