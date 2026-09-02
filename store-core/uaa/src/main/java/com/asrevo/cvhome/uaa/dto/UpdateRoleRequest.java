package com.asrevo.cvhome.uaa.dto;

import java.util.Set;
import java.util.UUID;

import com.asrevo.cvhome.uaa.domain.RoleScope;

/**
 * A partial update. For a system role {@code name} and {@code scope} must be absent or unchanged.
 *
 * @param clearInheritsFrom {@code true} to remove the parent; {@code inheritsFromId} sets one
 */
public record UpdateRoleRequest(String name, String description, RoleScope scope, UUID inheritsFromId,
                                Boolean clearInheritsFrom, Set<String> permissions) {
}
