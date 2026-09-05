package com.asrevo.cvhome.sso.dashboard;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.asrevo.cvhome.sso.audit.AuditQueryService;
import com.asrevo.cvhome.sso.audit.AuditSearch;
import com.asrevo.cvhome.sso.domain.SigningKey;
import com.asrevo.cvhome.sso.domain.SigningKeyStatus;
import com.asrevo.cvhome.sso.dto.AuditEventDto;
import com.asrevo.cvhome.sso.dto.Dashboard;
import com.asrevo.cvhome.sso.dto.UserCounts;
import com.asrevo.cvhome.sso.repo.IdentityProviderRepository;
import com.asrevo.cvhome.sso.repo.RoleRepository;
import com.asrevo.cvhome.sso.repo.SigningKeyRepository;
import com.asrevo.cvhome.sso.service.AdminService;
import com.asrevo.cvhome.sso.settings.RealmSettings;
import com.asrevo.cvhome.sso.settings.SettingsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The overview panel: the chart, the rail badges and the posture lines.
 *
 * <p>
 * The posture lines are the part worth pinning. Each one is supposed to be <em>computed</em> from the realm's own
 * data rather than declared, so a panel can never claim a state the realm is not in — an active signing key older
 * than the rotation interval must read {@code WARN}, and no active key at all must read {@code RISK} rather than
 * quietly reading {@code OK} because a count came back zero.
 * </p>
 *
 * <p>
 * {@link JdbcTemplate} is subclassed rather than mocked: the service reaches it through three different overloads
 * (two of them varargs), and a fake that dispatches on the SQL keeps the stubbing readable and keeps a changed
 * overload from silently matching the wrong stub.
 * </p>
 */
class DashboardServiceTest {

    private static final String UNKNOWN_RANGE = "eternity";
    private static final String KEY_AGE = "keyAge";
    private static final String OK = "OK";
    private static final String WARN = "WARN";
    private static final String NOW = "2026-03-01T12:00:00Z";
    private static final String USERS_NO_PASSWORD = "usersWithoutPassword";
    private static final String CLIENTS_NO_PKCE = "clientsWithoutPkce";
    private static final String SECRETS_EXPIRING = "secretsExpiring";
    private static final String BREACHED_CHECK = "breachedPasswordCheck";
    private static final String FAILED_SIGN_INS = "failedSignIns";
    private static final String CONSOLE = "console";
    private static final String STOREFRONT = "storefront";

    private final AuditQueryService auditQuery = mock(AuditQueryService.class);
    private final AdminService users = mock(AdminService.class);
    private final RoleRepository roles = mock(RoleRepository.class);
    private final IdentityProviderRepository providers = mock(IdentityProviderRepository.class);
    private final SigningKeyRepository keys = mock(SigningKeyRepository.class);
    private final SettingsService settings = mock(SettingsService.class);
    private final Clock clock = Clock.fixed(Instant.parse(NOW), ZoneOffset.UTC);

    private FakeJdbc jdbc;
    private DashboardService service;

    @BeforeEach
    void setUp() throws Exception {
        jdbc = new FakeJdbc();
        service = new DashboardService(jdbc, auditQuery, users, roles, providers, keys, settings, clock);
        when(users.counts()).thenReturn(new UserCounts(12, 9, 1, 1, 1));
        when(roles.count()).thenReturn(4L);
        when(providers.count()).thenReturn(2L);
        when(keys.findByStatus(SigningKeyStatus.ACTIVE)).thenReturn(List.of(keyActivated(Duration.ofDays(3))));
        when(settings.current()).thenReturn(realmSettings(30, true, 5));
        when(auditQuery.search(any(AuditSearch.class), any(Pageable.class))).thenReturn(emptyPage());
    }

    @Test
    void countsSignInsAndFailuresFromTheChartBucketsRatherThanASeparateQuery() throws Exception {
        jdbc.buckets = List.of(bucketRow(Instant.parse(NOW), 7, 2), bucketRow(Instant.parse(NOW), 3, 1));

        Dashboard dashboard = service.of(DashboardService.RANGE_24H);

        assertThat(dashboard.signIns()).hasSize(2);
        assertThat(dashboard.signInsTotal()).isEqualTo(10);
        assertThat(dashboard.signInFailures()).isEqualTo(3);
    }

