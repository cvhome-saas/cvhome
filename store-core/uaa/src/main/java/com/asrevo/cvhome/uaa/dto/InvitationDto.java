package com.asrevo.cvhome.uaa.dto;

import java.time.Instant;
import java.util.UUID;

import com.asrevo.cvhome.uaa.domain.InvitationStatus;

/** An invitation as the console lists it. Never carries the token. */
public record InvitationDto(UUID id, UUID userId, String username, String email, InvitationStatus status, Instant expiresAt,
                            Instant createdAt, String createdBy, Instant acceptedAt) {
}
