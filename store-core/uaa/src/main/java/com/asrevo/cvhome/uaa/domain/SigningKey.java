package com.asrevo.cvhome.uaa.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A key that signs tokens, or used to.
 *
 * <p>
 * Two halves, stored differently on purpose: the public JWK is plain JSON because it is what the JWKS endpoint serves,
 * and the private JWK is a secret-crypto envelope because a private signing key in clear text in a database is a
 * private signing key for anyone who can read the database.
 * </p>
 */
@Entity
@Table(name = "signing_keys")
@Getter
@Setter
@NoArgsConstructor
public class SigningKey {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 190)
    private String kid;

    @Column(nullable = false, length = 20)
    private String algorithm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SigningKeyStatus status;

    @Column(name = "public_jwk_json", nullable = false, columnDefinition = "text")
    private String publicJwkJson;

    @Column(name = "private_jwk_enc", nullable = false, columnDefinition = "text")
    private String privateJwkEnc;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "activated_at")
    private Instant activatedAt;

    @Column(name = "retire_after")
    private Instant retireAfter;

    @Column(name = "retired_at")
    private Instant retiredAt;

    public static SigningKey activate(String kid, String algorithm, String publicJwkJson, String privateJwkEnc, Instant now) {
        SigningKey key = new SigningKey();
        key.id = UUID.randomUUID();
        key.kid = kid;
        key.algorithm = algorithm;
        key.status = SigningKeyStatus.ACTIVE;
        key.publicJwkJson = publicJwkJson;
        key.privateJwkEnc = privateJwkEnc;
        key.createdAt = now;
        key.activatedAt = now;
        return key;
    }

    /** Stops signing; keeps verifying until {@code retireAfter}. */
    public void retiring(Instant retireAfter) {
        this.status = SigningKeyStatus.RETIRING;
        this.retireAfter = retireAfter;
    }

    /** Leaves the JWKS. Tokens it signed no longer verify. */
    public void retire(Instant now) {
        this.status = SigningKeyStatus.RETIRED;
        this.retiredAt = now;
    }

}
