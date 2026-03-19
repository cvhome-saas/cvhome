package com.asrevo.cvhome.uaa.sdk.dto;

import lombok.Builder;

import java.util.Map;
import java.util.Set;

@Builder
public record UpdateUserRequest(String firstName, String lastName, Boolean enabled, Set<String> roles,
		Map<String, String> metadata) {
}
