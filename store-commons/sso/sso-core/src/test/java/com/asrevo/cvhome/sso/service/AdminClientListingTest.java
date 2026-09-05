package com.asrevo.cvhome.sso.service;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.client.ClientType;
import com.asrevo.cvhome.sso.client.ClientsProperties;
import com.asrevo.cvhome.sso.client.RedirectUriRules;
import com.asrevo.cvhome.sso.domain.ClientExtension;
import com.asrevo.cvhome.sso.dto.ClientSearch;
import com.asrevo.cvhome.sso.dto.ClientSummary;
import com.asrevo.cvhome.sso.repo.ClientExtensionRepository;
import com.asrevo.cvhome.sso.repo.ClientSecretHistoryRepository;
import com.asrevo.cvhome.sso.settings.SettingsService;
import com.asrevo.cvhome.sso.token.TokenRevocationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The client listing, its filters and its counts — the half of AdminClientService that reads through JDBC.
 *
 * <p>
 * The listing is paged in memory over a full read, which is fine at the scale one realm's clients reach and is why
 * the offset arithmetic has to be clamped: a page past the end must answer empty rather than throw out of
 * {@code subList}. The filters are combined, and a blank query means "no query" rather than a search for the empty
 * string, which would match everything and look like the filter working.
 * </p>
 *
 * <p>
 * The row mapper is exercised directly against a stub {@link ResultSet}, because that is where a client's type is
 * derived from its authentication methods and grants — the difference between a public app, a machine client and a
 * confidential one, which is what decides whether it may hold a secret at all.
 * </p>
 */
class AdminClientListingTest {

    private static final Instant NOW = Instant.parse("2026-09-02T12:00:00Z");
    private static final String CLIENT_ID = "store-core@service.store-core.internal";
    private static final String NAME = "Store core";
    private static final String ROW_ID = "id-1";
    private static final String A = "a";
    private static final String B = "b";
    private static final String C = "c";
    private static final String BLANK = "   ";
    private static final String SECRET_BASIC = "client_secret_basic";
    private static final String CLIENT_CREDENTIALS = "client_credentials";
    private static final String MISSING = "missing";

    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final RegisteredClientRepository clients = mock(RegisteredClientRepository.class);
    private final ClientExtensionRepository extensions = mock(ClientExtensionRepository.class);
    private final TokenRevocationService revocation = mock(TokenRevocationService.class);
    private final AuditService audit = mock(AuditService.class);

    private final AdminClientService service = new AdminClientService(clients, mock(PasswordEncoder.class), jdbc,
            audit, extensions, mock(ClientSecretHistoryRepository.class), mock(SettingsService.class),
            new RedirectUriRules(new ClientsProperties(List.of("localhost"))), revocation,
            Clock.fixed(NOW, ZoneOffset.UTC));

    private static ClientSummary summary(String id, String clientId, ClientType type, boolean enabled,
                                         Instant secretExpiry) {
        return new ClientSummary(id, clientId, NAME, type, enabled, java.util.Set.of(), secretExpiry, null);
    }

    @SuppressWarnings("unchecked")
    private void rowsAre(ClientSummary... rows) {
        when(jdbc.query(anyString(), any(RowMapper.class))).thenReturn(List.of(rows));
    }

    @BeforeEach
    void setUp() {
        rowsAre();
    }

    @Test
    void aPagePastTheEndIsEmptyRatherThanAnIndexError() {
        rowsAre(summary(ROW_ID, CLIENT_ID, ClientType.MACHINE, true, null));

        assertThat(service.listClients(ClientSearch.none(), PageRequest.of(5, 20)).getContent()).isEmpty();
        assertThat(service.listClients(ClientSearch.none(), PageRequest.of(0, 20)).getTotalElements()).isOne();
    }

    @Test
    void aPartialLastPageIsClampedToWhatIsThere() {
        rowsAre(summary(A, A, ClientType.MACHINE, true, null),
                summary(B, B, ClientType.PUBLIC, true, null),
                summary(C, C, ClientType.CONFIDENTIAL, true, null));

        assertThat(service.listClients(ClientSearch.none(), PageRequest.of(1, 2)).getContent()).hasSize(1);
    }

