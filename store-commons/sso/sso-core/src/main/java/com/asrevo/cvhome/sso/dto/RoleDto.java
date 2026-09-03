package com.asrevo.cvhome.sso.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import com.asrevo.cvhome.sso.domain.RoleScope;

/**
 * A role as the console reads it.
 *
 * @param permissions          what the role grants itself
 * @param effectivePermissions those plus everything inherited
 * @param userCount            accounts holding it directly
 */
public record RoleDto(UUID id, String name, String description, RoleScope scope, boolean systemRole,
                      UUID inheritsFromId, String inheritsFromName, Set<String> permissions,
                      Set<String> effectivePermissions, long userCount, Instant createdAt, Instant updatedAt) {
}
