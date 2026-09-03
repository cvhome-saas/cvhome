package com.asrevo.cvhome.uaa.domain.user;

import java.io.Serializable;
import java.util.Map;

/**
 * What to narrow a user search by; every part is optional and they combine with AND.
 *
 * @param q        a case-insensitive contains over username, email and the names
 * @param status   {@code ACTIVE}, {@code PENDING}, {@code LOCKED} or {@code DISABLED}
 * @param role     an exact role name
 * @param metadata exact matches on metadata keys — how tenancy finds an organisation's members
 */
public record UserSearchFilters(String q, String status, String role, Map<String, String> metadata)
        implements Serializable {

    public static UserSearchFilters none() {
        return new UserSearchFilters(null, null, null, Map.of());
    }

    public static UserSearchFilters ofMetadata(Map<String, String> metadata) {
        return new UserSearchFilters(null, null, null, metadata);
    }

}
