package com.asrevo.cvhome.sso.client;

import java.util.Set;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

/**
 * What kind of client a registration is, derived from how it authenticates and what it may ask for — never stored,
 * so it cannot disagree with the settings it summarises.
 */
public enum ClientType {

    /** Authenticates with {@code none}: no secret, PKCE is the only protection. A browser or native app. */
    PUBLIC,

    /** Holds a secret and asks only for {@code client_credentials}: a service, with no user behind it. */
    MACHINE,

    /** Holds a secret and signs users in. */
    CONFIDENTIAL;

    public static ClientType of(RegisteredClient client) {
        return of(client.getClientAuthenticationMethods(), client.getAuthorizationGrantTypes());
    }

    public static ClientType of(Set<ClientAuthenticationMethod> methods, Set<AuthorizationGrantType> grants) {
        if (methods.size() == 1 && methods.contains(ClientAuthenticationMethod.NONE)) {
            return PUBLIC;
        }
        if (!grants.isEmpty() && grants.stream().allMatch(AuthorizationGrantType.CLIENT_CREDENTIALS::equals)) {
            return MACHINE;
        }
        return CONFIDENTIAL;
    }

    /** Whether the client holds a secret at all — the precondition for rotating one. */
    public boolean holdsSecret() {
        return this != PUBLIC;
    }

}
