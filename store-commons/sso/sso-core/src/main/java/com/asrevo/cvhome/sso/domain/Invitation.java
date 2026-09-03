package com.asrevo.cvhome.sso.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.TenantId;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A pending account's invitation: the hash of a one-time token, and when it stops working.
 *
 * <p>
 * The token itself is never stored, so no read can reconstruct the link; "resend" is a rotation that revokes this
 * row and issues another. A partial unique index keeps one {@code PENDING} row per account.
 * </p>
 */
@Entity
@Table(name = "invitations")
@Getter
@Setter
@NoArgsConstructor
public class Invitation {

    @Id
    private UUID id;

    /**
     * The realm this row belongs to. Hibernate fills it on insert and adds it to every query; no repository
     * method mentions it. uaa writes one constant value here forever, cua one per store.
     */
    @TenantId
    @Column(name = "realm_id", nullable = false, length = 64)
    private String realmId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private InvitationStatus status = InvitationStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by", length = 190)
    private String createdBy;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    public static Invitation issue(User user, String tokenHash, Instant now, Instant expiresAt, String createdBy) {
        Invitation invitation = new Invitation();
        invitation.id = UUID.randomUUID();
        invitation.userId = user.getId();
        invitation.email = user.getEmail();
        invitation.tokenHash = tokenHash;
        invitation.expiresAt = expiresAt;
        invitation.createdAt = now;
        invitation.createdBy = createdBy;
        return invitation;
    }

    public boolean usable(Instant now) {
        return status == InvitationStatus.PENDING && expiresAt.isAfter(now);
    }

}
