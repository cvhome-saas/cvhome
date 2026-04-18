package com.asrevo.cvhome.uaa.dto;

import java.util.Set;

import com.asrevo.cvhome.uaa.service.ClientAuthMethod;
import com.asrevo.cvhome.uaa.service.OAuthGrantType;

public record ClientDetails(String id, String clientId, String clientName,
                            Set<ClientAuthMethod> clientAuthenticationMethods, Set<OAuthGrantType> authorizationGrantTypes,
                            Set<String> redirectUris, Set<String> postLogoutRedirectUris, Set<String> scopes,
                            ClientDetailsSettings clientSettings, ClientDetailsTokens tokenSettings) {
}
