package com.asrevo.cvhome.sso.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.TenantId;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Which external identity (provider + subject) signs in as which account. */
@Entity
@Table(name = "user_identities")
@Getter
@Setter
@NoArgsConstructor
public class UserIdentity {

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

    @Column(name = "provider_id", nullable = false)
    private UUID providerId;

    @Column(nullable = false, length = 255)
    private String subject;

    @Column(length = 255)
    private String email;

    @Column(name = "linked_at", nullable = false)
    private Instant linkedAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    public static UserIdentity link(UUID userId, UUID providerId, String subject, String email, Instant now) {
        UserIdentity identity = new UserIdentity();
        identity.id = UUID.randomUUID();
        identity.userId = userId;
        identity.providerId = providerId;
        identity.subject = subject;
        identity.email = email;
        identity.linkedAt = now;
        identity.lastLoginAt = now;
        return identity;
    }

}
