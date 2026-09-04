package com.asrevo.cvhome.sso.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.asrevo.cvhome.sso.domain.AccountLinking;
import com.asrevo.cvhome.sso.domain.IdpType;
import com.asrevo.cvhome.sso.idp.IdpPreset;

/**
 * A provider as the console reads it. The client id is plain (it is public in every authorization URL); the secret
 * is never here — only {@code hasClientSecret}. {@code redirectUri} is what to register at the provider.
 */
public record IdentityProviderDto(UUID id, String alias, String displayName, IdpType type, IdpPreset preset, boolean enabled,
                                  boolean hideOnLogin, int sortOrder, String clientId, boolean hasClientSecret,
                                  String issuerUri, String authorizationUri, String tokenUri, String userInfoUri,
                                  String jwkSetUri, List<String> scopes, String userNameAttribute, String clientAuthMethod,
                                  List<String> emailDomains, AccountLinking accountLinking, boolean jitProvisioning,
                                  List<String> defaultRoles, boolean trustEmailVerified, Map<String, String> attributeMapping,
                                  String redirectUri, Instant createdAt, Instant updatedAt) {
}
