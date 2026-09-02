package com.asrevo.cvhome.uaa.dto;

import java.util.Set;

import com.asrevo.cvhome.uaa.service.ClientAuthMethod;
import com.asrevo.cvhome.uaa.service.OAuthGrantType;

/**
 * A registration, whole: Spring's settings plus uaa's {@link ClientStatus}. On input only {@code status.description}
 * is read from the status; the rest is the server's to say.
 */
public record ClientDetails(String id, String clientId, String clientName,
                            Set<ClientAuthMethod> clientAuthenticationMethods, Set<OAuthGrantType> authorizationGrantTypes,
                            Set<String> redirectUris, Set<String> postLogoutRedirectUris, Set<String> scopes,
                            ClientDetailsSettings clientSettings, ClientDetailsTokens tokenSettings, ClientStatus status) {

    public ClientDetails withStatus(ClientStatus newStatus) {
        return new ClientDetails(id, clientId, clientName, clientAuthenticationMethods, authorizationGrantTypes,
                redirectUris, postLogoutRedirectUris, scopes, clientSettings, tokenSettings, newStatus);
    }

    public ClientDetails withId(String newId) {
        return new ClientDetails(newId, clientId, clientName, clientAuthenticationMethods, authorizationGrantTypes,
                redirectUris, postLogoutRedirectUris, scopes, clientSettings, tokenSettings, status);
    }

}
