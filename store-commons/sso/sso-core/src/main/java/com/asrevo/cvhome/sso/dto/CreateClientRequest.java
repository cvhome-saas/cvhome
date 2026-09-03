package com.asrevo.cvhome.sso.dto;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;

import com.asrevo.cvhome.sso.service.ClientAuthMethod;
import com.asrevo.cvhome.sso.service.OAuthGrantType;

public record CreateClientRequest(String clientId, @NotBlank String clientName, Set<String> redirectUris,
                                  Set<String> scopes, Set<OAuthGrantType> grantTypes, // standardized enum grant
                                  // types
                                  Set<ClientAuthMethod> authMethods, // standardized enum client auth methods
                                  Boolean requirePkce, Boolean requireConsent) {
}
