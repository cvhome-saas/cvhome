package com.asrevo.cvhome.sso.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2TokenRevocationAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import com.asrevo.cvhome.sso.audit.AuditActorType;
import com.asrevo.cvhome.sso.audit.AuditEventType;
import com.asrevo.cvhome.sso.audit.AuditOutcome;
import com.asrevo.cvhome.sso.audit.AuditRecord;
import com.asrevo.cvhome.sso.audit.AuditRecords;
import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.audit.AuditTargetType;
import com.asrevo.cvhome.sso.domain.ClientExtension;
import com.asrevo.cvhome.sso.repo.ClientExtensionRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The protocol events no controller sees: a token issued, a token revoked, a client that failed to authenticate.
 *
 * <p>
 * The listener shares its event stream with {@link AuthenticationAuditListener}, so the discrimination matters as
 * much as the recording: every {@code AuthenticationSuccessEvent} in the realm reaches both handler methods here,
 * and anything that is not the OAuth2 token it expects must fall straight through. A form login recorded as a
 * token issue would put a person's sign-in in the clients' audit trail under a client actor.
 * </p>
 *
 * <p>
 * Issuing a token also stamps {@code last_token_issued_at} on the client, which is what makes an unused
 * registration visible in the console — so the stamp is asserted alongside the row, including the case where the
 * client has no extension row yet and one has to be created.
 * </p>
 */
class ProtocolAuditListenerTest {

    private static final String NOW = "2026-04-01T09:30:00Z";
    private static final String CLIENT_ID = "console";
    private static final String REGISTRATION_ID = "reg-1";
    private static final String TOKEN_VALUE = "token-value";
    private static final String PERSON = "someone";
    private static final String ACCOUNT_ID = "00000000-0000-0000-0000-000000000009";
    private static final String SCOPE_READ = "read";
    private static final String DESCRIPTION = "the console";
    private static final String INVALID_CLIENT = "INVALID_CLIENT";
    private static final String WRONG_SECRET = "no";

    private final AuditService audit = mock(AuditService.class);
    private final PrincipalNames principals = mock(PrincipalNames.class);
    private final ClientExtensionRepository extensions = mock(ClientExtensionRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse(NOW), ZoneOffset.UTC);
    private final ProtocolAuditListener listener =
            new ProtocolAuditListener(audit, principals, extensions, clock);

    @Test
    void aclientCredentialsTokenIsRecordedAgainstTheClientWithItsGrantScopesAndTtl() {
        when(extensions.findById(REGISTRATION_ID)).thenReturn(Optional.empty());

        listener.onTokenIssued(new AuthenticationSuccessEvent(clientCredentialsToken(Set.of(SCOPE_READ, "admin"))));

        AuditRecord record = recorded();
        assertThat(AuditRecords.typeOf(record)).isEqualTo(AuditEventType.TOKEN_ISSUED);
        assertThat(AuditRecords.actorOf(record).type()).isEqualTo(AuditActorType.CLIENT);
        assertThat(AuditRecords.actorOf(record).name()).isEqualTo(CLIENT_ID);
        assertThat(AuditRecords.clientIdOf(record)).isEqualTo(CLIENT_ID);
        assertThat(AuditRecords.targetTypeOf(record)).isEqualTo(AuditTargetType.TOKEN);
        // Scopes are sorted so two identical grants produce the same audit line.
        assertThat(AuditRecords.detailOf(record)).isEqualTo("grant=client_credentials scopes=[admin read] ttl=600s");
    }

    @Test
    void arefreshTokenAlongsideTheAccessTokenMarksTheRowAsAnAuthorizationCodeGrant() {
        when(extensions.findById(REGISTRATION_ID)).thenReturn(Optional.empty());
        when(principals.display(ACCOUNT_ID)).thenReturn(PERSON);

        listener.onTokenIssued(new AuthenticationSuccessEvent(authorizationCodeToken()));

        AuditRecord record = recorded();
        assertThat(AuditRecords.detailOf(record)).startsWith("grant=authorization_code");
        // The token's principal is the person for an authorization code, and the row names them, not their id.
        assertThat(AuditRecords.targetNameOf(record)).isEqualTo(PERSON);
    }

    @Test
    void atokenWithNoScopesRecordsAnEmptyScopeListRatherThanTheWordNull() {
        when(extensions.findById(REGISTRATION_ID)).thenReturn(Optional.empty());

        listener.onTokenIssued(new AuthenticationSuccessEvent(clientCredentialsToken(Set.of())));

        assertThat(AuditRecords.detailOf(recorded())).contains("scopes=[]");
    }

    @Test
    void issuingAtokenStampsTheClientEvenWhenItHasNoExtensionRowYet() {
        when(extensions.findById(REGISTRATION_ID)).thenReturn(Optional.empty());

        listener.onTokenIssued(new AuthenticationSuccessEvent(clientCredentialsToken(Set.of(SCOPE_READ))));

        ClientExtension saved = savedExtension();
        assertThat(saved.getRegisteredClientId()).isEqualTo(REGISTRATION_ID);
        assertThat(saved.getLastTokenIssuedAt()).isEqualTo(Instant.parse(NOW));
        assertThat(saved.getUpdatedAt()).isEqualTo(Instant.parse(NOW));
    }

