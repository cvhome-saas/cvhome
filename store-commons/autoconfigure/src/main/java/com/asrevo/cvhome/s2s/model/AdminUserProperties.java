package com.asrevo.cvhome.s2s.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "com.asrevo.cvhome.admin")
public record AdminUserProperties(String password) {
}
