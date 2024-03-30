package com.asrevo.cvhome.gateway;

import com.asrevo.cvhome.s2s.config.ReactiveTestCustomSecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration(proxyBeanMethods = false)
@Import(ReactiveTestCustomSecurityConfig.class)
public class TestSaasPodGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.from(SaasPodGatewayApplication::main).with(TestSaasPodGatewayApplication.class).run(args);
    }

}
