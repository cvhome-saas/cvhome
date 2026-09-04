package com.asrevo.cvhome.sso.mapper;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import com.asrevo.cvhome.sso.dto.ClientDetails;
import com.asrevo.cvhome.sso.dto.ClientDetailsSettings;
import com.asrevo.cvhome.sso.dto.ClientDetailsTokens;
import com.asrevo.cvhome.sso.service.ClientAuthMethod;
import com.asrevo.cvhome.sso.service.OAuthGrantType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * A registration, in both directions.
 *
 * <p>
 * <strong>The secret never appears in a {@link ClientDetails}.</strong> That is what makes it safe for the console
 * to read one back, and it is why an update has to be given the existing client to copy the secret, its issue time
 * and its expiry from — an update that dropped them would silently unauthenticate every holder of that client's
 * credentials.
 * </p>
 *
 * <p>
 * The custom-settings maps carry whatever Spring stores that is not one of the named settings, so a registration
 * written by a newer Spring survives a read-modify-write here instead of losing the settings this version does not
 * know about.
 * </p>
 *
 * <p>
 * Every optional token and client setting is null-guarded, because Spring's builders reject a null and a partially
 * filled form from the console is the normal case, not the exception.
 * </p>
 */
class ClientClientDetailsMapperTest {

    private static final String ID = "reg-1";
    private static final String CLIENT_ID = "console";
    private static final String CLIENT_NAME = "The Console";
    private static final String REDIRECT = "https://console.example/cb";
    private static final String LOGOUT_REDIRECT = "https://console.example/bye";
    private static final String SCOPE = "openid";
    private static final String JWK_SET_URL = "https://console.example/jwks";
    private static final String SUBJECT_DN = "CN=console";
    private static final String CUSTOM_KEY = "settings.token.our-own";
    private static final String SECRET = "{noop}s3cret";
    private static final String KEPT = "kept";

