package com.asrevo.cvhome.sso.config;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.s2s.model.AppProperties;
import com.asrevo.cvhome.s2s.model.OAuth2ClientProperties;
import com.asrevo.cvhome.sso.client.ClientSecretEncoder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Reconciles the configured OAuth2 clients with their seeded rows on boot.
 *
 * <p>
 * A configured secret is hashed with {@link ClientSecretEncoder}, a salted SHA-256, which is only as strong as the
 * secret is unguessable. So a configured secret shorter than {@link ClientSecretEncoder#MIN_LENGTH} characters stops
 * startup, before anything is written: the platform generates 43-character ones and the local profiles carry
 * 32-character ones, so only a mistake trips it.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2ClientDatabaseInitializer {

    private static final String WEAK_SECRET = """
            The configured secret of OAuth2 client '%s' is %d characters; at least %d are required, because a client \
            secret is stored as a fast hash that only a random secret makes safe""";

    private final OAuth2ClientProperties oAuth2ClientProperties;
    private final AppProperties appProperties;
    private final RegisteredClientRepository registeredClientRepository;
    private final ClientSecretEncoder clientSecrets;
    private final Environment environment;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onApplicationReady() {
        if (!SeedProperties.appliesOnBoot(environment)) {
            log.debug("{} is off; leaving the seeded rows as they are", SeedProperties.APPLY_ON_BOOT);
            return;
        }
        if (oAuth2ClientProperties.clients() == null || oAuth2ClientProperties.clients().isEmpty()) {
            log.debug("No OAuth2 clients provided in configuration, skipping initialization");
        } else {
            oAuth2ClientProperties.clients().forEach(OAuth2ClientDatabaseInitializer::requireStrongSecret);
            log.info("Checking OAuth2 clients for updates");
            oAuth2ClientProperties.clients().forEach(this::updateClient);
        }

    }

    private void updateClient(String clientId, OAuth2ClientProperties.ClientInfo clientInfo) {
        RegisteredClient client = registeredClientRepository.findByClientId(clientId);
        if (client == null) {
            log.warn("OAuth2 client with client_id '{}' not found in database", clientId);
            return;
        }

        log.info("Updating OAuth2 client '{}' from configuration", clientId);
        RegisteredClient.Builder builder = RegisteredClient.from(client);

        applySecret(clientInfo, builder);
        applyRedirectUris(clientInfo, builder);
        applyPostLogoutRedirectUris(clientInfo, builder);
        applyScopes(clientInfo, builder);
        applyGrantTypes(clientInfo, builder);

        registeredClientRepository.save(builder.build());
    }

    /** Names the client, never the secret: the message ends up in a log. */
    private static void requireStrongSecret(String clientId, OAuth2ClientProperties.ClientInfo clientInfo) {
        String secret = clientInfo.secret();
        if (secret != null && !secret.isBlank() && secret.length() < ClientSecretEncoder.MIN_LENGTH) {
            throw new IllegalStateException(String.format(WEAK_SECRET, clientId, secret.length(), ClientSecretEncoder.MIN_LENGTH));
        }
    }

    private void applySecret(OAuth2ClientProperties.ClientInfo clientInfo, RegisteredClient.Builder builder) {
        if (clientInfo.secret() != null && !clientInfo.secret().isBlank()) {
            builder.clientSecret(clientSecrets.encode(clientInfo.secret()));
        }
    }

    private void applyRedirectUris(OAuth2ClientProperties.ClientInfo clientInfo, RegisteredClient.Builder builder) {
        if (clientInfo.redirectUriPaths() != null && !clientInfo.redirectUriPaths().isEmpty()) {
            constructRedirectUris(clientInfo.redirectUriPaths()).forEach(builder::redirectUri);
        }
    }

    private void applyPostLogoutRedirectUris(OAuth2ClientProperties.ClientInfo clientInfo, RegisteredClient.Builder builder) {
        if (clientInfo.postLogoutRedirectUriPaths() != null && !clientInfo.postLogoutRedirectUriPaths().isEmpty()) {
            constructRedirectUris(clientInfo.postLogoutRedirectUriPaths()).forEach(builder::postLogoutRedirectUri);
        }
    }

    private void applyScopes(OAuth2ClientProperties.ClientInfo clientInfo, RegisteredClient.Builder builder) {
        if (clientInfo.scopes() != null && !clientInfo.scopes().isEmpty()) {
            clientInfo.scopes().forEach(builder::scope);
        }
    }

    private void applyGrantTypes(OAuth2ClientProperties.ClientInfo clientInfo, RegisteredClient.Builder builder) {
        if (clientInfo.grantTypes() != null && !clientInfo.grantTypes().isEmpty()) {
            clientInfo.grantTypes().forEach(grantType -> builder.authorizationGrantType(new AuthorizationGrantType(grantType)));
        }
    }

    private Set<String> constructRedirectUris(Set<String> redirectUriPaths) {
        Set<String> result = new LinkedHashSet<>();
        for (String url : appProperties.getUrls()) {
            for (String path : redirectUriPaths) {
                result.add(url + path);
            }
        }
        return result;
    }


}
