package com.asrevo.cvhome.uaa.domain.user;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReadableUser extends UserEntity implements Serializable {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private String lastAccess;

	private String loginTime;

	private String org;

	private String store;

	private String userName;

	private boolean active;

	private Set<String> roles = new HashSet<>();

}
