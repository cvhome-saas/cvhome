package com.asrevo.cvhome.uaa.settings;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The realm's one row of policy. Column defaults live in {@code schema.sql}; the seed inserts the row.
 */
@Entity
@Table(name = "settings")
@Data
@NoArgsConstructor
public class Settings {

    public static final short SINGLETON_ID = 1;

    @Id
    private short id = SINGLETON_ID;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName = "cvhome ID";

    @Column(name = "support_email", length = 254)
    private String supportEmail;

    @Column(name = "default_locale", nullable = false, length = 10)
    private String defaultLocale = "en";

    @Column(name = "self_registration_enabled", nullable = false)
    private boolean selfRegistrationEnabled;

    @Column(name = "require_email_verification", nullable = false)
    private boolean requireEmailVerification;

    @Column(name = "password_min_length", nullable = false)
    private int passwordMinLength = 12;

    @Column(name = "password_require_upper", nullable = false)
    private boolean passwordRequireUpper = true;

    @Column(name = "password_require_lower", nullable = false)
    private boolean passwordRequireLower = true;

    @Column(name = "password_require_digit", nullable = false)
    private boolean passwordRequireDigit = true;

    @Column(name = "password_require_special", nullable = false)
    private boolean passwordRequireSpecial;

    @Column(name = "password_history_count", nullable = false)
    private int passwordHistoryCount = 5;

    @Column(name = "password_expiry_days", nullable = false)
    private int passwordExpiryDays;

    @Column(name = "password_hibp_check", nullable = false)
    private boolean passwordHibpCheck;

    @Column(name = "lockout_threshold", nullable = false)
    private int lockoutThreshold = 5;

    @Column(name = "lockout_duration_seconds", nullable = false)
    private int lockoutDurationSeconds = 900;

    @Column(name = "lockout_permanent_after", nullable = false)
    private int lockoutPermanentAfter = 5;

    @Column(name = "session_idle_seconds", nullable = false)
    private int sessionIdleSeconds = 1800;

    @Column(name = "session_max_seconds", nullable = false)
    private int sessionMaxSeconds = 43200;

    @Column(name = "remember_me_enabled", nullable = false)
    private boolean rememberMeEnabled;

    @Column(name = "remember_me_seconds", nullable = false)
    private int rememberMeSeconds = 2592000;

    @Column(name = "single_session_per_user", nullable = false)
    private boolean singleSessionPerUser;

    @Column(name = "max_access_token_ttl_seconds", nullable = false)
    private int maxAccessTokenTtlSeconds = 3600;

    @Column(name = "default_access_token_ttl_seconds", nullable = false)
    private int defaultAccessTokenTtlSeconds = 900;

    @Column(name = "default_refresh_token_ttl_seconds", nullable = false)
    private int defaultRefreshTokenTtlSeconds = 43200;

    @Column(name = "client_secret_validity_days", nullable = false)
    private int clientSecretValidityDays = 365;

    @Column(name = "client_secret_grace_hours", nullable = false)
    private int clientSecretGraceHours = 24;

    @Column(name = "key_rotation_days", nullable = false)
    private int keyRotationDays = 90;

    @Column(name = "key_retire_days", nullable = false)
    private int keyRetireDays = 7;

    @Column(name = "audit_retention_days", nullable = false)
    private int auditRetentionDays = 365;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "updated_by", length = 190)
    private String updatedBy;

    @Version
    @Column(nullable = false)
    private long version;

}
