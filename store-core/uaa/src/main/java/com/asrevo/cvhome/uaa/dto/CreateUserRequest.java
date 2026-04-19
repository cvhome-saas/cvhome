package com.asrevo.cvhome.uaa.dto;

import java.util.Map;
import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(@NotBlank String username, @Email String email, String firstName, String lastName,
                                Set<String> roles, Map<String, Object> metadata) {
}
