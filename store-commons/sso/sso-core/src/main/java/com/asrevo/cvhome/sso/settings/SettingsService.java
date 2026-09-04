package com.asrevo.cvhome.sso.settings;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.RealmId;
import com.asrevo.cvhome.sso.audit.AuditEventType;
import com.asrevo.cvhome.sso.audit.AuditRecord;
import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.audit.AuditTargetType;
import com.asrevo.cvhome.sso.realm.RealmContext;
import com.asrevo.cvhome.sso.realm.SsoTenantIdentifierResolver;
import com.asrevo.cvhome.uaa.errors.SettingsConflictException;
import com.asrevo.cvhome.uaa.errors.SettingsInvalidException;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

/**
 * Reads and writes the realm's policy.
 *
 * <p>
 * {@link #current()} is on the path of every login and every token, so it is cached for thirty seconds and
 * invalidated on write. Ranges are checked here, not by the database: a lockout threshold of zero would lock every
 * account on its first attempt, and the column would happily store it.
 * </p>
 *
 * <p>
 * <strong>The cache is keyed by realm.</strong> It was keyed by a constant while uaa was the only deployment,
 * which with a realm per store would have let whichever store loaded first impose its password policy, lockout
 * thresholds and token lifetimes on every other store for the next thirty seconds. The key comes from the same
 * resolver Hibernate uses, so the cache cannot disagree with the row it caches.
 * </p>
 */
@Service
public class SettingsService {

    static final Duration CACHE_TTL = Duration.ofSeconds(30);

    private static final int MIN_PASSWORD_LENGTH = 8;

    private static final int MAX_PASSWORD_LENGTH = 128;

    private static final String NOT_NEGATIVE = "must not be negative";

    private static final String AT_LEAST_ONE = "must be at least 1";

    private static final String AT_LEAST_A_MINUTE = "must be at least 60";

    private static final String PASSWORD_MIN_LENGTH = "password.minLength";

    private static final String LOCKOUT_THRESHOLD = "lockout.threshold";

    private static final String LOCKOUT_DURATION = "lockout.durationSeconds";

    private static final String SESSION_MAX = "sessions.maxSeconds";

    private static final String REMEMBER_ME = "sessions.rememberMeSeconds";

    private static final String ACCESS_TTL = "tokens.defaultAccessTokenTtlSeconds";

    private static final String MAX_ACCESS_TTL = "tokens.maxAccessTokenTtlSeconds";

    private static final String REFRESH_TTL = "tokens.defaultRefreshTokenTtlSeconds";

    private static final String AUDIT_RETENTION = "auditRetentionDays";

    private final SettingsRepository repository;

    private final AuditService audit;

    private final SsoTenantIdentifierResolver realms;

    private final SsoPlatformCeilings ceilings;

    private final LoadingCache<String, RealmSettings> cache;

    public SettingsService(SettingsRepository repository, AuditService audit, SsoTenantIdentifierResolver realms,
                           SsoPlatformCeilings ceilings) {
        this.repository = repository;
        this.audit = audit;
        this.realms = realms;
        this.ceilings = ceilings;
        this.cache = Caffeine.newBuilder().expireAfterWrite(CACHE_TTL).build(realm -> RealmSettings.of(load(realm)));
    }

    /** A realm that has never been configured gets the defaults, written on first read so it has a version. */
    private Settings load(String realm) {
        return repository.findById(realm).orElseGet(() -> repository.save(new Settings(realm)));
    }

    /** The current realm's policy, at most thirty seconds old. */
    public RealmSettings current() {
        return Objects.requireNonNull(cache.get(realms.resolveCurrentTenantIdentifier()));
    }

    /**
     * The platform's own policy, for the things that are not a realm's to decide.
     *
     * <p>
     * The signing key is one per deployment — one per pod, on cua — so the interval it rotates on cannot come from
     * whichever realm a background thread last happened to be in. It comes from here. In a single-realm deployment
     * this is the same row {@link #current()} returns, so uaa's operator still sets it from their own console.
     * </p>
     */
    public RealmSettings platform() {
        return RealmContext.callIn(RealmId.PLATFORM, () -> Objects.requireNonNull(cache.get(RealmId.PLATFORM.getId())));
    }

    @Transactional
    public RealmSettings update(RealmSettings requested, String actor)
            throws SettingsInvalidException, SettingsConflictException {
        validate(requested);
        clamp(requested);
        String realm = realms.resolveCurrentTenantIdentifier();
        Settings entity = load(realm);
        if (entity.getVersion() != requested.version()) {
            throw SettingsConflictException.of(requested.version());
        }
        RealmSettings before = RealmSettings.of(entity);
        requested.applyTo(entity);
        entity.setUpdatedAt(Instant.now());
        entity.setUpdatedBy(actor);
        RealmSettings after;
        try {
            after = RealmSettings.of(repository.saveAndFlush(entity));
        } catch (ObjectOptimisticLockingFailureException e) {
            throw SettingsConflictException.of(requested.version());
        }
        cache.invalidate(realm);
        audit.record(AuditRecord.of(AuditEventType.SETTINGS_UPDATED)
                .target(AuditTargetType.SETTINGS, realm, realm)
                .change(before, after));
        return after;
    }

