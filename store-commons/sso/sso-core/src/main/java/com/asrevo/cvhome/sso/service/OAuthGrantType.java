package com.asrevo.cvhome.sso.service;

import org.springframework.security.oauth2.core.AuthorizationGrantType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum wrapper for supported OAuth2.1 grant types.
 *
 * <p>
 * {@link #TOKEN_EXCHANGE} is the impersonation grant (RFC 8693, with cvhome's {@code requested_subject} extension —
 * see {@code ImpersonationExchangeProvider}). It is registered on one seeded client, is deliberately absent from the
 * admin console's grant options, and its wire value is a URN rather than a word, which is why {@link #from} matches
 * the value before it tries the name.
 * </p>
 */
public enum OAuthGrantType {

    AUTHORIZATION_CODE("authorization_code", AuthorizationGrantType.AUTHORIZATION_CODE),
    CLIENT_CREDENTIALS("client_credentials", AuthorizationGrantType.CLIENT_CREDENTIALS),
    REFRESH_TOKEN("refresh_token", AuthorizationGrantType.REFRESH_TOKEN),
    TOKEN_EXCHANGE(AuthorizationGrantType.TOKEN_EXCHANGE.getValue(), AuthorizationGrantType.TOKEN_EXCHANGE);

    private final String value;

    private final AuthorizationGrantType asSpring;

    OAuthGrantType(String value, AuthorizationGrantType asSpring) {
        this.value = value;
        this.asSpring = asSpring;
    }

    @JsonCreator
    public static OAuthGrantType from(String s) {
        if (s == null) {
            throw new IllegalArgumentException("Grant type cannot be null");
        }
        for (OAuthGrantType type : values()) {
            if (type.value.equals(s)) {
                return type;
            }
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

    public AuthorizationGrantType toSpring() {
        return asSpring;
    }

}
