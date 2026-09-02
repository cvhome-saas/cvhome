package com.asrevo.cvhome.uaa.dto;

import java.util.Map;

import com.asrevo.cvhome.uaa.domain.UserStatus;

/**
 * The account list's filters; every part is optional and they combine with AND.
 *
 * @param q        a case-insensitive contains over username, email and names
 * @param metadata equality on metadata keys, the filter tenancy has always used ({@code metadata[org]=...})
 */
public record UserSearch(String q, UserStatus status, String role, Map<String, String> metadata) {

    public static UserSearch none() {
        return new UserSearch(null, null, null, Map.of());
    }

    public boolean hasQuery() {
        return q != null && !q.isBlank();
    }

    public boolean hasRole() {
        return role != null && !role.isBlank();
    }

}
