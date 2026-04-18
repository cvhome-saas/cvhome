package com.asrevo.cvhome.gateway;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

import com.asrevo.cvhome.s2s.config.ReactiveTestCustomSecurityConfig;

@TestConfiguration(proxyBeanMethods = false)
@Import(ReactiveTestCustomSecurityConfig.class)
public class TestcontainersConfiguration {

}
