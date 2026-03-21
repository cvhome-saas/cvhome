package com.asrevo.cvhome.uaa.sdk.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ClientAuthMethod {

	CLIENT_SECRET_BASIC("client_secret_basic"), CLIENT_SECRET_POST("client_secret_post"), NONE("none");

	private final String value;

	ClientAuthMethod(String value) {
		this.value = value;
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

}
