package com.asrevo.cvhome.sso.password;

import java.time.Clock;
import java.time.Duration;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.settings.RealmSettings;
import com.asrevo.cvhome.sso.settings.SettingsService;
import com.asrevo.cvhome.uaa.errors.PasswordCompromisedException;
import com.asrevo.cvhome.uaa.errors.PasswordPolicyViolationException;
import com.asrevo.cvhome.uaa.errors.PasswordReusedException;

import lombok.RequiredArgsConstructor;

/**
 * The one way a password is set.
 *
 * <p>
 * Admin reset, self-service change, invitation and reset-link acceptance all come through here, so the policy,
 * the history and the breach check apply to every one of them. Seeds and boot initializers write hashes directly
 * and are the deliberate exception.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class PasswordService {

    private final PasswordEncoder encoder;

    private final PasswordHistoryRepository history;

    private final PasswordPolicyValidator validator;

    private final CompromisedPasswordGate breached;

    private final SettingsService settings;

    private final Clock clock;

    /** Validates, checks history and the breach corpus, then writes the hash and remembers it. */
    @Transactional
    public void setPassword(User user, String raw)
            throws PasswordPolicyViolationException, PasswordReusedException, PasswordCompromisedException {
        RealmSettings.PasswordPolicy policy = settings.current().password();
        validator.validate(raw, user, policy);
        assertNotReused(user, raw, policy.historyCount());
        if (policy.rejectBreached()) {
            breached.check(raw);
        }
        String hash = encoder.encode(raw);
        user.setPasswordHash(hash);
        user.setPasswordChangedAt(clock.instant());
        user.setFailedLoginAttempts(0);
        if (user.getActivatedAt() == null) {
            user.setActivatedAt(clock.instant());
        }
        remember(user, hash, policy.historyCount());
    }

    /** Whether {@code raw} is the account's current password. */
    public boolean matches(User user, String raw) {
        return user.getPasswordHash() != null && encoder.matches(raw, user.getPasswordHash());
    }

    private void assertNotReused(User user, String raw, int remembered) throws PasswordReusedException {
        if (remembered <= 0 || user.getId() == null) {
            return;
        }
        if (matches(user, raw)) {
            throw PasswordReusedException.of(remembered);
        }
        List<PasswordHistory> recent = history.findByUserIdOrderByCreatedAtDesc(user.getId());
        for (int i = 0; i < Math.min(remembered, recent.size()); i++) {
            if (encoder.matches(raw, recent.get(i).getPasswordHash())) {
                throw PasswordReusedException.of(remembered);
            }
        }
    }

    private void remember(User user, String hash, int remembered) {
        if (user.getId() == null) {
            return;
        }
        PasswordHistory row = new PasswordHistory();
        row.setUserId(user.getId());
        row.setPasswordHash(hash);
        row.setCreatedAt(clock.instant());
        history.save(row);
        List<PasswordHistory> all = history.findByUserIdOrderByCreatedAtDesc(user.getId());
        int keep = Math.max(remembered, 1);
        if (all.size() > keep) {
            history.deleteAll(all.subList(keep, all.size()));
        }
    }

    /** Whether the account's password is older than the realm's expiry, when one is set. */
    public boolean expired(User user) {
        int days = settings.current().password().expiryDays();
        if (days <= 0 || user.getPasswordChangedAt() == null) {
            return false;
        }
        return user.getPasswordChangedAt().plus(Duration.ofDays(days)).isBefore(clock.instant());
    }

}
