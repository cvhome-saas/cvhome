package com.asrevo.cvhome.gateway;

import com.asrevo.cvhome.s2s.config.CustomSecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration(proxyBeanMethods = false)
@Import(CustomSecurityConfig.class)
public class TestSaasGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.from(SaasGatewayApplication::main).with(TestSaasGatewayApplication.class).run(args);
    }

}
