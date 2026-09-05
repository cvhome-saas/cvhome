package com.asrevo.cvhome.sso.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.client.ClientType;
import com.asrevo.cvhome.sso.client.ClientsProperties;
import com.asrevo.cvhome.sso.client.RedirectUriRules;
import com.asrevo.cvhome.sso.domain.ClientSecretHistory;
import com.asrevo.cvhome.sso.dto.ClientDetails;
import com.asrevo.cvhome.sso.dto.ClientDetailsTokens;
import com.asrevo.cvhome.sso.dto.CreatedClient;
import com.asrevo.cvhome.sso.dto.RotatedSecret;
import com.asrevo.cvhome.sso.repo.ClientExtensionRepository;
import com.asrevo.cvhome.sso.repo.ClientSecretHistoryRepository;
import com.asrevo.cvhome.sso.settings.RealmSettings;
import com.asrevo.cvhome.sso.settings.SettingsService;
import com.asrevo.cvhome.sso.token.TokenRevocationService;
import com.asrevo.cvhome.uaa.errors.ClientIdTakenException;
import com.asrevo.cvhome.uaa.errors.ClientNotConfidentialException;
import com.asrevo.cvhome.uaa.errors.ClientNotFoundException;
import com.asrevo.cvhome.uaa.errors.ClientTokenTtlExceedsPolicyException;
import com.asrevo.cvhome.uaa.errors.InvalidRedirectUriException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The path decides which client an update writes; the body's own id is ignored. A secret is answered once, and a
 * rotation keeps the old hash alive for the realm's grace window.
 */
class AdminClientServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-02T12:00:00Z");

    private static final int GRACE_HOURS = 24;

    private static final int VALIDITY_DAYS = 365;

    private static final String CLIENT_A = "client-a";

    private static final String B = "B";

    private static final String NEW = "New";

    private static final String CALLBACK = "https://app.example/cb";

    private static final String A = "A";

    private static final String MISSING = "missing";

    private static final String IGNORED = "ignored";

    private static final String RENAMED = "Renamed";

    private static final String KEPT_SECRET = "{bcrypt}kept";

    private static final String GENERATED_SECRET = "{bcrypt}generated";

    private static final String SCOPE = "store_core";
    private static final String CLIENT_B = "client-b";
    private static final String OPENID = "openid";
    private static final String THE_CONSOLE = "the console";
    private static final String CHOSEN = "chosen";

    private final RegisteredClientRepository clients = mock(RegisteredClientRepository.class);

    private final PasswordEncoder encoder = mock(PasswordEncoder.class);

    private final ClientExtensionRepository extensions = mock(ClientExtensionRepository.class);

    private final ClientSecretHistoryRepository history = mock(ClientSecretHistoryRepository.class);

    private final SettingsService settings = mock(SettingsService.class);

    private final RealmSettings realm = mock(RealmSettings.class);

    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);

    private final AuditService audit = mock(AuditService.class);

    private final AdminClientService service = new AdminClientService(clients, encoder, jdbc,
            audit, extensions, history, settings,
            new RedirectUriRules(new ClientsProperties(List.of("localhost"))), mock(TokenRevocationService.class),
            Clock.fixed(NOW, java.time.ZoneOffset.UTC));

    {
        when(settings.current()).thenReturn(realm);
        when(realm.tokens()).thenReturn(new RealmSettings.Tokens(3600, 900, 43200, VALIDITY_DAYS, GRACE_HOURS));
        when(extensions.findById(anyString())).thenReturn(Optional.empty());
        when(history.findByRegisteredClientIdAndRevokedAtIsNull(anyString())).thenReturn(List.of());
    }

    private static RegisteredClient existing(String id) {
        return RegisteredClient.withId(id).clientId(String.format("client-%s", id)).clientName(id)
                .clientSecret(KEPT_SECRET)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope(SCOPE)
                .build();
    }

    private static ClientDetails details(String id, String name) {
        return new ClientDetails(id, CLIENT_A, name, Set.of(ClientAuthMethod.CLIENT_SECRET_BASIC),
                Set.of(OAuthGrantType.CLIENT_CREDENTIALS), Set.of(), Set.of(), Set.of(SCOPE), null, null, null);
    }

    private static ClientDetails withRedirect(String uri) {
        return new ClientDetails(A, CLIENT_B, B, Set.of(ClientAuthMethod.NONE), Set.of(OAuthGrantType.AUTHORIZATION_CODE),
                Set.of(uri), Set.of(), Set.of(OPENID), null, null, null);
    }

    @Test
    void updateWritesThePathClientWhateverTheBodyNames()
            throws ClientNotFoundException, InvalidRedirectUriException, ClientTokenTtlExceedsPolicyException {
        when(clients.findById(A)).thenReturn(existing(A));

        ClientDetails result = service.update(A, details(B, RENAMED));

        ArgumentCaptor<RegisteredClient> saved = ArgumentCaptor.forClass(RegisteredClient.class);
        verify(clients).save(saved.capture());
        assertThat(saved.getValue().getId()).isEqualTo(A);
        assertThat(saved.getValue().getClientSecret()).isEqualTo(KEPT_SECRET);
        assertThat(result.id()).isEqualTo(A);
        assertThat(result.clientName()).isEqualTo(RENAMED);
    }

    @Test
    void updateOfAMissingClientIsNotFound() {
        when(clients.findById(MISSING)).thenReturn(null);

        assertThatThrownBy(() -> service.update(MISSING, details(MISSING, "x")))
                .isInstanceOf(ClientNotFoundException.class);
    }

    @Test
    void createGeneratesAnIdAndAnswersTheSecretOnce()
            throws ClientIdTakenException, InvalidRedirectUriException, ClientTokenTtlExceedsPolicyException {
        when(encoder.encode(anyString())).thenReturn(GENERATED_SECRET);

        CreatedClient result = service.create(details(IGNORED, NEW));

        ArgumentCaptor<RegisteredClient> saved = ArgumentCaptor.forClass(RegisteredClient.class);
        verify(clients).save(saved.capture());
        assertThat(saved.getValue().getId()).isNotEqualTo(IGNORED);
        assertThat(saved.getValue().getClientSecret()).isEqualTo(GENERATED_SECRET);
        assertThat(saved.getValue().getClientSecretExpiresAt()).isEqualTo(NOW.plus(VALIDITY_DAYS, java.time.temporal.ChronoUnit.DAYS));
        assertThat(result.client().id()).isEqualTo(saved.getValue().getId());
        assertThat(result.clientSecret()).isNotBlank().isNotEqualTo(GENERATED_SECRET);
        assertThat(result.client().status().type()).isEqualTo(ClientType.MACHINE);
        verify(extensions).save(any());
    }

    @Test
    void aPublicClientGetsNoSecret()
            throws ClientIdTakenException, InvalidRedirectUriException, ClientTokenTtlExceedsPolicyException {
        CreatedClient result = service.create(withRedirect(CALLBACK));

        assertThat(result.clientSecret()).isNull();
        assertThat(result.client().status().type()).isEqualTo(ClientType.PUBLIC);
        verify(encoder, never()).encode(anyString());
    }

    @Test
    void aTakenClientIdIsAConflict() {
        when(clients.findByClientId(CLIENT_A)).thenReturn(existing(A));

        assertThatThrownBy(() -> service.create(details(IGNORED, NEW))).isInstanceOf(ClientIdTakenException.class);
    }

    @Test
    void aWildcardRedirectIsRefused() {
        assertThatThrownBy(() -> service.create(withRedirect("https://*.example/cb")))
                .isInstanceOf(InvalidRedirectUriException.class);
    }

    @Test
    void anAccessTokenLifetimeOverTheCeilingIsRefused() {
        ClientDetailsTokens tokens = new ClientDetailsTokens(null, java.time.Duration.ofHours(2), null, null, false, null,
                null, false, null);
        ClientDetails details = new ClientDetails(IGNORED, "client-c", "C", Set.of(ClientAuthMethod.CLIENT_SECRET_BASIC),
                Set.of(OAuthGrantType.CLIENT_CREDENTIALS), Set.of(), Set.of(), Set.of(SCOPE), null, tokens, null);

        assertThatThrownBy(() -> service.create(details)).isInstanceOf(ClientTokenTtlExceedsPolicyException.class);
    }

    @Test
    void rotationRetiresTheOldHashIntoTheGraceWindow() throws ClientNotFoundException, ClientNotConfidentialException {
        when(clients.findById(A)).thenReturn(existing(A));
        when(encoder.encode(anyString())).thenReturn(GENERATED_SECRET);

        RotatedSecret rotated = service.rotateSecret(A);

        ArgumentCaptor<ClientSecretHistory> retired = ArgumentCaptor.forClass(ClientSecretHistory.class);
        verify(history).save(retired.capture());
        assertThat(retired.getValue().getSecretHash()).isEqualTo(KEPT_SECRET);
        assertThat(retired.getValue().getExpiresAt()).isEqualTo(NOW.plus(GRACE_HOURS, java.time.temporal.ChronoUnit.HOURS));
        ArgumentCaptor<RegisteredClient> saved = ArgumentCaptor.forClass(RegisteredClient.class);
        verify(clients).save(saved.capture());
        assertThat(saved.getValue().getClientSecret()).isEqualTo(GENERATED_SECRET);
        assertThat(rotated.clientSecret()).isNotBlank();
        assertThat(rotated.previousSecretUntil()).isEqualTo(retired.getValue().getExpiresAt());
    }

    @Test
    void aPublicClientHasNoSecretToRotate() {
        RegisteredClient publicClient = RegisteredClient.withId(A).clientId(CLIENT_A).clientName(A)
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE).redirectUri(CALLBACK)
                .build();
        when(clients.findById(A)).thenReturn(publicClient);

        assertThatThrownBy(() -> service.rotateSecret(A)).isInstanceOf(ClientNotConfidentialException.class);
    }

    @Test
    void findByIdAnswersTheRegistrationWithItsStatusAttached() throws ClientNotFoundException {
        when(clients.findById(A)).thenReturn(existing(A));

        ClientDetails found = service.findById(A);

        assertThat(found.id()).isEqualTo(A);
        assertThat(found.status().type()).isEqualTo(ClientType.MACHINE);
    }

    @Test
    void findByIdOfAmissingClientIsNotFound() {
        when(clients.findById(MISSING)).thenReturn(null);

        assertThatThrownBy(() -> service.findById(MISSING)).isInstanceOf(ClientNotFoundException.class);
    }

    @Test
    void thegraceWindowShownIsTheLatestLiveOneAndExpiredRowsAreIgnored() throws ClientNotFoundException {
        when(clients.findById(A)).thenReturn(existing(A));
        when(history.findByRegisteredClientIdAndRevokedAtIsNull(A)).thenReturn(List.of(
                ClientSecretHistory.retire(A, KEPT_SECRET, NOW, NOW.minusSeconds(60)),
                ClientSecretHistory.retire(A, KEPT_SECRET, NOW, NOW.plusSeconds(600)),
                ClientSecretHistory.retire(A, KEPT_SECRET, NOW, NOW.plusSeconds(60))));

        // An expired row still has no revokedAt, so filtering on live() rather than on the column is what matters.
        assertThat(service.findById(A).status().previousSecretUntil()).isEqualTo(NOW.plusSeconds(600));
    }

    @Test
    void aclientIdAlreadyTakenByAdisabledClientIsStillRefused() {
        // A disabled client is invisible to findByClientId, so the uniqueness check has to read the table.
        when(jdbc.queryForObject(anyString(), org.mockito.ArgumentMatchers.eq(Integer.class), anyString()))
                .thenReturn(1);

        assertThatThrownBy(() -> service.create(details(IGNORED, NEW))).isInstanceOf(ClientIdTakenException.class);
    }

    @Test
    void acountThatComesBackNullIsTreatedAsNoSuchClientRatherThanThrowing()
            throws ClientIdTakenException, InvalidRedirectUriException, ClientTokenTtlExceedsPolicyException {
        when(jdbc.queryForObject(anyString(), org.mockito.ArgumentMatchers.eq(Integer.class), anyString()))
                .thenReturn(null);

        assertThat(service.create(details(IGNORED, NEW))).isNotNull();
    }

    @Test
    void adescriptionSentWithTheStatusIsTheOnePartOfItTheServerReadsBack()
            throws ClientIdTakenException, InvalidRedirectUriException, ClientTokenTtlExceedsPolicyException {
        ClientDetails withDescription = details(IGNORED, NEW).withStatus(new com.asrevo.cvhome.sso.dto.ClientStatus(
                THE_CONSOLE, true, ClientType.MACHINE, null, null, null, null, null, null));

        service.create(withDescription);

        ArgumentCaptor<com.asrevo.cvhome.sso.domain.ClientExtension> saved =
                ArgumentCaptor.forClass(com.asrevo.cvhome.sso.domain.ClientExtension.class);
        verify(extensions).save(saved.capture());
        assertThat(saved.getValue().getDescription()).isEqualTo(THE_CONSOLE);
    }

    @Test
    void revokingThePreviousSecretEndsTheGraceWindowNow() throws Exception {
        ClientSecretHistory live = ClientSecretHistory.retire(A, KEPT_SECRET, NOW, NOW.plusSeconds(600));
        when(clients.findById(A)).thenReturn(existing(A));
        when(history.findByRegisteredClientIdAndRevokedAtIsNull(A)).thenReturn(List.of(live));

        service.revokePreviousSecret(A);

        assertThat(live.getRevokedAt()).isEqualTo(NOW);
        verify(history).save(live);
    }

    @Test
    void revokingApreviousSecretThatIsAlreadyGoneIsRefusedRatherThanAnsweredAsDone() {
        when(clients.findById(A)).thenReturn(existing(A));
        when(history.findByRegisteredClientIdAndRevokedAtIsNull(A))
                .thenReturn(List.of(ClientSecretHistory.retire(A, KEPT_SECRET, NOW, NOW.minusSeconds(1))));

        assertThatThrownBy(() -> service.revokePreviousSecret(A))
                .isInstanceOf(com.asrevo.cvhome.uaa.errors.ClientNoPreviousSecretException.class);
    }

    @Test
    void revokingThePreviousSecretOfAmissingClientIsNotFound() {
        when(clients.findById(MISSING)).thenReturn(null);

        assertThatThrownBy(() -> service.revokePreviousSecret(MISSING))
                .isInstanceOf(ClientNotFoundException.class);
    }

    @Test
    void resettingAsecretWritesTheOperatorsChoiceWithNoGraceWindow() throws Exception {
        ClientSecretHistory live = ClientSecretHistory.retire(A, KEPT_SECRET, NOW, NOW.plusSeconds(600));
        when(clients.findById(A)).thenReturn(existing(A));
        when(history.findByRegisteredClientIdAndRevokedAtIsNull(A)).thenReturn(List.of(live));
        when(encoder.encode(CHOSEN)).thenReturn(GENERATED_SECRET);

        service.resetSecret(A, CHOSEN);

        ArgumentCaptor<RegisteredClient> saved = ArgumentCaptor.forClass(RegisteredClient.class);
        verify(clients).save(saved.capture());
        assertThat(saved.getValue().getClientSecret()).isEqualTo(GENERATED_SECRET);
        // No grace window at all: the previous secret stops working the moment this returns.
        assertThat(live.getRevokedAt()).isEqualTo(NOW);
    }

    @Test
    void resettingTheSecretOfApublicClientIsRefused() {
        when(clients.findById(A)).thenReturn(RegisteredClient.withId(A).clientId(CLIENT_A).clientName(A)
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(CALLBACK).scope(OPENID).build());

        assertThatThrownBy(() -> service.resetSecret(A, CHOSEN))
                .isInstanceOf(ClientNotConfidentialException.class);
    }

    @Test
    void resettingTheSecretOfAmissingClientIsNotFoundRatherThanAsilentSuccess() {
        when(clients.findById(MISSING)).thenReturn(null);

        // The previous `if (client != null)` answered 200 without rotating anything.
        assertThatThrownBy(() -> service.resetSecret(MISSING, CHOSEN))
                .isInstanceOf(ClientNotFoundException.class);
    }

    @Test
    void rotatingEverythingSkipsTheClientsThatHoldNoSecret() throws Exception {
        when(jdbc.query(anyString(), org.mockito.ArgumentMatchers.<org.springframework.jdbc.core.RowMapper<
                com.asrevo.cvhome.sso.dto.ClientSummary>>any())).thenReturn(List.of(
                        new com.asrevo.cvhome.sso.dto.ClientSummary(A, CLIENT_A, A, ClientType.MACHINE, true,
                                Set.of("client_credentials"), null, null),
                        new com.asrevo.cvhome.sso.dto.ClientSummary(B, CLIENT_B, B, ClientType.PUBLIC, true,
                                Set.of("authorization_code"), null, null)));
        when(clients.findById(A)).thenReturn(existing(A));
        when(encoder.encode(anyString())).thenReturn(GENERATED_SECRET);

        List<RotatedSecret> rotated = service.rotateAll();

        // A public client has no secret to rotate; asking would throw and abort the whole incident response.
        assertThat(rotated).extracting(RotatedSecret::id).containsExactly(A);
    }

}
