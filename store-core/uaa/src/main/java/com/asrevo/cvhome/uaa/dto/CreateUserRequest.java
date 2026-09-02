package com.asrevo.cvhome.uaa.dto;

import java.util.Map;
import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * @param password optional; without it the account exists but cannot sign in until a password is set
 * @param roles    bare role names ({@code STORE_ADMIN}); an unknown name fails the whole request
 */
public record CreateUserRequest(@NotBlank String username, @Email String email, String firstName, String lastName,
                                String password, Set<String> roles, Map<String, Object> metadata) {
}
