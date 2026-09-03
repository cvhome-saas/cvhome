package com.asrevo.cvhome.uaa.settings;

import java.time.Instant;

/**
 * The realm's policy on the wire. Every field is read back exactly as written; {@code version} must be sent back on
 * an update so a stale form cannot overwrite a newer one.
 */
public record RealmSettings(String displayName, String supportEmail, String defaultLocale,
                            boolean selfRegistrationEnabled, boolean requireEmailVerification,
                            PasswordPolicy password, Lockout lockout, Sessions sessions, Tokens tokens, Keys keys,
                            int auditRetentionDays, Instant updatedAt, String updatedBy, long version) {

    public record PasswordPolicy(int minLength, boolean requireUpper, boolean requireLower, boolean requireDigit,
                                 boolean requireSpecial, int historyCount, int expiryDays, boolean rejectBreached) {
    }

    public record Lockout(int threshold, int durationSeconds, int permanentAfter) {
    }

    public record Sessions(int idleSeconds, int maxSeconds, boolean rememberMeEnabled, int rememberMeSeconds,
                           boolean singleSessionPerUser) {
    }

    public record Tokens(int maxAccessTokenTtlSeconds, int defaultAccessTokenTtlSeconds,
                         int defaultRefreshTokenTtlSeconds, int clientSecretValidityDays, int clientSecretGraceHours) {
    }

    public record Keys(int rotationDays, int retireDays) {
    }

    static RealmSettings of(Settings s) {
        return new RealmSettings(s.getDisplayName(), s.getSupportEmail(), s.getDefaultLocale(),
                s.isSelfRegistrationEnabled(), s.isRequireEmailVerification(),
                new PasswordPolicy(s.getPasswordMinLength(), s.isPasswordRequireUpper(), s.isPasswordRequireLower(),
                        s.isPasswordRequireDigit(), s.isPasswordRequireSpecial(), s.getPasswordHistoryCount(),
                        s.getPasswordExpiryDays(), s.isPasswordHibpCheck()),
                new Lockout(s.getLockoutThreshold(), s.getLockoutDurationSeconds(), s.getLockoutPermanentAfter()),
                new Sessions(s.getSessionIdleSeconds(), s.getSessionMaxSeconds(), s.isRememberMeEnabled(),
                        s.getRememberMeSeconds(), s.isSingleSessionPerUser()),
                new Tokens(s.getMaxAccessTokenTtlSeconds(), s.getDefaultAccessTokenTtlSeconds(),
                        s.getDefaultRefreshTokenTtlSeconds(), s.getClientSecretValidityDays(),
                        s.getClientSecretGraceHours()),
                new Keys(s.getKeyRotationDays(), s.getKeyRetireDays()),
                s.getAuditRetentionDays(), s.getUpdatedAt(), s.getUpdatedBy(), s.getVersion());
    }

    void applyTo(Settings s) {
        s.setDisplayName(displayName);
        s.setSupportEmail(supportEmail);
        s.setDefaultLocale(defaultLocale);
        s.setSelfRegistrationEnabled(selfRegistrationEnabled);
        s.setRequireEmailVerification(requireEmailVerification);
        s.setPasswordMinLength(password.minLength());
        s.setPasswordRequireUpper(password.requireUpper());
        s.setPasswordRequireLower(password.requireLower());
        s.setPasswordRequireDigit(password.requireDigit());
        s.setPasswordRequireSpecial(password.requireSpecial());
        s.setPasswordHistoryCount(password.historyCount());
        s.setPasswordExpiryDays(password.expiryDays());
        s.setPasswordHibpCheck(password.rejectBreached());
        s.setLockoutThreshold(lockout.threshold());
        s.setLockoutDurationSeconds(lockout.durationSeconds());
        s.setLockoutPermanentAfter(lockout.permanentAfter());
        s.setSessionIdleSeconds(sessions.idleSeconds());
        s.setSessionMaxSeconds(sessions.maxSeconds());
        s.setRememberMeEnabled(sessions.rememberMeEnabled());
        s.setRememberMeSeconds(sessions.rememberMeSeconds());
        s.setSingleSessionPerUser(sessions.singleSessionPerUser());
        s.setMaxAccessTokenTtlSeconds(tokens.maxAccessTokenTtlSeconds());
        s.setDefaultAccessTokenTtlSeconds(tokens.defaultAccessTokenTtlSeconds());
        s.setDefaultRefreshTokenTtlSeconds(tokens.defaultRefreshTokenTtlSeconds());
        s.setClientSecretValidityDays(tokens.clientSecretValidityDays());
        s.setClientSecretGraceHours(tokens.clientSecretGraceHours());
        s.setKeyRotationDays(keys.rotationDays());
        s.setKeyRetireDays(keys.retireDays());
        s.setAuditRetentionDays(auditRetentionDays);
    }

}
