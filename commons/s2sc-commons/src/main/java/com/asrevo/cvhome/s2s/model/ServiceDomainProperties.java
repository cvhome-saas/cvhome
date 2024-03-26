package com.asrevo.cvhome.s2s.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties("com.asrevo.cvhome")
public record ServiceDomainProperties(Map<String, ServiceDomain> services) {
}

