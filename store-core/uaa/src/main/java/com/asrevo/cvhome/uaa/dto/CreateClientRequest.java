package com.asrevo.cvhome.uaa.dto;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;

import com.asrevo.cvhome.uaa.service.ClientAuthMethod;
import com.asrevo.cvhome.uaa.service.OAuthGrantType;

public record CreateClientRequest(String clientId, @NotBlank String clientName, Set<String> redirectUris,
                                  Set<String> scopes, Set<OAuthGrantType> grantTypes, // standardized enum grant
                                  // types
                                  Set<ClientAuthMethod> authMethods, // standardized enum client auth methods
                                  Boolean requirePkce, Boolean requireConsent) {
}
