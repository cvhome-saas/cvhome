package com.asrevo.cvhome.uaa.config;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.s2s.model.AppProperties;
import com.asrevo.cvhome.s2s.model.OAuth2ClientProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2ClientDatabaseInitializer {

    private final OAuth2ClientProperties oAuth2ClientProperties;
    private final AppProperties appProperties;
    private final RegisteredClientRepository registeredClientRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onApplicationReady() {
        if (oAuth2ClientProperties.clients() == null || oAuth2ClientProperties.clients().isEmpty()) {
            log.debug("No OAuth2 clients provided in configuration, skipping initialization");
        } else {
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

        if (clientInfo.secret() != null && !clientInfo.secret().isBlank()) {
            builder.clientSecret(passwordEncoder.encode(clientInfo.secret()));
        }

        if (clientInfo.redirectUriPaths() != null && !clientInfo.redirectUriPaths().isEmpty()) {
            constructRedirectUris(clientInfo.redirectUriPaths()).forEach(builder::redirectUri);
        }

        if (clientInfo.postLogoutRedirectUriPaths() != null && !clientInfo.postLogoutRedirectUriPaths().isEmpty()) {
            constructRedirectUris(clientInfo.postLogoutRedirectUriPaths()).forEach(builder::postLogoutRedirectUri);
        }

        if (clientInfo.scopes() != null && !clientInfo.scopes().isEmpty()) {
            clientInfo.scopes().forEach(builder::scope);
        }

        if (clientInfo.grantTypes() != null && !clientInfo.grantTypes().isEmpty()) {
            clientInfo.grantTypes().forEach(grantType -> builder.authorizationGrantType(new AuthorizationGrantType(grantType)));
        }

        registeredClientRepository.save(builder.build());
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