    @Test
    void anExistingExtensionRowIsStampedRatherThanReplaced() {
        ClientExtension existing = ClientExtension.create(REGISTRATION_ID, DESCRIPTION,
                Instant.parse(NOW).minus(Duration.ofDays(30)));
        when(extensions.findById(REGISTRATION_ID)).thenReturn(Optional.of(existing));

        listener.onTokenIssued(new AuthenticationSuccessEvent(clientCredentialsToken(Set.of(SCOPE_READ))));

        ClientExtension saved = savedExtension();
        assertThat(saved).isSameAs(existing);
        assertThat(saved.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(saved.getLastTokenIssuedAt()).isEqualTo(Instant.parse(NOW));
    }

    @Test
    void arevocationIsAttributedToTheClientThatAskedForIt() {
        listener.onTokenRevoked(new AuthenticationSuccessEvent(revocation(clientPrincipal())));

        AuditRecord record = recorded();
        assertThat(AuditRecords.typeOf(record)).isEqualTo(AuditEventType.TOKEN_REVOKED);
        assertThat(AuditRecords.clientIdOf(record)).isEqualTo(CLIENT_ID);
        assertThat(AuditRecords.detailOf(record)).isEqualTo("revocation endpoint");
    }

    @Test
    void arevocationWithNoIdentifiableClientIsStillRecorded() {
        listener.onTokenRevoked(new AuthenticationSuccessEvent(
                revocation(new TestingAuthenticationToken(CLIENT_ID, null, List.of()))));

        // Better an anonymous row than a missing one: a revocation nobody can attribute is itself worth seeing.
        assertThat(AuditRecords.clientIdOf(recorded())).isEqualTo(ProtocolAuditListener.ANONYMOUS_CLIENT);
    }

    @Test
    void afailedClientAuthenticationIsRecordedAsAfailureUnderTheOauthErrorCode() {
        OAuth2ClientAuthenticationToken client = clientPrincipal();

        listener.onClientAuthFailure(new AuthenticationFailureBadCredentialsEvent(client,
                new OAuth2AuthenticationException(new OAuth2Error("invalid_client"))));

        AuditRecord record = recorded();
        assertThat(AuditRecords.typeOf(record)).isEqualTo(AuditEventType.CLIENT_AUTH_FAILED);
        assertThat(AuditRecords.outcomeOf(record)).isEqualTo(AuditOutcome.FAILURE);
        assertThat(AuditRecords.reasonCodeOf(record)).isEqualTo(INVALID_CLIENT);
        assertThat(AuditRecords.actorOf(record).type()).isEqualTo(AuditActorType.ANONYMOUS);
        // The client id is what the caller claimed — which is exactly what an operator needs to see.
        assertThat(AuditRecords.targetIdOf(record)).isNull();
        assertThat(AuditRecords.targetNameOf(record)).isEqualTo(CLIENT_ID);
        assertThat(AuditRecords.detailOf(record)).isEqualTo(ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue());
    }

    @Test
    void anonOauthFailureFallsBackToTheCatchAllReason() {
        OAuth2ClientAuthenticationToken client = clientPrincipal();

        listener.onClientAuthFailure(new AuthenticationFailureBadCredentialsEvent(client,
                new org.springframework.security.authentication.BadCredentialsException(WRONG_SECRET)));

        assertThat(AuditRecords.reasonCodeOf(recorded())).isEqualTo(INVALID_CLIENT);
    }

    @Test
    void anAuthenticationThatIsNotAnOauthTokenFallsStraightThrough() {
        Authentication other = new TestingAuthenticationToken(PERSON, null, List.of());

        listener.onTokenIssued(new AuthenticationSuccessEvent(other));
        listener.onTokenRevoked(new AuthenticationSuccessEvent(other));
        listener.onClientAuthFailure(new AuthenticationFailureBadCredentialsEvent(other,
                new org.springframework.security.authentication.BadCredentialsException(WRONG_SECRET)));

        // A form login recorded here would file a person's sign-in under a client actor.
        Mockito.verifyNoInteractions(audit);
        Mockito.verifyNoInteractions(extensions);
    }

    private AuditRecord recorded() {
        ArgumentCaptor<AuditRecord> captor = ArgumentCaptor.forClass(AuditRecord.class);
        verify(audit).recordDetached(captor.capture());
        return captor.getValue();
    }

    private ClientExtension savedExtension() {
        ArgumentCaptor<ClientExtension> captor = ArgumentCaptor.forClass(ClientExtension.class);
        verify(extensions).save(captor.capture());
        return captor.getValue();
    }

    private static RegisteredClient client() {
        return RegisteredClient.withId(REGISTRATION_ID)
                .clientId(CLIENT_ID)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://example.com/cb")
                .scope(SCOPE_READ)
                .tokenSettings(TokenSettings.builder().accessTokenTimeToLive(Duration.ofMinutes(10)).build())
                .build();
    }

    private static OAuth2AccessToken accessToken(Set<String> scopes) {
        Instant now = Instant.parse(NOW);
        return new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, TOKEN_VALUE, now,
                now.plus(Duration.ofMinutes(10)), scopes);
    }

    private static OAuth2AccessTokenAuthenticationToken clientCredentialsToken(Set<String> scopes) {
        return new OAuth2AccessTokenAuthenticationToken(client(), clientPrincipal(), accessToken(scopes));
    }

    private static OAuth2AccessTokenAuthenticationToken authorizationCodeToken() {
        Authentication person = new TestingAuthenticationToken(ACCOUNT_ID, null, List.of());
        OAuth2RefreshToken refresh = new OAuth2RefreshToken("refresh-value", Instant.parse(NOW));
        return new OAuth2AccessTokenAuthenticationToken(client(), person, accessToken(Set.of(SCOPE_READ)), refresh);
    }

    private static OAuth2ClientAuthenticationToken clientPrincipal() {
        return new OAuth2ClientAuthenticationToken(client(), ClientAuthenticationMethod.CLIENT_SECRET_BASIC, "secret");
    }

    private static OAuth2TokenRevocationAuthenticationToken revocation(Authentication principal) {
        return new OAuth2TokenRevocationAuthenticationToken(TOKEN_VALUE, principal, "access_token");
    }

}
