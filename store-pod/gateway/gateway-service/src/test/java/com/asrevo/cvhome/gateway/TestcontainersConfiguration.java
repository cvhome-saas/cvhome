package com.asrevo.cvhome.gateway;

import com.asrevo.cvhome.s2s.config.ReactiveTestCustomSecurityConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration(proxyBeanMethods = false)
@Import(ReactiveTestCustomSecurityConfig.class)
public class TestcontainersConfiguration {}
