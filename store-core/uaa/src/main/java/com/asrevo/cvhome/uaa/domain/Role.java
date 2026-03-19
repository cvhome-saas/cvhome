package com.asrevo.cvhome.uaa.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
public class Role {

	@Id
	private UUID id;

	@Column(nullable = false, unique = true, length = 80)
	private String name;

	public Role(String name) {
		this.name = name;
	}

	@PrePersist
	public void prePersist() {
		if (id == null)
			id = UUID.randomUUID();
	}

}
