package com.asrevo.cvhome.uaa.sdk.dto;

import lombok.Builder;

import java.util.Map;

@Builder
public record UpdateUserRequest(String firstName, String lastName, Boolean enabled, Map<String, String> metadata) {
}
