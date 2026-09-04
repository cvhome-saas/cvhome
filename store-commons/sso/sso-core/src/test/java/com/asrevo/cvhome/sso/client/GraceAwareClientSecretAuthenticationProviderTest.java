package com.asrevo.cvhome.sso.client;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import com.asrevo.cvhome.sso.domain.ClientSecretHistory;
import com.asrevo.cvhome.sso.repo.ClientSecretHistoryRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The live secret and a secret inside its grace window both authenticate; a revoked, expired or unknown one does not.
 * The encoder is the plain-text one so the test reads as what it checks.
 */
class GraceAwareClientSecretAuthenticationProviderTest {

    /** Real time, not a fixed instant: Spring's provider checks a retired hash's expiry against its own clock. */
    private static final Instant NOW = Instant.now();

    private static final String ID = "reg-1";

    private static final String CLIENT_ID = "svc";

    private static final String LIVE = "{noop}live";

    private static final String OLD = "{noop}old";

    private static final String ENCODED_PREFIX = "{noop}";

    private static final String OLD_RAW = "old";

    private final RegisteredClientRepository clients = mock(RegisteredClientRepository.class);

    private final ClientSecretHistoryRepository history = mock(ClientSecretHistoryRepository.class);

    private final GraceAwareClientSecretAuthenticationProvider provider = new GraceAwareClientSecretAuthenticationProvider(
            clients, mock(OAuth2AuthorizationService.class), new PlainEncoder(), history,
            Clock.fixed(NOW, ZoneOffset.UTC));

    private final RegisteredClient client = RegisteredClient.withId(ID).clientId(CLIENT_ID).clientSecret(LIVE)
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS).build();

    private static OAuth2ClientAuthenticationToken presenting(String secret) {
        return new OAuth2ClientAuthenticationToken(CLIENT_ID, ClientAuthenticationMethod.CLIENT_SECRET_BASIC, secret, null);
    }

    @Test
    void theLiveSecretAuthenticates() {
        when(clients.findByClientId(CLIENT_ID)).thenReturn(client);

        Authentication result = provider.authenticate(presenting("live"));

        assertThat(result.isAuthenticated()).isTrue();
        assertThat(((OAuth2ClientAuthenticationToken) result).getRegisteredClient().getClientSecret()).isEqualTo(LIVE);
    }

    @Test
    void aSecretInsideItsGraceWindowAuthenticates() {
        when(clients.findByClientId(CLIENT_ID)).thenReturn(client);
        when(history.findByRegisteredClientIdAndRevokedAtIsNull(ID))
                .thenReturn(List.of(ClientSecretHistory.retire(ID, OLD, NOW.minusSeconds(60), NOW.plusSeconds(3600))));

        Authentication result = provider.authenticate(presenting(OLD_RAW));

        assertThat(result.isAuthenticated()).isTrue();
    }

    @Test
    void anExpiredOrRevokedPreviousSecretDoesNot() {
        when(clients.findByClientId(CLIENT_ID)).thenReturn(client);
        ClientSecretHistory revoked = ClientSecretHistory.retire(ID, OLD, NOW.minusSeconds(60), NOW.plusSeconds(3600));
        revoked.setRevokedAt(NOW);
        ClientSecretHistory expired = ClientSecretHistory.retire(ID, OLD, NOW.minusSeconds(7200), NOW.minusSeconds(1));
        when(history.findByRegisteredClientIdAndRevokedAtIsNull(ID)).thenReturn(List.of(revoked, expired));

        assertThatThrownBy(() -> provider.authenticate(presenting(OLD_RAW))).isInstanceOf(OAuth2AuthenticationException.class);
    }

    @Test
    void anUnknownClientOrWrongSecretIsInvalidClient() {
        when(clients.findByClientId(CLIENT_ID)).thenReturn(client);

        assertThatThrownBy(() -> provider.authenticate(presenting("nope"))).isInstanceOf(OAuth2AuthenticationException.class);
        assertThatThrownBy(() -> provider.authenticate(new OAuth2ClientAuthenticationToken("ghost",
                ClientAuthenticationMethod.CLIENT_SECRET_BASIC, "x", null))).isInstanceOf(OAuth2AuthenticationException.class);
    }

    @Test
    void otherMethodsAreLeftToOtherProviders() {
        assertThat(provider.authenticate(new OAuth2ClientAuthenticationToken(CLIENT_ID, ClientAuthenticationMethod.NONE, null,
                null))).isNull();
    }

    /** {@code {noop}} prefixed comparison, so the hashes above read as the secrets they hold. */
    private static final class PlainEncoder implements org.springframework.security.crypto.password.PasswordEncoder {

        @Override
        public String encode(CharSequence rawPassword) {
            return ENCODED_PREFIX + rawPassword;
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return encodedPassword != null && (ENCODED_PREFIX + rawPassword).equals(encodedPassword);
        }

    }

}