    private static void validate(RealmSettings s) throws SettingsInvalidException {
        require(s.password().minLength() >= MIN_PASSWORD_LENGTH && s.password().minLength() <= MAX_PASSWORD_LENGTH,
                PASSWORD_MIN_LENGTH, "must be between 8 and 128");
        require(s.password().historyCount() >= 0, "password.historyCount", NOT_NEGATIVE);
        require(s.password().expiryDays() >= 0, "password.expiryDays", NOT_NEGATIVE);
        require(s.lockout().threshold() >= 1, LOCKOUT_THRESHOLD, AT_LEAST_ONE);
        require(s.lockout().durationSeconds() >= 1, LOCKOUT_DURATION, AT_LEAST_ONE);
        require(s.lockout().permanentAfter() >= 0, "lockout.permanentAfter", NOT_NEGATIVE);
        require(s.sessions().idleSeconds() >= 60, "sessions.idleSeconds", AT_LEAST_A_MINUTE);
        require(s.sessions().maxSeconds() >= s.sessions().idleSeconds(), SESSION_MAX,
                "must not be shorter than the idle timeout");
        require(s.sessions().rememberMeSeconds() >= 60, REMEMBER_ME, AT_LEAST_A_MINUTE);
        require(s.tokens().defaultAccessTokenTtlSeconds() >= 60, ACCESS_TTL,
                AT_LEAST_A_MINUTE);
        require(s.tokens().maxAccessTokenTtlSeconds() >= s.tokens().defaultAccessTokenTtlSeconds(),
                MAX_ACCESS_TTL, "must not be shorter than the default");
        require(s.tokens().defaultRefreshTokenTtlSeconds() >= 60, REFRESH_TTL,
                AT_LEAST_A_MINUTE);
        require(s.tokens().clientSecretValidityDays() >= 0, "tokens.clientSecretValidityDays", NOT_NEGATIVE);
        require(s.tokens().clientSecretGraceHours() >= 0, "tokens.clientSecretGraceHours", NOT_NEGATIVE);
        require(s.keys().rotationDays() >= 0, "keys.rotationDays", NOT_NEGATIVE);
        require(s.keys().retireDays() >= 1, "keys.retireDays", AT_LEAST_ONE);
        require(s.auditRetentionDays() >= 1, AUDIT_RETENTION, AT_LEAST_ONE);
        require(s.displayName() != null && !s.displayName().isBlank(), "displayName", "must not be blank");
    }

    /**
     * Holds a realm's policy to the platform's limits. Separate from {@link #validate} because the two refuse for
     * different reasons: validation rejects a value that cannot work at all, this rejects one that works fine for
     * the realm asking and badly for everyone sharing the deployment with it.
     */
    private void clamp(RealmSettings s) throws SettingsInvalidException {
        atMost(s.tokens().maxAccessTokenTtlSeconds(), ceilings.maxAccessTokenTtlSeconds(),
                MAX_ACCESS_TTL);
        atMost(s.tokens().defaultAccessTokenTtlSeconds(), ceilings.maxAccessTokenTtlSeconds(),
                ACCESS_TTL);
        atMost(s.tokens().defaultRefreshTokenTtlSeconds(), ceilings.maxRefreshTokenTtlSeconds(),
                REFRESH_TTL);
        atMost(s.sessions().maxSeconds(), ceilings.maxSessionSeconds(), SESSION_MAX);
        atMost(s.sessions().rememberMeSeconds(), ceilings.maxRememberMeSeconds(), REMEMBER_ME);
        atMost(s.lockout().threshold(), ceilings.maxLockoutThreshold(), LOCKOUT_THRESHOLD);
        atLeast(s.lockout().durationSeconds(), ceilings.minLockoutDurationSeconds(), LOCKOUT_DURATION);
        atLeast(s.password().minLength(), ceilings.minPasswordLength(), PASSWORD_MIN_LENGTH);
        atLeast(s.auditRetentionDays(), ceilings.minAuditRetentionDays(), AUDIT_RETENTION);
    }

    private static void atMost(int value, int ceiling, String field) throws SettingsInvalidException {
        require(value <= ceiling, field, String.format("must not be more than %d on this platform", ceiling));
    }

    private static void atLeast(int value, int floor, String field) throws SettingsInvalidException {
        require(value >= floor, field, String.format("must be at least %d on this platform", floor));
    }

    private static void require(boolean condition, String field, String rule) throws SettingsInvalidException {
        if (!condition) {
            throw SettingsInvalidException.of(field, rule);
        }
    }

    /** For tests and callers that need this realm's policy without the cache. */
    RealmSettings reload() {
        cache.invalidate(realms.resolveCurrentTenantIdentifier());
        return current();
    }


}