    @Test
    void readsTheRailBadgesAndTheTopClientsFromTheirOwnSources() throws Exception {
        jdbc.topClients = List.of(new Object[] {CONSOLE, 40L}, new Object[] {STOREFRONT, 9L});
        jdbc.clientCount = 6L;

        Dashboard dashboard = service.of(DashboardService.RANGE_7D);

        assertThat(dashboard.counts()).isEqualTo(new Dashboard.RailCounts(12, 4, 6, 2));
        assertThat(dashboard.topClients()).extracting(Dashboard.Ranked::label).containsExactly(CONSOLE, STOREFRONT);
        assertThat(dashboard.users().active()).isEqualTo(9);
    }

    @ParameterizedTest(name = "{0} spans {1} days and buckets by {2}")
    @CsvSource({"24h,1,hour", "7d,7,day", "30d,30,day"})
    void eachRangeSpansItsOwnWindowAndBucketsAtItsOwnResolution(String range, long days, String truncation)
            throws Exception {
        Dashboard dashboard = service.of(range);

        assertThat(Duration.between(dashboard.from(), dashboard.to())).isEqualTo(Duration.ofDays(days));
        assertThat(jdbc.bucketTruncation).isEqualTo(truncation);
    }

    @Test
    void anUnknownRangeFallsBackToTwentyFourHoursRatherThanFailing() throws Exception {
        Dashboard dashboard = service.of(UNKNOWN_RANGE);

        assertThat(dashboard.range()).isEqualTo(UNKNOWN_RANGE);
        assertThat(Duration.between(dashboard.from(), dashboard.to())).isEqualTo(Duration.ofHours(24));
    }

    @Test
    void anActiveKeyYoungerThanTheRotationIntervalReadsOkAndReportsItsAgeInDays() throws Exception {
        when(keys.findByStatus(SigningKeyStatus.ACTIVE)).thenReturn(List.of(keyActivated(Duration.ofDays(10))));

        assertThat(check(service.of(DashboardService.RANGE_24H), KEY_AGE))
                .isEqualTo(new Dashboard.PostureCheck(KEY_AGE, OK, "10"));
    }

    @Test
    void anActiveKeyOlderThanTheRotationIntervalReadsWarn() throws Exception {
        when(keys.findByStatus(SigningKeyStatus.ACTIVE)).thenReturn(List.of(keyActivated(Duration.ofDays(45))));

        assertThat(check(service.of(DashboardService.RANGE_24H), KEY_AGE))
                .isEqualTo(new Dashboard.PostureCheck(KEY_AGE, WARN, "45"));
    }

    @Test
    void rotationDisabledMeansNoKeyIsEverOverdue() throws Exception {
        when(settings.current()).thenReturn(realmSettings(0, true, 5));
        when(keys.findByStatus(SigningKeyStatus.ACTIVE)).thenReturn(List.of(keyActivated(Duration.ofDays(900))));

        assertThat(check(service.of(DashboardService.RANGE_24H), KEY_AGE).level()).isEqualTo(OK);
    }

    @Test
    void noActiveSigningKeyAtAllIsARiskRatherThanASilentOk() throws Exception {
        when(keys.findByStatus(SigningKeyStatus.ACTIVE)).thenReturn(List.of());

        assertThat(check(service.of(DashboardService.RANGE_24H), KEY_AGE))
                .isEqualTo(new Dashboard.PostureCheck(KEY_AGE, "RISK", "0"));
    }

    @Test
    void aQuietRealmReportsEveryPostureLineAsOk() throws Exception {
        Dashboard dashboard = service.of(DashboardService.RANGE_24H);

        assertThat(dashboard.posture()).extracting(Dashboard.PostureCheck::level).containsOnly(OK);
        assertThat(dashboard.posture()).extracting(Dashboard.PostureCheck::id)
                .containsExactly(KEY_AGE, USERS_NO_PASSWORD, CLIENTS_NO_PKCE, SECRETS_EXPIRING,
                        BREACHED_CHECK, FAILED_SIGN_INS);
    }

