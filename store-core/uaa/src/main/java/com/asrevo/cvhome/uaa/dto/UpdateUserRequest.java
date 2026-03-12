package com.asrevo.cvhome.uaa.dto;

import java.util.Map;

public record UpdateUserRequest(String status, Map<String, Object> metadata) {
}
