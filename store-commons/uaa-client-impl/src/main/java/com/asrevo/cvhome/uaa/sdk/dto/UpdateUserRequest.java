package com.asrevo.cvhome.uaa.sdk.dto;

import java.util.Map;

public record UpdateUserRequest(String status, Map<String, String> metadata) {
}
