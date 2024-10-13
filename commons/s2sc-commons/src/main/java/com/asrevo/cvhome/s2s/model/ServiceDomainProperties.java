package com.asrevo.cvhome.s2s.model;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("com.asrevo.cvhome")
public record ServiceDomainProperties(Map<String, ServiceDomain> services) {}
