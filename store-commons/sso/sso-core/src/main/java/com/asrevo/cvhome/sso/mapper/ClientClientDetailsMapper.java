package com.asrevo.cvhome.sso.mapper;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import com.asrevo.cvhome.sso.dto.ClientDetails;
import com.asrevo.cvhome.sso.dto.ClientDetailsSettings;
import com.asrevo.cvhome.sso.dto.ClientDetailsTokens;
import com.asrevo.cvhome.sso.service.ClientAuthMethod;
import com.asrevo.cvhome.sso.service.OAuthGrantType;

public final class ClientClientDetailsMapper {

    public static final Set<String> KNOWN_TOKEN_SETTINGS = Set.of("settings.token.authorization-code-time-to-live",
            "settings.token.access-token-time-to-live", "settings.token.access-token-format",
            "settings.token.device-code-time-to-live", "settings.token.reuse-refresh-tokens",
            "settings.token.refresh-token-time-to-live", "settings.token.id-token-signature-algorithm",
            "settings.token.x509-certificate-bound-access-tokens");

    public static final Set<String> KNOWN_CLIENT_SETTINGS = Set.of("settings.client.require-proof-key",
            "settings.client.require-authorization-consent", "settings.client.jwk-set-url",
            "settings.client.token-endpoint-authentication-signing-algorithm",
            "settings.client.x509-certificate-subject-dn");

    private ClientClientDetailsMapper() {
    }

    public static ClientDetails toClientDetails(RegisteredClient client) {
        Set<ClientAuthMethod> clientAuthenticationMethods = client.getClientAuthenticationMethods()
                .stream()
                .map(ClientAuthenticationMethod::getValue)
                .map(ClientAuthMethod::from)
                .collect(Collectors.toSet());
        Set<OAuthGrantType> authorizationGrantTypes = client.getAuthorizationGrantTypes()
                .stream()
                .map(AuthorizationGrantType::getValue)
                .map(OAuthGrantType::from)
                .collect(Collectors.toSet());
        return new ClientDetails(client.getId(), client.getClientId(), client.getClientName(),
                clientAuthenticationMethods, authorizationGrantTypes, client.getRedirectUris(),
                client.getPostLogoutRedirectUris(), client.getScopes(), toClientSetting(client.getClientSettings()),
                toTokenSetting(client.getTokenSettings()), null);
    }