    @Test
    void afullyPopulatedRegistrationSurvivesTheRoundTrip() {
        ClientDetails details = ClientClientDetailsMapper.toClientDetails(registeredClient());

        RegisteredClient back = ClientClientDetailsMapper.toRegisteredClient(details);

        assertThat(back.getClientId()).isEqualTo(CLIENT_ID);
        assertThat(back.getClientName()).isEqualTo(CLIENT_NAME);
        assertThat(back.getRedirectUris()).containsExactly(REDIRECT);
        assertThat(back.getPostLogoutRedirectUris()).containsExactly(LOGOUT_REDIRECT);
        assertThat(back.getScopes()).containsExactly(SCOPE);
        assertThat(back.getClientAuthenticationMethods())
                .containsExactly(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
        assertThat(back.getAuthorizationGrantTypes())
                .containsExactlyInAnyOrder(AuthorizationGrantType.AUTHORIZATION_CODE,
                        AuthorizationGrantType.REFRESH_TOKEN);
    }

    @Test
    void everyTokenSettingIsCarriedAcross() {
        RegisteredClient back = ClientClientDetailsMapper.toRegisteredClient(
                ClientClientDetailsMapper.toClientDetails(registeredClient()));

        TokenSettings tokens = back.getTokenSettings();
        assertThat(tokens.getAuthorizationCodeTimeToLive()).isEqualTo(Duration.ofMinutes(2));
        assertThat(tokens.getAccessTokenTimeToLive()).isEqualTo(Duration.ofMinutes(10));
        assertThat(tokens.getAccessTokenFormat()).isEqualTo(OAuth2TokenFormat.REFERENCE);
        assertThat(tokens.getDeviceCodeTimeToLive()).isEqualTo(Duration.ofMinutes(6));
        assertThat(tokens.getRefreshTokenTimeToLive()).isEqualTo(Duration.ofDays(1));
        assertThat(tokens.getIdTokenSignatureAlgorithm()).isEqualTo(SignatureAlgorithm.ES256);
        assertThat(tokens.isReuseRefreshTokens()).isFalse();
        assertThat(tokens.isX509CertificateBoundAccessTokens()).isTrue();
    }

    @Test
    void everyClientSettingIsCarriedAcross() {
        RegisteredClient back = ClientClientDetailsMapper.toRegisteredClient(
                ClientClientDetailsMapper.toClientDetails(registeredClient()));

        ClientSettings settings = back.getClientSettings();
        assertThat(settings.isRequireProofKey()).isTrue();
        assertThat(settings.isRequireAuthorizationConsent()).isTrue();
        assertThat(settings.getJwkSetUrl()).isEqualTo(JWK_SET_URL);
        assertThat(settings.<SignatureAlgorithm>getTokenEndpointAuthenticationSigningAlgorithm())
                .isEqualTo(SignatureAlgorithm.RS512);
        assertThat(settings.getX509CertificateSubjectDN()).isEqualTo(SUBJECT_DN);
    }

    @Test
    void settingsThisVersionDoesNotKnowAboutAreKeptRatherThanDropped() {
        ClientDetails details = ClientClientDetailsMapper.toClientDetails(registeredClient());

        assertThat(details.tokenSettings().customSettings()).containsEntry(CUSTOM_KEY, KEPT);
        assertThat(ClientClientDetailsMapper.toRegisteredClient(details).getTokenSettings().getSettings())
                .containsEntry(CUSTOM_KEY, KEPT);
    }

    @Test
    void thenamedSettingsAreNotAlsoRepeatedInTheCustomMap() {
        ClientDetails details = ClientClientDetailsMapper.toClientDetails(registeredClient());

        assertThat(details.tokenSettings().customSettings())
                .doesNotContainKey("settings.token.access-token-time-to-live");
        assertThat(details.clientSettings().customSettings())
                .doesNotContainKey("settings.client.require-proof-key");
    }

    @Test
    void thesecretNeverAppearsInAclientDetails() {
        ClientDetails details = ClientClientDetailsMapper.toClientDetails(registeredClient());

        // The console reads these back; a secret in one would be a secret in a log, a cache and a browser.
        assertThat(details.toString()).doesNotContain(SECRET);
        assertThat(ClientClientDetailsMapper.toRegisteredClient(details).getClientSecret()).isNull();
    }

    @Test
    void anUpdateCopiesTheSecretAndItsDatesFromTheClientBeingUpdated() {
        RegisteredClient existing = registeredClient();
        ClientDetails details = ClientClientDetailsMapper.toClientDetails(existing);

        RegisteredClient updated = ClientClientDetailsMapper.toRegisteredClient(details, existing);

        // Dropping these would silently unauthenticate every holder of this client's credentials.
        assertThat(updated.getClientSecret()).isEqualTo(SECRET);
        assertThat(updated.getClientIdIssuedAt()).isEqualTo(existing.getClientIdIssuedAt());
        assertThat(updated.getClientSecretExpiresAt()).isEqualTo(existing.getClientSecretExpiresAt());
    }

    @Test
    void aformThatFilledInAlmostNothingStillMapsRatherThanRejectingAnull() {
        ClientDetails sparse = new ClientDetails(ID, CLIENT_ID, CLIENT_NAME,
                Set.of(ClientAuthMethod.CLIENT_SECRET_BASIC), Set.of(OAuthGrantType.CLIENT_CREDENTIALS),
                null, null, null,
                new ClientDetailsSettings(false, false, null, null, null, null),
                new ClientDetailsTokens(null, null, null, null, true, null, null, false, null), null);

        RegisteredClient client = ClientClientDetailsMapper.toRegisteredClient(sparse);

        // Spring's builders reject a null; a partly filled console form is the normal case, not the exception.
        assertThat(client.getRedirectUris()).isEmpty();
        assertThat(client.getPostLogoutRedirectUris()).isEmpty();
        assertThat(client.getScopes()).isEmpty();
        assertThat(client.getClientSettings().getJwkSetUrl()).isNull();
        assertThat(client.getTokenSettings().isReuseRefreshTokens()).isTrue();
    }

    @Test
    void aformNamingNoGrantTypeIsRefusedBySpringRatherThanRegisteredAsUnusable() {
        ClientDetails empty = new ClientDetails(ID, CLIENT_ID, CLIENT_NAME, null, null, Set.of(REDIRECT), Set.of(),
                Set.of(SCOPE), new ClientDetailsSettings(false, false, null, null, null, Map.of()),
                new ClientDetailsTokens(null, null, null, null, false, null, null, false, Map.of()), null);

        // The mapper's null guards stop an NPE, not an empty registration: a client that can perform no grant
        // could never obtain a token, so Spring's own validation is what refuses it.
        assertThatThrownBy(() -> ClientClientDetailsMapper.toRegisteredClient(empty))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("authorizationGrantTypes");
    }

    @Test
    void aformNamingNoAuthenticationMethodFallsBackToBasicRatherThanToApublicClient() {
        ClientDetails noMethod = new ClientDetails(ID, CLIENT_ID, CLIENT_NAME, null,
                Set.of(OAuthGrantType.AUTHORIZATION_CODE), Set.of(REDIRECT), Set.of(), Set.of(SCOPE),
                new ClientDetailsSettings(false, false, null, null, null, Map.of()),
                new ClientDetailsTokens(null, null, null, null, false, null, null, false, Map.of()), null);

        // Spring's own default. Falling back to `none` instead would silently make the client public.
        assertThat(ClientClientDetailsMapper.toRegisteredClient(noMethod).getClientAuthenticationMethods())
                .containsExactly(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
    }

    private static RegisteredClient registeredClient() {
        return RegisteredClient.withId(ID)
                .clientId(CLIENT_ID)
                .clientName(CLIENT_NAME)
                .clientSecret(SECRET)
                .clientIdIssuedAt(Instant.parse("2026-01-01T00:00:00Z"))
                .clientSecretExpiresAt(Instant.parse("2027-01-01T00:00:00Z"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri(REDIRECT)
                .postLogoutRedirectUri(LOGOUT_REDIRECT)
                .scope(SCOPE)
                .tokenSettings(TokenSettings.builder()
                        .authorizationCodeTimeToLive(Duration.ofMinutes(2))
                        .accessTokenTimeToLive(Duration.ofMinutes(10))
                        .accessTokenFormat(OAuth2TokenFormat.REFERENCE)
                        .deviceCodeTimeToLive(Duration.ofMinutes(6))
                        .refreshTokenTimeToLive(Duration.ofDays(1))
                        .idTokenSignatureAlgorithm(SignatureAlgorithm.ES256)
                        .reuseRefreshTokens(false)
                        .x509CertificateBoundAccessTokens(true)
                        .setting(CUSTOM_KEY, KEPT)
                        .build())
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(true)
                        .requireAuthorizationConsent(true)
                        .jwkSetUrl(JWK_SET_URL)
                        .tokenEndpointAuthenticationSigningAlgorithm(SignatureAlgorithm.RS512)
                        .x509CertificateSubjectDN(SUBJECT_DN)
                        .build())
                .build();
    }

}
