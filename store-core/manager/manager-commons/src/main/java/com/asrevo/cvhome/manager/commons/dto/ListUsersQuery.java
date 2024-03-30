package com.asrevo.cvhome.manager.commons.dto;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.manager.commons.domain.KeyCloakQuery;

import java.util.Map;
import java.util.stream.Collectors;

public record ListUsersQuery(IdentityId org, ManagerStoreId storeId) implements KeyCloakQuery {
    @Override
    public String query() {
        return Map.of("org", org, "store", storeId).entrySet().stream()
                .filter(it -> it.getValue() != null)
                .map(it -> it.getKey() + ":" + it.getValue().getId().toString())
                .collect(Collectors.joining(" "));
    }
}