    @Test
    void ablankQueryIsNoQueryRatherThanASearchForTheEmptyString() {
        rowsAre(summary(ROW_ID, CLIENT_ID, ClientType.MACHINE, true, null));

        assertThat(service.listClients(new ClientSearch(BLANK, null, null), PageRequest.of(0, 20)).getContent())
                .hasSize(1);
        assertThat(service.listClients(new ClientSearch(null, null, null), PageRequest.of(0, 20)).getContent())
                .hasSize(1);
    }

    @Test
    void aQueryMatchesEitherTheClientIdOrItsName() {
        rowsAre(summary(ROW_ID, CLIENT_ID, ClientType.MACHINE, true, null));

        assertThat(service.listClients(new ClientSearch("STORE-CORE", null, null), PageRequest.of(0, 20))
                .getContent()).hasSize(1);
        assertThat(service.listClients(new ClientSearch("store core", null, null), PageRequest.of(0, 20))
                .getContent()).hasSize(1);
        assertThat(service.listClients(new ClientSearch("nothing", null, null), PageRequest.of(0, 20))
                .getContent()).isEmpty();
    }

    @Test
    void theEnabledAndTypeFiltersNarrowIndependentlyAndTogether() {
        rowsAre(summary(A, A, ClientType.MACHINE, true, null),
                summary(B, B, ClientType.PUBLIC, false, null));

        assertThat(service.listClients(new ClientSearch(null, true, null), PageRequest.of(0, 20)).getContent())
                .hasSize(1);
        assertThat(service.listClients(new ClientSearch(null, null, ClientType.PUBLIC), PageRequest.of(0, 20))
                .getContent()).hasSize(1);
        assertThat(service.listClients(new ClientSearch(null, true, ClientType.PUBLIC), PageRequest.of(0, 20))
                .getContent()).isEmpty();
    }

    @Test
    void theStatsCountEachTypeAndFlagSecretsExpiringSoon() {
        rowsAre(summary(A, A, ClientType.MACHINE, true, NOW.plusSeconds(3600)),
                summary(B, B, ClientType.PUBLIC, false, null),
                summary(C, C, ClientType.CONFIDENTIAL, true, NOW.plusSeconds(60L * 60 * 24 * 365)));

        var stats = service.stats();

        assertThat(stats.total()).isEqualTo(3);
        assertThat(stats.enabled()).isEqualTo(2);
        assertThat(stats.disabled()).isOne();
        assertThat(stats.machine()).isOne();
        assertThat(stats.publicClients()).isOne();
        assertThat(stats.confidential()).isOne();
        // Only the one inside the window; a far-future expiry and a null one are not "soon".
        assertThat(stats.secretsExpiringSoon()).isOne();
    }

    @SuppressWarnings("unchecked")
    private RowMapper<ClientSummary> capturedRowMapper() {
        service.stats();
        ArgumentCaptor<RowMapper<ClientSummary>> captor = ArgumentCaptor.forClass(RowMapper.class);
        verify(jdbc).query(anyString(), captor.capture());
        return captor.getValue();
    }

    @Test
    void theRowMapperDerivesAClientsTypeFromItsMethodsAndGrants() throws Exception {
        ResultSet row = mock(ResultSet.class);
        when(row.getString(1)).thenReturn(ROW_ID);
        when(row.getString(2)).thenReturn(CLIENT_ID);
        when(row.getString(3)).thenReturn(NAME);
        when(row.getString(4)).thenReturn(SECRET_BASIC);
        when(row.getString(5)).thenReturn(CLIENT_CREDENTIALS);
        when(row.getBoolean(7)).thenReturn(true);
        when(row.getTimestamp(6)).thenReturn(Timestamp.from(NOW));
        when(row.getTimestamp(8)).thenReturn(null);

        ClientSummary mapped = capturedRowMapper().mapRow(row, 0);

        // A secret plus client_credentials and nothing else is a machine client.
        assertThat(mapped.type()).isEqualTo(ClientType.MACHINE);
        assertThat(mapped.grantTypes()).containsExactly(CLIENT_CREDENTIALS);
        assertThat(mapped.clientSecretExpiresAt()).isEqualTo(NOW);
        assertThat(mapped.lastTokenIssuedAt()).isNull();
    }

