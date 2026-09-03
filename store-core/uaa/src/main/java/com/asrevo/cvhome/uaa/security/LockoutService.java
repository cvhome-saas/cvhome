package com.asrevo.cvhome.uaa.security;

import java.time.Clock;
import java.time.Duration;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.uaa.audit.AuditActor;
import com.asrevo.cvhome.uaa.audit.AuditEventType;
import com.asrevo.cvhome.uaa.audit.AuditRecord;
import com.asrevo.cvhome.uaa.audit.AuditService;
import com.asrevo.cvhome.uaa.domain.User;
import com.asrevo.cvhome.uaa.repo.UserRepository;
import com.asrevo.cvhome.uaa.settings.RealmSettings;
import com.asrevo.cvhome.uaa.settings.SettingsService;

import lombok.RequiredArgsConstructor;

/**
 * Counts failed sign-ins and locks the account at the realm's threshold.
 *
 * <p>
 * Every method commits on its own ({@code REQUIRES_NEW}): a failed login has no transaction of its own, and the
 * count must survive whatever the request does next. The lock itself is enforced by {@link JpaUserDetailsService}
 * reporting {@code accountNonLocked = false}, which Spring checks <em>before</em> comparing the password — so a
 * locked account never learns whether a guess was right, and never counts another attempt.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class LockoutService {

    public static final String REASON_THRESHOLD = "THRESHOLD";

    public static final String REASON_PERMANENT = "PERMANENT";

    public static final String VIA_PASSWORD = "PASSWORD";

    private final UserRepository users;

    private final SettingsService settings;

    private final AuditService audit;

    private final Clock clock;

    /** Records a wrong password for {@code username}; locks the account when the threshold is reached. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Outcome failed(String username) {
        Optional<User> found = users.findByUsername(username);
        if (found.isEmpty()) {
            return Outcome.UNKNOWN_USER;
        }
        User user = found.get();
        RealmSettings.Lockout policy = settings.current().lockout();
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        if (user.getFailedLoginAttempts() < policy.threshold()) {
            return new Outcome(false, policy.threshold() - user.getFailedLoginAttempts(), false);
        }
        user.setFailedLoginAttempts(0);
        user.setLockoutCount(user.getLockoutCount() + 1);
        user.setLockedUntil(clock.instant().plus(Duration.ofSeconds(policy.durationSeconds())));
        boolean permanent = policy.permanentAfter() > 0 && user.getLockoutCount() >= policy.permanentAfter();
        user.setLockedPermanently(permanent);
        audit.record(AuditRecord.of(AuditEventType.USER_LOCKED).actor(AuditActor.SYSTEM).user(user.getId(), username)
                .reason(permanent ? REASON_PERMANENT : REASON_THRESHOLD)
                .detail(String.format("lockout %d after %d failures", user.getLockoutCount(), policy.threshold())));
        return new Outcome(true, 0, permanent);
    }

    /** A successful sign-in clears the counters and stamps the sign-in. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void succeeded(String username, String ip, String via) {
        users.findByUsername(username).ifPresent(user -> {
            user.setFailedLoginAttempts(0);
            user.setLastSignInAt(clock.instant());
            user.setLastSignInIp(ip);
            user.setLastSignInVia(via);
        });
    }

    /** An administrator clears every lock and counter. Idempotent. */
    @Transactional
    public void unlock(User user) {
        user.setLockedUntil(null);
        user.setLockedPermanently(false);
        user.setLockoutCount(0);
        user.setFailedLoginAttempts(0);
        audit.record(AuditRecord.of(AuditEventType.USER_UNLOCKED).user(user.getId(), user.getUsername()));
    }

    /** How many attempts an account has left before its next lock, for the sign-in page; zero while it is locked. */
    @Transactional(readOnly = true)
    public int attemptsLeft(String username) {
        int threshold = settings.current().lockout().threshold();
        return users.findByUsername(username)
                .map(u -> u.isLocked(clock.instant()) ? 0 : Math.max(0, threshold - u.getFailedLoginAttempts()))
                .orElse(threshold);
    }

    /**
     * @param locked       whether this failure locked the account
     * @param attemptsLeft attempts remaining before the next lock (0 when locked)
     * @param permanent    whether the lock needs an administrator
     */
    public record Outcome(boolean locked, int attemptsLeft, boolean permanent) {

        static final Outcome UNKNOWN_USER = new Outcome(false, -1, false);

    }

}
