package com.asrevo.cvhome.uaa.dto;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record UserDto(UUID id, String username, String email, String firstName, String lastName, boolean enabled,
                      Set<String> roles, Map<String, Object> metadata) {
}
