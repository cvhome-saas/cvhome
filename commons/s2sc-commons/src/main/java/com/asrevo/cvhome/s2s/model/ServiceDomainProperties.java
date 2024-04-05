package com.asrevo.cvhome.s2s.model;

import com.asrevo.cvhome.s2s.config.internal.CallStrategy;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties("com.asrevo.cvhome")
public record ServiceDomainProperties(Map<String, ServiceDomain> services, Map<String, CallStrategy> calls) {
}

