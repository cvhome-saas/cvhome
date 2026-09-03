package com.asrevo.cvhome.uaa.dto;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import com.asrevo.cvhome.uaa.domain.AccountLinking;
import com.asrevo.cvhome.uaa.idp.IdpPreset;

/**
 * What creates or updates a provider. Blank endpoint fields take the preset's defaults; a blank {@code clientSecret}
 * on an update keeps the stored one. {@code preset} is fixed after creation.
 *
 * @param alias the registration id: lower case, digits and dashes, 2–50 characters, and the redirect URI's last segment
 */
public record IdentityProviderRequest(@NotBlank @Pattern(regexp = "^[a-z0-9-]{2,50}$") String alias, String displayName,
                                      @NotNull IdpPreset preset, Boolean hideOnLogin, @NotBlank String clientId,
                                      String clientSecret, String issuerUri, String authorizationUri, String tokenUri,
                                      String userInfoUri, String jwkSetUri, List<String> scopes, String userNameAttribute,
                                      String clientAuthMethod, List<String> emailDomains, AccountLinking accountLinking,
                                      Boolean jitProvisioning, List<String> defaultRoles, Boolean trustEmailVerified,
                                      Map<String, String> attributeMapping) {
}
