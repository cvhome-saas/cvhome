package com.asrevo.cvhome.uaa.sdk.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OAuthGrantType {

	AUTHORIZATION_CODE("authorization_code"), CLIENT_CREDENTIALS("client_credentials"), REFRESH_TOKEN("refresh_token");

	private final String value;

	OAuthGrantType(String value) {
		this.value = value;
	}

	@JsonCreator
	public static OAuthGrantType from(String s) {
		if (s == null)
			throw new IllegalArgumentException("Grant type cannot be null");
		return switch (s.toLowerCase()) {
			case "authorization_code" -> AUTHORIZATION_CODE;
			case "client_credentials" -> CLIENT_CREDENTIALS;
			case "refresh_token" -> REFRESH_TOKEN;
			default -> throw new IllegalArgumentException("Unknown grant type: " + s);
		};
	}

	@JsonValue
	public String value() {
		return value;
	}

}
