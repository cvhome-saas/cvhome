package com.asrevo.cvhome.uaa.sdk.dto;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record UserDto(UUID id, String username, String email, Set<String> roles, Map<String, Object> metadata) {
}
