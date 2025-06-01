package com.asrevo.cvhome.s2s.model;

import com.asrevo.cvhome.commons.domain.KeycloakProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("com.asrevo.cvhome.kc")
public record KeycloakCredentialsProperties(KeycloakProperties credentials) {}
