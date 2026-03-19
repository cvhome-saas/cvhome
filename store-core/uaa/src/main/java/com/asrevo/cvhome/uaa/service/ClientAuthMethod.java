package com.asrevo.cvhome.uaa.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

/**
 * Enum for supported OAuth2 client authentication methods.
 */
public enum ClientAuthMethod {

	CLIENT_SECRET_BASIC("client_secret_basic", ClientAuthenticationMethod.CLIENT_SECRET_BASIC),
	CLIENT_SECRET_POST("client_secret_post", ClientAuthenticationMethod.CLIENT_SECRET_POST),
	NONE("none", ClientAuthenticationMethod.NONE);

	private final String value;

	private final ClientAuthenticationMethod asSpring;

	ClientAuthMethod(String value, ClientAuthenticationMethod asSpring) {
		this.value = value;
		this.asSpring = asSpring;
	}

	@JsonCreator
	public static ClientAuthMethod from(String s) {
		if (s == null)
			throw new IllegalArgumentException("Auth method cannot be null");
		return switch (s.toLowerCase()) {
			case "client_secret_basic" -> CLIENT_SECRET_BASIC;
			case "client_secret_post" -> CLIENT_SECRET_POST;
			case "none" -> NONE;
			default -> throw new IllegalArgumentException("Unknown client auth method: " + s);
		};
	}

	@JsonValue
	public String value() {
		return value;
	}

	public ClientAuthenticationMethod toSpring() {
		return asSpring;
	}

}
