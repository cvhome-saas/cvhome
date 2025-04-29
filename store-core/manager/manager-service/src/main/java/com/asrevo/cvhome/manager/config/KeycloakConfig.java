package com.asrevo.cvhome.manager.config;

import com.asrevo.cvhome.keycloak.service.UserAccountService;
import com.asrevo.cvhome.keycloak.service.impl.KeycloakUserAccountServiceImpl;
import com.asrevo.cvhome.s2s.model.KeycloakCredentialsProperties;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
public class KeycloakConfig {

    @SneakyThrows
    @Bean
    public UserAccountService userAccountService(OAuth2ResourceServerProperties properties, KeycloakCredentialsProperties credentialsProperties) {
        return new KeycloakUserAccountServiceImpl(new URI(properties.getJwt().getJwkSetUri()), credentialsProperties.credentials());
    }

}
