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

/**
 * A rotated-out client secret that still authenticates until its grace window closes. Only the hash is kept, exactly
 * as the live secret is; an operator may end the window early, and expiry ends it on its own.
 */
@Entity
@Table(name = "client_secret_history")
@Getter
@Setter
@NoArgsConstructor
public class ClientSecretHistory {

    @Id
    private UUID id;

    /**
     * The realm this row belongs to. Hibernate fills it on insert and adds it to every query; no repository
     * method mentions it. uaa writes one constant value here forever, cua one per store.
     */
    @TenantId
    @Column(name = "realm_id", nullable = false, length = 64)
    private String realmId;

    @Column(name = "registered_client_id", nullable = false, length = 100)
    private String registeredClientId;

    @Column(name = "secret_hash", nullable = false, length = 200)
    private String secretHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    public static ClientSecretHistory retire(String registeredClientId, String secretHash, Instant now, Instant expiresAt) {
        ClientSecretHistory history = new ClientSecretHistory();
        history.id = UUID.randomUUID();
        history.registeredClientId = registeredClientId;
        history.secretHash = secretHash;
        history.createdAt = now;
        history.expiresAt = expiresAt;
        return history;
    }

    /** Still authenticates: not revoked and not past its window. */
    public boolean live(Instant now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }

}
