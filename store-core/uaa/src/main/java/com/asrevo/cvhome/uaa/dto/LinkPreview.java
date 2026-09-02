package com.asrevo.cvhome.uaa.dto;

import java.time.Instant;

import com.asrevo.cvhome.uaa.settings.RealmSettings;

/**
 * What the public accept page shows before a password is chosen: whose account this is and what the password must
 * satisfy. Deliberately nothing else — the page is reachable by whoever holds the link.
 *
 * @param kind {@code INVITATION} or {@code PASSWORD_RESET}
 */
public record LinkPreview(String kind, String username, String email, String firstName, Instant expiresAt,
                          PasswordRules password) {

    public static final String INVITATION = "INVITATION";

    public static final String PASSWORD_RESET = "PASSWORD_RESET";

    public record PasswordRules(int minLength, boolean requireUpper, boolean requireLower, boolean requireDigit,
                                boolean requireSpecial) {

        public static PasswordRules of(RealmSettings.PasswordPolicy policy) {
            return new PasswordRules(policy.minLength(), policy.requireUpper(), policy.requireLower(),
                    policy.requireDigit(), policy.requireSpecial());
        }

    }

}
