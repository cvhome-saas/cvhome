package com.asrevo.cvhome.sso.realm;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A user pool this deployment serves.
 *
 * <p>
 * The registry of realms, and deliberately <em>not</em> realm-scoped itself — a row here is what makes a realm
 * exist, so filtering it by the current realm would be circular.
 * </p>
 *
 * <p>
 * uaa holds exactly one row. cua holds one per store, written when the store is provisioned, and that row is what
 * lets cua answer "no such store" instead of serving whoever asked.
 * </p>
 */
@Entity
@Table(name = "realms")
@Data
@NoArgsConstructor
public class Realm {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "display_name", length = 190)
    private String displayName;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Realm(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

}
