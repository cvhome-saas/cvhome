package com.asrevo.cvhome.uaa.dto;

import com.asrevo.cvhome.uaa.service.ClientAuthMethod;
import com.asrevo.cvhome.uaa.service.OAuthGrantType;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record CreateClientRequest(String clientId, @NotBlank String clientName, Set<String> redirectUris,
		Set<String> scopes, Set<OAuthGrantType> grantTypes, // standardized enum grant
															// types
		Set<ClientAuthMethod> authMethods, // standardized enum client auth methods
		Boolean requirePkce, Boolean requireConsent) {
}
