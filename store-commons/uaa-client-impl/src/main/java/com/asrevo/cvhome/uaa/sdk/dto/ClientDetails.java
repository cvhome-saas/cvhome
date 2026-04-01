package com.asrevo.cvhome.uaa.sdk.dto;

import java.util.Set;

public record ClientDetails(String id, String clientId, String clientName,
		Set<ClientAuthMethod> clientAuthenticationMethods, Set<OAuthGrantType> authorizationGrantTypes,
		Set<String> redirectUris, Set<String> postLogoutRedirectUris, Set<String> scopes,
		ClientDetailsSettings clientSettings, ClientDetailsTokens tokenSettings) {
}
