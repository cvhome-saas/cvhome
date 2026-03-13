package com.asrevo.cvhome.uaa.sdk.dto;

import java.util.Map;

public record CreateUserRequest(String username, String email, Map<String, Object> metadata) {
}
