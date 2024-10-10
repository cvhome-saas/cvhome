package com.asrevo.cvhome.manager.config;

import com.asrevo.cvhome.commons.domain.KeycloakCredentials;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("com.asrevo.cvhome.kc")
public record KeycloakCredentialsProperties(KeycloakCredentials credentials) {
}
