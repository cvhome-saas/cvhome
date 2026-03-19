package com.asrevo.cvhome.uaa.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record CreateUserRequest(@NotBlank String username, @Email String email, String firstName, String lastName,
		Map<String, Object> metadata) {
}
