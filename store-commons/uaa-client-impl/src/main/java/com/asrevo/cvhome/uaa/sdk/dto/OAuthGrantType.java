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
        if (s == null) {
            throw new IllegalArgumentException("Grant type cannot be null");
        }

        try {
            return OAuthGrantType.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("Unknown grant type: %s", s), e);
        }
    }

    @JsonValue
    public String value() {
        return value;
    }

}
