package com.asrevo.cvhome.gateway;

import com.asrevo.cvhome.s2s.config.TestCustomSecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration(proxyBeanMethods = false)
@Import(TestCustomSecurityConfig.class)
public class TestStoreGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.from(StoreGatewayApplication::main).with(TestStoreGatewayApplication.class).run(args);
    }

}
