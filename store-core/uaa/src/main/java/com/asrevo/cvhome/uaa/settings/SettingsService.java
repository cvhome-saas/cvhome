package com.asrevo.cvhome.uaa.settings;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.uaa.audit.AuditEventType;
import com.asrevo.cvhome.uaa.audit.AuditRecord;
import com.asrevo.cvhome.uaa.audit.AuditService;
import com.asrevo.cvhome.uaa.audit.AuditTargetType;
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
 */
@Service
public class SettingsService {

    static final Duration CACHE_TTL = Duration.ofSeconds(30);

    static final String SINGLETON = "settings";

    private static final int MIN_PASSWORD_LENGTH = 8;

    private static final int MAX_PASSWORD_LENGTH = 128;

    private static final String NOT_NEGATIVE = "must not be negative";

    private static final String AT_LEAST_ONE = "must be at least 1";

    private static final String AT_LEAST_A_MINUTE = "must be at least 60";

    private final SettingsRepository repository;

    private final AuditService audit;

    private final LoadingCache<String, RealmSettings> cache;

    public SettingsService(SettingsRepository repository, AuditService audit) {
        this.repository = repository;
        this.audit = audit;
        this.cache = Caffeine.newBuilder().expireAfterWrite(CACHE_TTL).build(key -> RealmSettings.of(load()));
    }

    private Settings load() {
        return repository.findById(Settings.SINGLETON_ID).orElseGet(() -> repository.save(new Settings()));
    }

    /** The current policy, at most thirty seconds old. */
    public RealmSettings current() {
        return Objects.requireNonNull(cache.get(SINGLETON));
    }

    @Transactional
    public RealmSettings update(RealmSettings requested, String actor)
            throws SettingsInvalidException, SettingsConflictException {
        validate(requested);
        Settings entity = load();
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
        cache.invalidateAll();
        audit.record(AuditRecord.of(AuditEventType.SETTINGS_UPDATED)
                .target(AuditTargetType.SETTINGS, SINGLETON, SINGLETON)
                .change(before, after));
        return after;
    }

    private static void validate(RealmSettings s) throws SettingsInvalidException {
        require(s.password().minLength() >= MIN_PASSWORD_LENGTH && s.password().minLength() <= MAX_PASSWORD_LENGTH,
                "password.minLength", "must be between 8 and 128");
        require(s.password().historyCount() >= 0, "password.historyCount", NOT_NEGATIVE);
        require(s.password().expiryDays() >= 0, "password.expiryDays", NOT_NEGATIVE);
        require(s.lockout().threshold() >= 1, "lockout.threshold", AT_LEAST_ONE);
        require(s.lockout().durationSeconds() >= 1, "lockout.durationSeconds", AT_LEAST_ONE);
        require(s.lockout().permanentAfter() >= 0, "lockout.permanentAfter", NOT_NEGATIVE);
        require(s.sessions().idleSeconds() >= 60, "sessions.idleSeconds", AT_LEAST_A_MINUTE);
        require(s.sessions().maxSeconds() >= s.sessions().idleSeconds(), "sessions.maxSeconds",
                "must not be shorter than the idle timeout");
        require(s.sessions().rememberMeSeconds() >= 60, "sessions.rememberMeSeconds", AT_LEAST_A_MINUTE);
        require(s.tokens().defaultAccessTokenTtlSeconds() >= 60, "tokens.defaultAccessTokenTtlSeconds",
                AT_LEAST_A_MINUTE);
        require(s.tokens().maxAccessTokenTtlSeconds() >= s.tokens().defaultAccessTokenTtlSeconds(),
                "tokens.maxAccessTokenTtlSeconds", "must not be shorter than the default");
        require(s.tokens().defaultRefreshTokenTtlSeconds() >= 60, "tokens.defaultRefreshTokenTtlSeconds",
                AT_LEAST_A_MINUTE);
        require(s.tokens().clientSecretValidityDays() >= 0, "tokens.clientSecretValidityDays", NOT_NEGATIVE);
        require(s.tokens().clientSecretGraceHours() >= 0, "tokens.clientSecretGraceHours", NOT_NEGATIVE);
        require(s.keys().rotationDays() >= 0, "keys.rotationDays", NOT_NEGATIVE);
        require(s.keys().retireDays() >= 1, "keys.retireDays", AT_LEAST_ONE);
        require(s.auditRetentionDays() >= 1, "auditRetentionDays", AT_LEAST_ONE);
        require(s.displayName() != null && !s.displayName().isBlank(), "displayName", "must not be blank");
    }

    private static void require(boolean condition, String field, String rule) throws SettingsInvalidException {
        if (!condition) {
            throw SettingsInvalidException.of(field, rule);
        }
    }

    /** For tests and callers that need the policy without the cache. */
    RealmSettings reload() {
        cache.invalidateAll();
        return current();
    }


}
