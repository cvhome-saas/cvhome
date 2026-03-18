package com.asrevo.cvhome.uaa.sdk.dto;

import lombok.Builder;

import java.util.Map;

@Builder
public record CreateUserRequest(String username, String email, Map<String, String> metadata) {
}
