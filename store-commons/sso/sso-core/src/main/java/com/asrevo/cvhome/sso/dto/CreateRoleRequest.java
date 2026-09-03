package com.asrevo.cvhome.sso.dto;

import java.util.Set;
import java.util.UUID;

import com.asrevo.cvhome.sso.domain.RoleScope;

/**
 * @param name        upper-case, {@code A-Z 0-9 _}, 2–80 characters — it is the token claim and the authority
 * @param permissions catalogue keys; an unknown one fails the request
 */
public record CreateRoleRequest(String name, String description, RoleScope scope, UUID inheritsFromId,
                                Set<String> permissions) {
}
