package com.asrevo.cvhome.cua.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "signing_keys")
@Data
@NoArgsConstructor
public class SigningKey {

	@Id
	private UUID id;

	@Column(nullable = false, unique = true, length = 190)
	private String kid; // JWK key ID

	@Lob
	@Column(name = "jwk_json", nullable = false)
	private String jwkJson; // full JWK JSON (including private for signing)

	@Column(nullable = false)
	private boolean active = true; // only one should be active at a time

	@Column(nullable = false)
	private Instant createdAt;

	@PrePersist
	public void prePersist() {
		if (id == null)
			id = UUID.randomUUID();
		if (createdAt == null)
			createdAt = Instant.now();
	}

}
