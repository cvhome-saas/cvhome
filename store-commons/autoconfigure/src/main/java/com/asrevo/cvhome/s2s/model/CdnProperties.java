package com.asrevo.cvhome.s2s.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("com.asrevo.cvhome.cdn")
public record CdnProperties(String basePath, String contextPath) {
}
