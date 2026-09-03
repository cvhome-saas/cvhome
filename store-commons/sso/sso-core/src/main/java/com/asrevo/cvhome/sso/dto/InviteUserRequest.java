package com.asrevo.cvhome.sso.dto;

import java.util.Map;
import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * An invitation: the account is created pending and a one-time link is issued for its first password.
 *
 * @param username optional; the email address when absent, which keeps the JWT {@code sub} stable and readable
 * @param roles    bare role names; an unknown name fails the whole request
 */
public record InviteUserRequest(String username, @NotBlank @Email String email, String firstName, String lastName,
                                Set<String> roles, Map<String, Object> metadata) {
}
