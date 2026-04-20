package com.asrevo.cvhome.uaa.sdk.dto;

import java.util.Map;
import java.util.Set;

import lombok.Builder;

@Builder
public record CreateUserRequest(String username, String email, String firstName, String lastName, Set<String> roles,
                                Map<String, String> metadata) {
}
