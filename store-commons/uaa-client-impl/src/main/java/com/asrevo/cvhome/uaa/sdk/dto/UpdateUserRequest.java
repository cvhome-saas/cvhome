package com.asrevo.cvhome.uaa.sdk.dto;

import java.util.Map;

public record UpdateUserRequest(String email, String status, Map<String, Object> metadata) {
}
