package com.asrevo.cvhome.sso.domain;

/**
 * The one word the console shows for an account.
 *
 * <p>
 * Derived, never stored: {@code DISABLED} when an administrator switched the account off, {@code LOCKED} while a
 * lockout holds, {@code PENDING} when the account exists but has never had a password (an invitation not yet
 * accepted), {@code ACTIVE} otherwise.
 * </p>
 */
public enum UserStatus {
    ACTIVE, PENDING, LOCKED, DISABLED
}
