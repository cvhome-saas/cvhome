package com.asrevo.cvhome.uaa.sdk.dto;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * An account as uaa answers it.
 *
 * <p>
 * A subset of what the endpoint returns, on purpose: this SDK reads what a service needs to make a decision — who
 * the account is, whether it can sign in, and when it last did — and leaves the operator-facing detail (failed
 * attempts, lock expiry, password age) to the console, which reads the same endpoint directly. Unknown fields are
 * ignored, so uaa may add to the payload without a release here.
 * </p>
 *
 * @param status        {@code ACTIVE}, {@code PENDING} (invited, never signed in), {@code LOCKED} or {@code DISABLED}
 * @param emailVerified whether the address has been proven, by an invitation or by an administrator
 * @param lastSignInAt  null for an account that has never signed in
 */
public record UserDto(UUID id, String username, String email, String firstName, String lastName, boolean enabled,
                      String status, boolean emailVerified, Set<String> roles, Map<String, Object> metadata,
                      Instant lastSignInAt) {
}
