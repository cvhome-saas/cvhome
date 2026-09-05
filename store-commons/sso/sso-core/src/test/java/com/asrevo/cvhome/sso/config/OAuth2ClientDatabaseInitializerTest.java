package com.asrevo.cvhome.sso.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import com.asrevo.cvhome.s2s.model.AppProperties;
import com.asrevo.cvhome.s2s.model.OAuth2ClientProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The boot-time reconciliation of configured OAuth2 clients against the rows already in the database.
 *
 * <p>
 * It only ever <em>updates</em> a client that already exists: the seeded rows come from SQL, and creating one from
 * configuration would let a misconfigured deployment mint a client nobody registered. A configured client with no
 * row is logged and skipped rather than inserted.
 * </p>
 *
 * <p>
 * Every field is applied only when configuration actually names it. That is what lets a deployment override just
 * the secret — the common case — without wiping the redirect URIs the SQL seeded, which would break the console's
 * sign-in the next time the service restarted.
 * </p>
 */
class OAuth2ClientDatabaseInitializerTest {

    private static final String OPENID = "openid";

    private static final String SECRET = "secret";

    private static final String HASHED = "{bcrypt}hashed";

    private static final String STORE_CORE_SCOPE = "store_core";

    private static final String CLIENT_CREDENTIALS = "client_credentials";

    private static final String CLIENT_ID = "web-app";
    private static final String CALLBACK = "/callback";
    private static final String SEEDED_REDIRECT = "https://seeded.example.com/callback";

    private final RegisteredClientRepository clients = mock(RegisteredClientRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);

    private static AppProperties app() {
        AppProperties properties = new AppProperties();
        properties.setDomain("gateway.com");
        properties.setHandlers(List.of(new AppProperties.Handler("https", "443")));
        return properties;
    }

    private static RegisteredClient seeded() {
        return RegisteredClient.withId("id-1").clientId(CLIENT_ID)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(SEEDED_REDIRECT)
                .scope(OPENID)
                .build();
    }

    private OAuth2ClientDatabaseInitializer initializerFor(Map<String, OAuth2ClientProperties.ClientInfo> configured) {
        return new OAuth2ClientDatabaseInitializer(new OAuth2ClientProperties(configured), app(), clients, encoder);
    }

    private RegisteredClient saved() {
        ArgumentCaptor<RegisteredClient> captor = ArgumentCaptor.forClass(RegisteredClient.class);
        verify(clients).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void noConfiguredClientsMeansNothingIsTouched() {
        initializerFor(null).onApplicationReady();
        initializerFor(Map.of()).onApplicationReady();

        Mockito.verifyNoInteractions(clients);
    }

    @Test
    void aConfiguredClientWithNoRowIsSkippedRatherThanCreated() {
        when(clients.findByClientId(CLIENT_ID)).thenReturn(null);

        initializerFor(Map.of(CLIENT_ID,
                new OAuth2ClientProperties.ClientInfo(SECRET, null, null, null, null))).onApplicationReady();

        // Creating one here would let a misconfigured deployment mint a client nobody registered.
        verify(clients, never()).save(any());
    }

    @Test
    void asecretOnlyOverrideLeavesTheSeededRedirectsAlone() {
        when(clients.findByClientId(CLIENT_ID)).thenReturn(seeded());
        when(encoder.encode(SECRET)).thenReturn(HASHED);

        initializerFor(Map.of(CLIENT_ID,
                new OAuth2ClientProperties.ClientInfo(SECRET, null, null, null, null))).onApplicationReady();

        // Wiping them would break the console's sign-in at the next restart.
        assertThat(saved().getRedirectUris()).containsExactly(SEEDED_REDIRECT);
        assertThat(saved().getClientSecret()).isEqualTo(HASHED);
    }

    @Test
    void ablankSecretIsNoSecretRatherThanAnEmptyOne() {
        when(clients.findByClientId(CLIENT_ID)).thenReturn(seeded());

        initializerFor(Map.of(CLIENT_ID,
                new OAuth2ClientProperties.ClientInfo("   ", null, null, null, null))).onApplicationReady();

        // Encoding a blank would make every client share one guessable secret.
        Mockito.verifyNoInteractions(encoder);
    }

    @Test
    void redirectPathsAreExpandedAcrossEveryConfiguredHost() {
        when(clients.findByClientId(CLIENT_ID)).thenReturn(seeded());
        AppProperties properties = app();
        properties.setSub(new java.util.LinkedHashSet<>(Set.of("console")));

        new OAuth2ClientDatabaseInitializer(new OAuth2ClientProperties(Map.of(CLIENT_ID,
                new OAuth2ClientProperties.ClientInfo(null, Set.of(CALLBACK), Set.of("/"), null, null))),
                properties, clients, encoder).onApplicationReady();

        // One path times every host the app answers on; a store reachable on two hosts needs both registered.
        assertThat(saved().getRedirectUris()).anyMatch(it -> it.endsWith(CALLBACK));
        assertThat(saved().getRedirectUris()).contains(SEEDED_REDIRECT);
    }

    @Test
    void scopesAndGrantTypesAreAddedWhenNamed() {
        when(clients.findByClientId(CLIENT_ID)).thenReturn(seeded());

        initializerFor(Map.of(CLIENT_ID, new OAuth2ClientProperties.ClientInfo(null, null, null,
                Set.of(STORE_CORE_SCOPE), Set.of(CLIENT_CREDENTIALS)))).onApplicationReady();

        assertThat(saved().getScopes()).contains(STORE_CORE_SCOPE, OPENID);
        assertThat(saved().getAuthorizationGrantTypes())
                .contains(new AuthorizationGrantType(CLIENT_CREDENTIALS),
                        AuthorizationGrantType.AUTHORIZATION_CODE);
    }

    @Test
    void anEmptyCollectionIsTreatedAsUnsetRatherThanAsAnInstructionToClear() {
        when(clients.findByClientId(CLIENT_ID)).thenReturn(seeded());

        initializerFor(Map.of(CLIENT_ID, new OAuth2ClientProperties.ClientInfo(null, Set.of(), Set.of(), Set.of(),
                Set.of()))).onApplicationReady();

        assertThat(saved().getRedirectUris()).containsExactly(SEEDED_REDIRECT);
        assertThat(saved().getScopes()).containsExactly(OPENID);
    }
}
