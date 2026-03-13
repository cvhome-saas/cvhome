package com.asrevo.cvhome.uaa.domain.user;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import java.util.Map;
import java.util.stream.Collectors;

public record ListUsersQuery(ManagerOrgId org, ManagerStoreId store) implements KeyCloakQuery {
	@Override
	public String query() {
		return Map.of("org", org, "store", store)
			.entrySet()
			.stream()
			.filter(it -> it.getValue() != null && it.getValue().getId() != null
					&& !"*".equals(it.getValue().getId().toString()))
			.map(it -> it.getKey() + ":" + it.getValue().getId().toString())
			.collect(Collectors.joining(" "));
	}
}
