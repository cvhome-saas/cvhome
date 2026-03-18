package com.asrevo.cvhome.uaa.sdk.dto;

import lombok.Builder;

import java.util.Map;

@Builder
public record UpdateUserRequest(String status, Map<String, String> metadata) {
}