    @Test
    void accountsWithoutAPasswordAndClientsWithoutPkceEachRaiseTheirOwnWarning() throws Exception {
        jdbc.usersWithoutPassword = 3L;
        jdbc.clientsWithoutPkce = 1L;

        Dashboard dashboard = service.of(DashboardService.RANGE_24H);

        assertThat(check(dashboard, USERS_NO_PASSWORD)).isEqualTo(
                new Dashboard.PostureCheck(USERS_NO_PASSWORD, WARN, "3"));
        assertThat(check(dashboard, CLIENTS_NO_PKCE)).isEqualTo(
                new Dashboard.PostureCheck(CLIENTS_NO_PKCE, WARN, "1"));
    }

    @Test
    void aSecretExpiringWithinThirtyDaysWarnsAndTheQueryLooksThatFarAhead() throws Exception {
        jdbc.secretsExpiring = 2L;

        Dashboard dashboard = service.of(DashboardService.RANGE_24H);

        assertThat(check(dashboard, SECRETS_EXPIRING).level()).isEqualTo(WARN);
        assertThat(jdbc.secretsExpiringHorizon)
                .isEqualTo(Timestamp.from(Instant.parse(NOW).plus(Duration.ofDays(30))));
    }

    @Test
    void aRealmThatDoesNotRejectBreachedPasswordsIsFlagged() throws Exception {
        when(settings.current()).thenReturn(realmSettings(30, false, 5));

        assertThat(check(service.of(DashboardService.RANGE_24H), BREACHED_CHECK).level()).isEqualTo(WARN);
    }

    @Test
    void failedSignInsAboveTheLockoutThresholdAreFlaggedAgainstThatThreshold() throws Exception {
        jdbc.buckets = List.<Object[]>of(bucketRow(Instant.parse(NOW), 1, 6));

        assertThat(check(service.of(DashboardService.RANGE_24H), FAILED_SIGN_INS))
                .isEqualTo(new Dashboard.PostureCheck(FAILED_SIGN_INS, WARN, "6"));
    }

    @Test
    void failedSignInsAtTheThresholdAreNotYetAWarning() throws Exception {
        jdbc.buckets = List.<Object[]>of(bucketRow(Instant.parse(NOW), 1, 5));

        assertThat(check(service.of(DashboardService.RANGE_24H), FAILED_SIGN_INS).level()).isEqualTo(OK);
    }

    @Test
    void aNullCountFromTheDatabaseReadsAsZeroRatherThanThrowing() throws Exception {
        jdbc.nullCounts = true;

        Dashboard dashboard = service.of(DashboardService.RANGE_24H);

        assertThat(dashboard.tokensIssued()).isZero();
        assertThat(dashboard.activeSessions()).isZero();
        assertThat(dashboard.counts().clients()).isZero();
    }

    @Test
    void activeSessionsAreCountedAgainstNowAndTokensSinceTheWindowStarted() throws Exception {
        jdbc.tokensIssued = 88L;
        jdbc.activeSessions = 5L;

        Dashboard dashboard = service.of(DashboardService.RANGE_24H);

        assertThat(dashboard.tokensIssued()).isEqualTo(88);
        assertThat(dashboard.activeSessions()).isEqualTo(5);
        assertThat(jdbc.sessionExpiryFloor).isEqualTo(Instant.parse(NOW).toEpochMilli());
    }

    @Test
    void recentFailuresComeFromTheAuditLogRatherThanTheChart() throws Exception {
        when(auditQuery.search(any(AuditSearch.class), any(Pageable.class))).thenReturn(emptyPage());

        assertThat(service.of(DashboardService.RANGE_30D).recentFailures()).isEmpty();
    }

