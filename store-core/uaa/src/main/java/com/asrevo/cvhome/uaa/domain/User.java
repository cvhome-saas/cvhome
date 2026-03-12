package com.asrevo.cvhome.uaa.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

	@Id
	private UUID id;

	@Column(nullable = false, unique = true, length = 190)
	private String username;

	@Column(unique = true, length = 254)
	private String email;

	@Column(name = "password_hash", length = 100)
	private String passwordHash;

	@Column(nullable = false, length = 20)
	private String status = "ACTIVE";

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles = new HashSet<>();

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "metadata", columnDefinition = "jsonb")
	private Map<String, Object> metadata = new HashMap<>();

	@PrePersist
	public void prePersist() {
		if (id == null)
			id = UUID.randomUUID();
	}

}
