package com.asrevo.cvhome.tenancy.commons.dto;

import java.time.Instant;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;

/**
 * An invitation as it is safe to show — <strong>without the token</strong>.
 *
 * <p>
 * The token grants membership, so it exists in plaintext exactly once, in the response to whoever created the
 * invitation ({@link CreatedInvitationDto}). Every later read returns this instead. Putting it here would mean
 * anyone who can list invitations can accept them.
 * </p>
 */
public record InvitationDto(String id, ManagerOrgId orgId, String email, String role, InvitationStatus status,
                            Instant expiresAt, Instant createdAt, String createdBy) {
}