    private static Dashboard.PostureCheck check(Dashboard dashboard, String id) {
        return dashboard.posture().stream().filter(c -> c.id().equals(id)).findFirst().orElseThrow();
    }

    private SigningKey keyActivated(Duration ago) {
        SigningKey key = new SigningKey();
        key.setStatus(SigningKeyStatus.ACTIVE);
        key.setActivatedAt(Instant.parse(NOW).minus(ago));
        return key;
    }

    private static Object[] bucketRow(Instant at, long ok, long bad) {
        return new Object[] {Timestamp.from(at), ok, bad};
    }

    private static Page<AuditEventDto> emptyPage() {
        return new PageImpl<>(List.of());
    }

    private static RealmSettings realmSettings(int rotationDays, boolean rejectBreached, int lockoutThreshold) {
        return new RealmSettings("Realm", "ops@example.com", "en", false, true,
                new RealmSettings.PasswordPolicy(12, true, true, true, false, 3, 0, rejectBreached),
                new RealmSettings.Lockout(lockoutThreshold, 900, 0),
                new RealmSettings.Sessions(1800, 28800, false, 0, false),
                new RealmSettings.Tokens(3600, 300, 86400, 365, 24),
                new RealmSettings.Keys(rotationDays, 7), 90, Instant.parse(NOW), "ops", 1L);
    }

    /**
     * A {@link JdbcTemplate} whose overloads dispatch on the SQL text, so each query the service issues can be
     * answered — and inspected — independently.
     */
    private static final class FakeJdbc extends JdbcTemplate {

        private List<Object[]> buckets = new ArrayList<>();
        private List<Object[]> topClients = new ArrayList<>();
        private long tokensIssued;
        private long activeSessions;
        private long clientCount;
        private long usersWithoutPassword;
        private long clientsWithoutPkce;
        private long secretsExpiring;
        private boolean nullCounts;
        private String bucketTruncation;
        private Object secretsExpiringHorizon;
        private Object sessionExpiryFloor;

        @Override
        @SuppressWarnings("unchecked")
        public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
            List<Object[]> rows;
            if (sql.contains("date_trunc")) {
                bucketTruncation = (String) args[0];
                rows = buckets;
            } else {
                rows = topClients;
            }
            List<T> mapped = new ArrayList<>();
            for (int i = 0; i < rows.size(); i++) {
                mapped.add((T) map(rowMapper, rows.get(i), i));
            }
            return mapped;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T queryForObject(String sql, Class<T> requiredType) {
            return (T) (Long) (nullCounts ? null : clientCount);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T queryForObject(String sql, Class<T> requiredType, Object... args) {
            if (nullCounts) {
                return null;
            }
            return (T) (Long) answer(sql, args);
        }

        private long answer(String sql, Object... args) {
            if (sql.contains("spring_session")) {
                sessionExpiryFloor = args[0];
                return activeSessions;
            }
            if (sql.contains("client_secret_expires_at")) {
                secretsExpiringHorizon = args[0];
                return secretsExpiring;
            }
            if (sql.contains("require-proof-key")) {
                return clientsWithoutPkce;
            }
            if (sql.contains("password_hash is null")) {
                return usersWithoutPassword;
            }
            return tokensIssued;
        }

        private static <T> T map(RowMapper<T> rowMapper, Object[] row, int index) {
            try {
                return rowMapper.mapRow(resultSet(row), index);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        }

        private static ResultSet resultSet(Object[] row) throws SQLException {
            Map<Integer, Object> byColumn = new HashMap<>();
            for (int i = 0; i < row.length; i++) {
                byColumn.put(i + 1, row[i]);
            }
            ResultSet rs = mock(ResultSet.class);
            if (byColumn.get(1) instanceof Timestamp at) {
                when(rs.getTimestamp(1)).thenReturn(at);
            } else {
                when(rs.getString(1)).thenReturn(String.valueOf(byColumn.get(1)));
            }
            for (int column = 2; column <= row.length; column++) {
                when(rs.getLong(column)).thenReturn((Long) byColumn.get(column));
            }
            return rs;
        }

    }

}
