package com.asrevo.cvhome.sso.config;

import java.util.Map;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientProperties;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientPropertiesMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

import com.asrevo.cvhome.sso.idp.ClientRegistrationFactory;
import com.asrevo.cvhome.sso.idp.DynamicClientRegistrationRepository;
import com.asrevo.cvhome.sso.realm.SsoTenantIdentifierResolver;
import com.asrevo.cvhome.sso.repo.IdentityProviderRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * The OAuth2-client side of uaa: the registrations Spring's login filter looks up. Configuration first (the
 * platform's own entries, mapped the way Boot would), then the identity providers in the database.
 */
@Configuration
@EnableConfigurationProperties(OAuth2ClientProperties.class)
@Slf4j
public class IdpConfig {

    @Bean
    DynamicClientRegistrationRepository clientRegistrationRepository(OAuth2ClientProperties properties,
                                                                     IdentityProviderRepository providers,
                                                                     ClientRegistrationFactory factory,
                                                                     SsoTenantIdentifierResolver realms) {
        Map<String, ClientRegistration> configured;
        try {
            configured = new OAuth2ClientPropertiesMapper(properties).asClientRegistrations();
        } catch (IllegalStateException e) {
            log.warn("The configured OAuth2 client registrations could not be mapped and are skipped: {}", e.getMessage());
            configured = Map.of();
        }
        return new DynamicClientRegistrationRepository(configured, providers, factory, realms);
    }

    /** The issuer the redirect URIs are shown under: the same pinned value every token names. */
    @Bean
    String uaaIssuer(AuthorizationServerSettings settings) {
        return settings.getIssuer();
    }

}
