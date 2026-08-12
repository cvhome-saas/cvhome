package com.asrevo.cvhome.tenancy.manager.entity;

import java.time.Instant;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.tenancy.commons.dto.InvitationStatus;

import lombok.Getter;
import lombok.Setter;

/**
 * An invitation to join an organization.
 *
 * <p>
 * Holds only the token's hash. The plaintext is returned once, when the invitation is created, and is not
 * recoverable afterwards — the same shape as a password reset, and for the same reason: anyone who can read this
 * table would otherwise be able to accept any outstanding invitation.
 * </p>
 */
@Getter
@Setter
@Table(schema = "tenancy", name = "org_invitation")
public class OrgInvitationEntity extends BaseEntity<OrgInvitationEntity, ManagerStoreId> {

    @Column("org_id")
    private ManagerOrgId orgId;

    @Column("email")
    private String email;

    @Column("role")
    private String role;

    @Column("token_hash")
    private String tokenHash;

    @Column("status")
    private InvitationStatus status;

    @Column("expires_at")
    private Instant expiresAt;

    @Column("created_at")
    private Instant createdAt;

    @Column("created_by")
    private String createdBy;

    @Column("accepted_at")
    private Instant acceptedAt;

    @Column("accepted_by")
    private String acceptedBy;

    public static OrgInvitationEntity create(ManagerOrgId orgId, String email, String role, String tokenHash,
                                             Instant expiresAt, String createdBy) {
        OrgInvitationEntity entity = new OrgInvitationEntity();
        entity.id = entity.generateId();
        entity.orgId = orgId;
        entity.email = email;
        entity.role = role;
        entity.tokenHash = tokenHash;
        entity.status = InvitationStatus.PENDING;
        entity.expiresAt = expiresAt;
        entity.createdAt = Instant.now();
        entity.createdBy = createdBy;
        return entity;
    }

    /** Whether this can still be accepted right now, expiry included. */
    public boolean usable() {
        return status == InvitationStatus.PENDING && expiresAt.isAfter(Instant.now());
    }

    @Override
    protected ManagerStoreId generateId() {
        return ManagerStoreId.newId();
    }

}
