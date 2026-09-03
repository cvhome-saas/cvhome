package com.asrevo.cvhome.sso.dto;

import java.time.Instant;

/**
 * The one response that carries a one-time link. Shown once by the console; no later read can reproduce it.
 *
 * @param invitation the invitation row, or {@code null} for a password-reset link
 */
public record IssuedLink(UserDto user, InvitationDto invitation, String link, Instant expiresAt) {
}
