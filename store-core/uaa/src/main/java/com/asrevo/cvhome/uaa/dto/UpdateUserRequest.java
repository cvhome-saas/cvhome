package com.asrevo.cvhome.uaa.dto;

import java.util.Map;
import java.util.Set;

/**
 * A partial update: every absent field is left as it is.
 *
 * @param roles    when present, <em>replaces</em> the role set
 * @param metadata merged key by key; a key with a {@code null} value is removed
 */
public record UpdateUserRequest(String firstName, String lastName, Boolean enabled, Set<String> roles,
                                Map<String, Object> metadata) {
}