    private static ClientDetailsTokens toTokenSetting(TokenSettings settings) {
        Map<String, Object> customSettings = settings.getSettings()
                .entrySet()
                .stream()
                .filter(entry -> !KNOWN_TOKEN_SETTINGS.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return new ClientDetailsTokens(settings.getAuthorizationCodeTimeToLive(), settings.getAccessTokenTimeToLive(),
                settings.getAccessTokenFormat(), settings.getDeviceCodeTimeToLive(), settings.isReuseRefreshTokens(),
                settings.getRefreshTokenTimeToLive(), settings.getIdTokenSignatureAlgorithm(),
                settings.isX509CertificateBoundAccessTokens(), customSettings);
    }

    private static ClientDetailsSettings toClientSetting(ClientSettings settings) {
        Map<String, Object> customSettings = settings.getSettings()
                .entrySet()
                .stream()
                .filter(entry -> !KNOWN_CLIENT_SETTINGS.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return new ClientDetailsSettings(settings.isRequireProofKey(), settings.isRequireAuthorizationConsent(),
                settings.getJwkSetUrl(), (SignatureAlgorithm) settings.getTokenEndpointAuthenticationSigningAlgorithm(),
                settings.getX509CertificateSubjectDN(), customSettings);
    }

    public static RegisteredClient toRegisteredClient(ClientDetails details) {
        return toRegisteredClient(details, null);
    }

    public static RegisteredClient toRegisteredClient(ClientDetails details, RegisteredClient existingClient) {
        final var tokenSettingsBuilder = fillTokenSettingsBuilder(details);
        final var clientSettingsBuilder = fillClientSettingsBuilder(details);
        RegisteredClient.Builder builder = RegisteredClient.withId(details.id())
                .clientId(details.clientId())
                .clientName(details.clientName());

        if (existingClient != null) {
            builder.clientSecret(existingClient.getClientSecret())
                    .clientIdIssuedAt(existingClient.getClientIdIssuedAt())
                    .clientSecretExpiresAt(existingClient.getClientSecretExpiresAt());
        }

        builder.clientAuthenticationMethods(clientAuthenticationMethods -> {
            if (details.clientAuthenticationMethods() != null) {
                Set<ClientAuthenticationMethod> newClientAuthenticationMethods = details.clientAuthenticationMethods()
                        .stream()
                        .map(ClientAuthMethod::value)
                        .map(ClientAuthenticationMethod::new)
                        .collect(Collectors.toSet());
                clientAuthenticationMethods.addAll(newClientAuthenticationMethods);
            }
        }).authorizationGrantTypes(authorizationGrantTypes -> {
            if (details.authorizationGrantTypes() != null) {
                Set<AuthorizationGrantType> newAuthorizationGrantTypes = details.authorizationGrantTypes()
                        .stream()
                        .map(OAuthGrantType::value)
                        .map(AuthorizationGrantType::new)
                        .collect(Collectors.toSet());
                authorizationGrantTypes.addAll(newAuthorizationGrantTypes);
            }
        }).redirectUris(redirectUris -> {
            if (details.redirectUris() != null) {
                redirectUris.addAll(details.redirectUris());
            }
        }).postLogoutRedirectUris(postLogoutRedirectUris -> {
            if (details.postLogoutRedirectUris() != null) {
                postLogoutRedirectUris.addAll(details.postLogoutRedirectUris());
            }
        }).scopes(scopes -> {
            if (details.scopes() != null) {
                scopes.addAll(details.scopes());
            }
        }).tokenSettings(tokenSettingsBuilder.build()).clientSettings(clientSettingsBuilder.build());

        return builder.build();
    }

    private static TokenSettings.Builder fillTokenSettingsBuilder(ClientDetails details) {
        var tokenSettingsBuilder = TokenSettings.builder();
        ClientDetailsTokens clientDetailsTokens = details.tokenSettings();
        if (clientDetailsTokens != null) {
            if (Objects.nonNull(clientDetailsTokens.authorizationCodeTimeToLive())) {
                tokenSettingsBuilder.authorizationCodeTimeToLive(clientDetailsTokens.authorizationCodeTimeToLive());
            }
            if (Objects.nonNull(clientDetailsTokens.accessTokenTimeToLive())) {
                tokenSettingsBuilder.accessTokenTimeToLive(clientDetailsTokens.accessTokenTimeToLive());
            }
            if (Objects.nonNull(clientDetailsTokens.accessTokenFormat())) {
                tokenSettingsBuilder.accessTokenFormat(clientDetailsTokens.accessTokenFormat());
            }
            if (Objects.nonNull(clientDetailsTokens.deviceCodeTimeToLive())) {
                tokenSettingsBuilder.deviceCodeTimeToLive(clientDetailsTokens.deviceCodeTimeToLive());
            }
            if (Objects.nonNull(clientDetailsTokens.refreshTokenTimeToLive())) {
                tokenSettingsBuilder.refreshTokenTimeToLive(clientDetailsTokens.refreshTokenTimeToLive());
            }
            if (Objects.nonNull(clientDetailsTokens.idTokenSignatureAlgorithm())) {
                tokenSettingsBuilder.idTokenSignatureAlgorithm(clientDetailsTokens.idTokenSignatureAlgorithm());
            }
            tokenSettingsBuilder.reuseRefreshTokens(clientDetailsTokens.reuseRefreshTokens());
            tokenSettingsBuilder
                    .x509CertificateBoundAccessTokens(clientDetailsTokens.x509CertificateBoundAccessTokens());

            if (clientDetailsTokens.customSettings() != null) {
                tokenSettingsBuilder.settings(settings -> settings.putAll(clientDetailsTokens.customSettings()));
            }
        }
        return tokenSettingsBuilder;
    }

    private static ClientSettings.Builder fillClientSettingsBuilder(ClientDetails details) {
        var clientSettingsBuilder = ClientSettings.builder();
        ClientDetailsSettings clientDetailsSettings = details.clientSettings();
        if (clientDetailsSettings != null) {

            clientSettingsBuilder.requireProofKey(clientDetailsSettings.requireProofKey());

            clientSettingsBuilder.requireAuthorizationConsent(clientDetailsSettings.requireAuthorizationConsent());
            if (Objects.nonNull(clientDetailsSettings.jwkSetUrl())) {
                clientSettingsBuilder.jwkSetUrl(clientDetailsSettings.jwkSetUrl());
            }
            if (Objects.nonNull(clientDetailsSettings.tokenEndpointAuthenticationSigningAlgorithm())) {
                clientSettingsBuilder.tokenEndpointAuthenticationSigningAlgorithm(
                        clientDetailsSettings.tokenEndpointAuthenticationSigningAlgorithm());
            }
            if (Objects.nonNull(clientDetailsSettings.x509CertificateSubjectDN())) {
                clientSettingsBuilder.x509CertificateSubjectDN(clientDetailsSettings.x509CertificateSubjectDN());
            }

            if (clientDetailsSettings.customSettings() != null) {
                clientSettingsBuilder.settings(settings -> settings.putAll(clientDetailsSettings.customSettings()));
            }

        }
        return clientSettingsBuilder;
    }

}
