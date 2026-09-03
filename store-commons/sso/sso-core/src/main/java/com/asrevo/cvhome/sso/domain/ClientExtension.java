package com.asrevo.cvhome.sso.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * What uaa knows about a registered client beyond Spring's own row: whether it may authenticate, a description, and
 * when it last obtained a token. Keyed by the registration id; created with the registration and cascaded away with it.
 */
@Entity
@Table(name = "client_extension")
@Getter
@Setter
@NoArgsConstructor
public class ClientExtension {

    @Id
    @Column(name = "registered_client_id", length = 100)
    private String registeredClientId;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(length = 500)
    private String description;

    @Column(name = "disabled_at")
    private Instant disabledAt;

    @Column(name = "disabled_by", length = 200)
    private String disabledBy;

    @Column(name = "last_token_issued_at")
    private Instant lastTokenIssuedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static ClientExtension create(String registeredClientId, String description, Instant now) {
        ClientExtension extension = new ClientExtension();
        extension.registeredClientId = registeredClientId;
        extension.description = description;
        extension.createdAt = now;
        extension.updatedAt = now;
        return extension;
    }

    public void disable(Instant now, String by) {
        this.enabled = false;
        this.disabledAt = now;
        this.disabledBy = by;
        this.updatedAt = now;
    }

    public void enable(Instant now) {
        this.enabled = true;
        this.disabledAt = null;
        this.disabledBy = null;
        this.updatedAt = now;
    }

}