    @Test
    void theRowMapperTreatsAnAbsentOrBlankCsvAsNoValuesAtAll() throws Exception {
        ResultSet row = mock(ResultSet.class);
        when(row.getString(4)).thenReturn(null);
        when(row.getString(5)).thenReturn(BLANK);

        ClientSummary mapped = capturedRowMapper().mapRow(row, 0);

        assertThat(mapped.grantTypes()).isEmpty();
    }

    @Test
    void theRowMapperTrimsAndDropsEmptyEntriesInACsv() throws Exception {
        ResultSet row = mock(ResultSet.class);
        when(row.getString(4)).thenReturn(SECRET_BASIC);
        when(row.getString(5)).thenReturn(" authorization_code , , refresh_token ");

        ClientSummary mapped = capturedRowMapper().mapRow(row, 0);

        assertThat(mapped.grantTypes()).containsExactlyInAnyOrder("authorization_code", "refresh_token");
    }

    @Test
    void deletingAnUnknownClientAnswersFalseWithoutRevokingAnything() {
        when(clients.findById(MISSING)).thenReturn(null);

        assertThat(service.delete(MISSING)).isFalse();
        Mockito.verifyNoInteractions(revocation);
    }

    @Test
    void deletingAKnownClientRevokesItsAuthorizationsFirst() {
        RegisteredClient client = RegisteredClient.withId(ROW_ID).clientId(CLIENT_ID)
                .authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType
                        .CLIENT_CREDENTIALS)
                .build();
        when(clients.findById(ROW_ID)).thenReturn(client);
        when(jdbc.update(anyString(), eq(ROW_ID))).thenReturn(1);

        assertThat(service.delete(ROW_ID)).isTrue();

        // Revoked before the row goes: afterwards there is no client id to revoke against.
        verify(revocation).revokeAllForClient(ROW_ID, CLIENT_ID);
        verify(audit).record(any());
    }

    @Test
    void aDeleteThatChangedNoRowsIsNotRecordedAsOne() {
        RegisteredClient client = RegisteredClient.withId(ROW_ID).clientId(CLIENT_ID)
                .authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType
                        .CLIENT_CREDENTIALS)
                .build();
        when(clients.findById(ROW_ID)).thenReturn(client);
        when(jdbc.update(anyString(), eq(ROW_ID))).thenReturn(0);

        assertThat(service.delete(ROW_ID)).isFalse();
        Mockito.verifyNoInteractions(audit);
    }

    @Test
    void disablingAClientRevokesEveryAuthorizationAndSaysHowMany() throws Exception {
        RegisteredClient client = RegisteredClient.withId(ROW_ID).clientId(CLIENT_ID)
                .authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType
                        .CLIENT_CREDENTIALS)
                .build();
        when(clients.findById(ROW_ID)).thenReturn(client);
        when(extensions.findById(ROW_ID)).thenReturn(java.util.Optional.empty());
        when(revocation.revokeAllForClient(ROW_ID, CLIENT_ID)).thenReturn(3);

        service.disable(ROW_ID, "ops@example.com");

        // A disabled client whose tokens still work is not disabled.
        verify(revocation).revokeAllForClient(ROW_ID, CLIENT_ID);
        verify(extensions).save(any(ClientExtension.class));
    }

    @Test
    void enablingAClientTouchesNoAuthorizations() throws Exception {
        RegisteredClient client = RegisteredClient.withId(ROW_ID).clientId(CLIENT_ID)
                .authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType
                        .CLIENT_CREDENTIALS)
                .build();
        when(clients.findById(ROW_ID)).thenReturn(client);
        when(extensions.findById(ROW_ID)).thenReturn(java.util.Optional.empty());

        service.enable(ROW_ID);

        verify(extensions).save(any(ClientExtension.class));
        Mockito.verifyNoInteractions(revocation);
    }
}
