package com.asrevo.cvhome.commons.domain;

public enum Roles {

	ROLE_SUPER_ADMIN, ROLE_ORG_ADMIN, ROLE_STORE_ADMIN, ROLE_STORE_MODERATOR, ROLE_STORE_RETAIL, ROLE_CUSTOMER,
	SCOPE_STORE, SCOPE_INTERNAL;

	public static Roles parse(String role) {
		try {
			return Roles.valueOf(role);
		}
		catch (IllegalArgumentException e) {
			return null;
		}
	}

}
