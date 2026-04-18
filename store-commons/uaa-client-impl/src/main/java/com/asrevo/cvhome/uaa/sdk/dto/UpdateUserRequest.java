package com.asrevo.cvhome.uaa.sdk.dto;

import java.util.Map;
import java.util.Set;

import lombok.Builder;

@Builder
public record UpdateUserRequest(String firstName, String lastName, Boolean enabled, Set<String> roles,
                                Map<String, String> metadata) {
}
