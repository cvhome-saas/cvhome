package com.asrevo.cvhome.sso.dto;

import java.util.Map;
import java.util.Set;

import jakarta.validation.constraints.Email;

/**
 * A partial update: every absent field is left as it is.
 *
 * @param email    when present, replaces the address and marks it unverified again
 * @param roles    when present, <em>replaces</em> the role set
 * @param metadata merged key by key; a key with a {@code null} value is removed
 */
public record UpdateUserRequest(String firstName, String lastName, @Email String email, Boolean enabled, Set<String> roles,
                                Map<String, Object> metadata) {
}
