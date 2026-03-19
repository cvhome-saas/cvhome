package com.asrevo.cvhome.uaa.dto;

import java.util.Map;
import java.util.Set;

public record UpdateUserRequest(String firstName, String lastName, Boolean enabled, Set<String> roles,
		Map<String, Object> metadata) {
}
