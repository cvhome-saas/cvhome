package com.asrevo.cvhome.uaa.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

/**
 * Enum wrapper for supported OAuth2.1 grant types.
 */
public enum OAuthGrantType {

	AUTHORIZATION_CODE("authorization_code", AuthorizationGrantType.AUTHORIZATION_CODE),
	CLIENT_CREDENTIALS("client_credentials", AuthorizationGrantType.CLIENT_CREDENTIALS),
	REFRESH_TOKEN("refresh_token", AuthorizationGrantType.REFRESH_TOKEN);

	private final String value;

	private final AuthorizationGrantType asSpring;

	OAuthGrantType(String value, AuthorizationGrantType asSpring) {
		this.value = value;
		this.asSpring = asSpring;
	}

	@JsonCreator
	public static OAuthGrantType from(String s) {
		if (s == null)
			throw new IllegalArgumentException("Grant type cannot be null");
		return switch (s.toLowerCase()) {
			case "authorization_code" -> AUTHORIZATION_CODE;
			case "client_credentials" -> CLIENT_CREDENTIALS;
			case "refresh_token" -> REFRESH_TOKEN;
			case "implicit" -> throw new IllegalArgumentException("Unsupported grant type: implicit");
			case "password", "resource_owner_password_credentials" ->
				throw new IllegalArgumentException("Unsupported grant type: password");
			default -> throw new IllegalArgumentException("Unknown grant type: " + s);
		};
	}

	@JsonValue
	public String value() {
		return value;
	}

	public AuthorizationGrantType toSpring() {
		return asSpring;
	}

}
