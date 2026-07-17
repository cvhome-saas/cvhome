package com.asrevo.cvhome.s2s.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "com.asrevo.cvhome.uaa.admin")
public record AdminUserProperties(String password) {
}
