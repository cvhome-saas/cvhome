package com.asrevo.cvhome.manager.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class KeycloakConfig {
    @Bean
    public Keycloak keycloak(OAuth2ResourceServerProperties properties) throws URISyntaxException {
        URI jwkSetUri = new URI(properties.getJwt().getJwkSetUri());
        String serverUrl = jwkSetUri.getScheme() + "://" + jwkSetUri.getAuthority();
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master")
                .grantType(OAuth2Constants.PASSWORD)
                .clientId("admin-cli")
                .password("admin")
                .username("admin")
                .build();
    }
}
