package com.asrevo.cvhome.uaa.dashboard;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.uaa.audit.AuditEventType;
import com.asrevo.cvhome.uaa.audit.AuditOutcome;
import com.asrevo.cvhome.uaa.audit.AuditQueryService;
import com.asrevo.cvhome.uaa.audit.AuditSearch;
import com.asrevo.cvhome.uaa.domain.SigningKeyStatus;
import com.asrevo.cvhome.uaa.dto.AuditEventDto;
import com.asrevo.cvhome.uaa.dto.Dashboard;
import com.asrevo.cvhome.uaa.errors.AuditQueryInvalidException;
import com.asrevo.cvhome.uaa.repo.IdentityProviderRepository;
import com.asrevo.cvhome.uaa.repo.RoleRepository;
import com.asrevo.cvhome.uaa.repo.SigningKeyRepository;
import com.asrevo.cvhome.uaa.service.AdminService;
import com.asrevo.cvhome.uaa.settings.RealmSettings;
import com.asrevo.cvhome.uaa.settings.SettingsService;

import lombok.RequiredArgsConstructor;

/**
 * The overview: what happened lately, and what the realm's own data says about how it is set up.
 *
 * <p>
 * Every number here is counted from a table — the audit log, the sessions, the users — rather than kept as a
 * statistic, because a counter that drifts is worse than a query that takes a moment. The buckets come from
 * {@code date_trunc}, so a chart with a gap has a gap because nothing happened, not because a row is missing.
 * </p>
 *
 * <p>
 * <strong>The posture lines are computed, never declared.</strong> Each one asks a question of the data — is the
 * signing key older than the rotation interval, does a client hold a secret that expires this month, can an account
 * sign in without a password — so the panel cannot claim a state the realm is not in.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    static final String RANGE_24H = "24h";

    static final String RANGE_7D = "7d";

    static final String RANGE_30D = "30d";

    private static final String OK = "OK";

    private static final String WARN = "WARN";

    private static final String KEY_AGE = "keyAge";

    private static final int TOP_CLIENTS = 5;

    private static final int RECENT_FAILURES = 5;

    private static final int EXPIRING_DAYS = 30;

    private static final String BUCKETS = """
            select date_trunc(?, occurred_at) as bucket,
                   count(*) filter (where outcome = 'SUCCESS') as ok,
                   count(*) filter (where outcome = 'FAILURE') as bad
            from uaa.audit_events
            where occurred_at >= ? and event_type in ('user.login', 'user.login.failed')
            group by bucket order by bucket""";

    private static final String TOP_CLIENTS_SQL = """
            select client_id, count(*) from uaa.audit_events
            where occurred_at >= ? and event_type = 'token.issued' and client_id is not null
            group by client_id order by count(*) desc limit ?""";

    private static final String COUNT_SINCE = """
            select count(*) from uaa.audit_events where occurred_at >= ? and event_type = ?""";

    private static final String SECRETS_EXPIRING = """
            select count(*) from uaa.oauth2_registered_client
            where client_secret_expires_at is not null and client_secret_expires_at < ?""";

    private static final String CLIENTS_WITHOUT_PKCE = """
            select count(*) from uaa.oauth2_registered_client
            where authorization_grant_types like '%authorization_code%' and client_settings not like '%require-proof-key":true%'""";

    private static final String USERS_WITHOUT_PASSWORD = "select count(*) from uaa.users where password_hash is null";

    /** Live sessions, counted from Spring Session's own table: there is no API that answers "how many". */
    private static final String ACTIVE_SESSIONS = "select count(*) from uaa.spring_session where expiry_time > ?";

    private final JdbcTemplate jdbc;

    private final AuditQueryService auditQuery;

    private final AdminService users;

    private final RoleRepository roles;

    private final IdentityProviderRepository providers;

    private final SigningKeyRepository keys;

    private final SettingsService settings;

    private final Clock clock;

    @Transactional(readOnly = true)
    public Dashboard of(String range) throws AuditQueryInvalidException {
        Instant now = clock.instant();
        Duration window = windowOf(range);
        Instant from = now.minus(window);
        String truncation = RANGE_24H.equals(range) ? "hour" : "day";

        List<Dashboard.Bucket> buckets = jdbc.query(BUCKETS, (rs, i) -> new Dashboard.Bucket(
                rs.getTimestamp(1).toInstant(), rs.getLong(2), rs.getLong(3)), truncation, Timestamp.from(from));
        long signIns = buckets.stream().mapToLong(Dashboard.Bucket::success).sum();
        long failures = buckets.stream().mapToLong(Dashboard.Bucket::failure).sum();
        long tokens = count(COUNT_SINCE, Timestamp.from(from), AuditEventType.TOKEN_ISSUED.wire());

        List<Dashboard.Ranked> topClients = jdbc.query(TOP_CLIENTS_SQL,
                (rs, i) -> new Dashboard.Ranked(rs.getString(1), rs.getLong(2)), Timestamp.from(from), TOP_CLIENTS);

        List<AuditEventDto> recent = auditQuery.search(
                new AuditSearch(List.of(AuditEventType.USER_LOGIN_FAILED.wire(), AuditEventType.CLIENT_AUTH_FAILED.wire()),
                        List.of(), null, null, null, AuditOutcome.FAILURE, null, null, from, null),
                PageRequest.of(0, RECENT_FAILURES, Sort.by("occurredAt").descending())).getContent();

        return new Dashboard(range, from, now, buckets, signIns, failures, tokens, activeSessions(now), users.counts(),
                topClients, recent, posture(now, failures), counts());
    }

    private long activeSessions(Instant now) {
        return count(ACTIVE_SESSIONS, now.toEpochMilli());
    }

    private Dashboard.RailCounts counts() {
        var userCounts = users.counts();
        return new Dashboard.RailCounts(userCounts.total(), roles.count(), clientCount(), providers.count());
    }

    private long clientCount() {
        Long count = jdbc.queryForObject("select count(*) from uaa.oauth2_registered_client", Long.class);
        return count == null ? 0 : count;
    }

    /** Each line asks the data a question; nothing here is a claim the realm has not earned. */
    private List<Dashboard.PostureCheck> posture(Instant now, long failures) {
        RealmSettings realm = settings.current();
        List<Dashboard.PostureCheck> checks = new ArrayList<>();

        keys.findByStatus(SigningKeyStatus.ACTIVE).stream().findFirst().ifPresentOrElse(key -> {
            long age = Duration.between(key.getActivatedAt(), now).toDays();
            int interval = realm.keys().rotationDays();
            boolean overdue = interval > 0 && age > interval;
            checks.add(new Dashboard.PostureCheck(KEY_AGE, overdue ? WARN : OK,
                    String.format("%d", age)));
        }, () -> checks.add(new Dashboard.PostureCheck(KEY_AGE, "RISK", "0")));

        long withoutPassword = count(USERS_WITHOUT_PASSWORD);
        checks.add(new Dashboard.PostureCheck("usersWithoutPassword", withoutPassword > 0 ? WARN : OK,
                String.valueOf(withoutPassword)));

        long withoutPkce = count(CLIENTS_WITHOUT_PKCE);
        checks.add(new Dashboard.PostureCheck("clientsWithoutPkce", withoutPkce > 0 ? WARN : OK,
                String.valueOf(withoutPkce)));

        long expiring = jdbc.queryForObject(SECRETS_EXPIRING, Long.class,
                Timestamp.from(now.plus(EXPIRING_DAYS, ChronoUnit.DAYS)));
        checks.add(new Dashboard.PostureCheck("secretsExpiring", expiring > 0 ? WARN : OK, String.valueOf(expiring)));

        checks.add(new Dashboard.PostureCheck("breachedPasswordCheck", realm.password().rejectBreached() ? OK : WARN, ""));

        checks.add(new Dashboard.PostureCheck("failedSignIns", failures > realm.lockout().threshold() ? WARN : OK,
                String.valueOf(failures)));

        return checks;
    }

    private long count(String sql, Object... args) {
        Long value = jdbc.queryForObject(sql, Long.class, args);
        return value == null ? 0 : value;
    }

    private static Duration windowOf(String range) {
        Map<String, Duration> windows = new LinkedHashMap<>();
        windows.put(RANGE_24H, Duration.ofHours(24));
        windows.put(RANGE_7D, Duration.ofDays(7));
        windows.put(RANGE_30D, Duration.ofDays(30));
        return windows.getOrDefault(range, Duration.ofHours(24));
    }

}
