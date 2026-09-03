package com.asrevo.cvhome.sso.settings;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The limits a realm's own policy may not cross.
 *
 * <p>
 * Every field of {@link RealmSettings} is merchant-editable, and the shared infrastructure underneath it is not
 * theirs alone: a store that sets a ten-year access token, a lockout threshold of a million, or a one-day audit
 * retention is not only weakening itself. It is weakening a pod that other merchants' shoppers sign in to, and
 * shortening the record that a later investigation would read.
 * </p>
 *
 * <p>
 * These are deliberately configuration and not settings: they live in the deployment's own configuration, they are
 * never returned by the settings API, and there is no endpoint that writes them. A merchant cannot see the ceiling
 * they are being held to, only that a value was refused.
 * </p>
 *
 * <p>
 * The defaults are chosen to sit above what a realm starts with, so an untouched deployment is unaffected and a
 * ceiling only ever bites on a deliberate change.
 * </p>
 */
@ConfigurationProperties("com.asrevo.cvhome.sso.ceilings")
public record SsoPlatformCeilings(int maxAccessTokenTtlSeconds, int maxRefreshTokenTtlSeconds,
                                  int maxSessionSeconds, int maxRememberMeSeconds, int maxLockoutThreshold,
                                  int minLockoutDurationSeconds, int minPasswordLength, int minAuditRetentionDays) {

    private static final int ONE_HOUR = 3600;

    private static final int THIRTY_DAYS = 2_592_000;

    public SsoPlatformCeilings {
        maxAccessTokenTtlSeconds = maxAccessTokenTtlSeconds > 0 ? maxAccessTokenTtlSeconds : ONE_HOUR;
        maxRefreshTokenTtlSeconds = maxRefreshTokenTtlSeconds > 0 ? maxRefreshTokenTtlSeconds : THIRTY_DAYS;
        maxSessionSeconds = maxSessionSeconds > 0 ? maxSessionSeconds : THIRTY_DAYS;
        maxRememberMeSeconds = maxRememberMeSeconds > 0 ? maxRememberMeSeconds : THIRTY_DAYS;
        maxLockoutThreshold = maxLockoutThreshold > 0 ? maxLockoutThreshold : 20;
        minLockoutDurationSeconds = minLockoutDurationSeconds > 0 ? minLockoutDurationSeconds : 60;
        minPasswordLength = minPasswordLength > 0 ? minPasswordLength : 8;
        minAuditRetentionDays = minAuditRetentionDays > 0 ? minAuditRetentionDays : 30;
    }

}
