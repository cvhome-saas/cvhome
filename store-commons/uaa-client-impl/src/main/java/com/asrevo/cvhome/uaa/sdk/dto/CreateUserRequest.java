package com.asrevo.cvhome.uaa.sdk.dto;

import lombok.Builder;

import java.util.Map;

@Builder
public record CreateUserRequest(String username, String email, String firstName, String lastName,
		Map<String, String> metadata) {
}
