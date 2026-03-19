package com.asrevo.cvhome.uaa.dto;

import java.util.Map;

public record UpdateUserRequest(String firstName, String lastName, Boolean enabled, Map<String, Object> metadata) {
}
