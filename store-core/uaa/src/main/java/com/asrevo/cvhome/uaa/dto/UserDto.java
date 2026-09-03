package com.asrevo.cvhome.uaa.dto;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.asrevo.cvhome.uaa.domain.UserStatus;

/**
 * An account as the console reads it. {@code status} is derived (see {@link UserStatus}); {@code roles} are bare
 * names; {@code metadata} is the open bag tenancy stamps {@code org}/{@code store} into.
 */
public record UserDto(UUID id, String username, String email, String firstName, String lastName, boolean enabled,
                      UserStatus status, boolean emailVerified, Set<String> roles, Map<String, Object> metadata,
                      Instant lastSignInAt, String lastSignInClientId, String lastSignInVia, Instant lockedUntil,
                      int failedLoginAttempts, Instant passwordChangedAt, Instant createdAt) {
}
