package com.asrevo.cvhome.uaa.idp;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.asrevo.cvhome.crypto.EncryptedValue;
import com.asrevo.cvhome.crypto.SecretCryptoProvider;
import com.asrevo.cvhome.uaa.domain.IdentityProvider;
import com.asrevo.cvhome.uaa.dto.IdentityProviderDto;
import com.asrevo.cvhome.uaa.dto.IdentityProviderRequest;

import lombok.RequiredArgsConstructor;

/**
 * Between the request, the row and the DTO. The client id and secret are envelopes in the row; this is the one place
 * that opens them, and the DTO never carries the secret — only whether one is set.
 */
@Component
@RequiredArgsConstructor
public class IdentityProviderMapper {

    private static final String COMMA = ",";

    private static final String EQUALS = "=";

    private final SecretCryptoProvider crypto;

    public IdentityProvider toNewEntity(IdentityProviderRequest req, int sortOrder, Instant now) {
        IdentityProvider p = new IdentityProvider();
        p.setId(UUID.randomUUID());
        p.setAlias(req.alias().trim().toLowerCase(Locale.ROOT));
        p.setPreset(req.preset());
        p.setType(req.preset().type());
        p.setSortOrder(sortOrder);
        p.setCreatedAt(now);
        apply(p, req, now);
        return p;
    }

    /** Applies every writable field; a blank {@code clientSecret} keeps the stored one. */
    public void apply(IdentityProvider p, IdentityProviderRequest req, Instant now) {
        IdpPreset preset = p.getPreset();
        p.setDisplayName(StringUtils.hasText(req.displayName()) ? req.displayName().trim() : preset.displayName());
        p.setHideOnLogin(Boolean.TRUE.equals(req.hideOnLogin()));
        p.setClientIdEnc(encrypt(req.clientId().trim()));
        if (StringUtils.hasText(req.clientSecret())) {
            p.setClientSecretEnc(encrypt(req.clientSecret().trim()));
        }
        p.setIssuerUri(firstText(req.issuerUri(), preset.issuerUri()));
        p.setAuthorizationUri(firstText(req.authorizationUri(), preset.authorizationUri()));
        p.setTokenUri(firstText(req.tokenUri(), preset.tokenUri()));
        p.setUserInfoUri(firstText(req.userInfoUri(), preset.userInfoUri()));
        p.setJwkSetUri(firstText(req.jwkSetUri(), preset.jwkSetUri()));
        p.setScopes(req.scopes() == null || req.scopes().isEmpty() ? preset.scopes() : String.join(" ", req.scopes()));
        p.setUserNameAttribute(firstText(req.userNameAttribute(), preset.userNameAttribute()));
        p.setClientAuthMethod(firstText(req.clientAuthMethod(), preset.clientAuthMethod()));
        p.setEmailDomains(joinLower(req.emailDomains()));
        p.setAccountLinking(req.accountLinking() == null ? p.getAccountLinking() : req.accountLinking());
        p.setJitProvisioning(Boolean.TRUE.equals(req.jitProvisioning()));
        // Role names are case-sensitive identifiers, and they are the claim: SUPER_ADMIN is not super_admin.
        p.setDefaultRoles(join(req.defaultRoles()));
        p.setTrustEmailVerified(req.trustEmailVerified() == null || req.trustEmailVerified());
        Map<String, String> mapping = req.attributeMapping() == null || req.attributeMapping().isEmpty()
                ? preset.attributeMapping() : req.attributeMapping();
        p.setAttributeMapping(mapping.entrySet().stream()
                .map(e -> e.getKey().trim() + EQUALS + e.getValue().trim()).collect(Collectors.joining(COMMA)));
        p.setUpdatedAt(now);
    }

    public IdentityProviderDto toDto(IdentityProvider p, String redirectUri) {
        return new IdentityProviderDto(p.getId(), p.getAlias(), p.getDisplayName(), p.getType(), p.getPreset(), p.isEnabled(),
                p.isHideOnLogin(), p.getSortOrder(), clientId(p), p.getClientSecretEnc() != null, p.getIssuerUri(),
                p.getAuthorizationUri(), p.getTokenUri(), p.getUserInfoUri(), p.getJwkSetUri(), scopes(p),
                p.getUserNameAttribute(), p.getClientAuthMethod(), split(p.getEmailDomains()), p.getAccountLinking(),
                p.isJitProvisioning(), split(p.getDefaultRoles()), p.isTrustEmailVerified(), mapping(p), redirectUri,
                p.getCreatedAt(), p.getUpdatedAt());
    }

    public String clientId(IdentityProvider p) {
        return decrypt(p.getClientIdEnc());
    }

    public String clientSecret(IdentityProvider p) {
        return p.getClientSecretEnc() == null ? null : decrypt(p.getClientSecretEnc());
    }

    public static List<String> scopes(IdentityProvider p) {
        return p.getScopes() == null || p.getScopes().isBlank() ? List.of()
                : Arrays.stream(p.getScopes().trim().split("\\s+")).toList();
    }

    public static List<String> split(String csv) {
        return csv == null || csv.isBlank() ? List.of()
                : Arrays.stream(csv.split(COMMA)).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    /** {@code providerClaim → userAttribute}, in the order stored. */
    public static Map<String, String> mapping(IdentityProvider p) {
        Map<String, String> out = new LinkedHashMap<>();
        for (String pair : split(p.getAttributeMapping())) {
            int at = pair.indexOf(EQUALS);
            if (at > 0) {
                out.put(pair.substring(0, at).trim(), pair.substring(at + 1).trim());
            }
        }
        return out;
    }

    private static String join(List<String> values) {
        return values == null || values.isEmpty() ? null
                : values.stream().map(String::trim).filter(v -> !v.isEmpty()).collect(Collectors.joining(COMMA));
    }

    /** Domains are compared case-insensitively, so they are stored folded. */
    private static String joinLower(List<String> values) {
        return values == null || values.isEmpty() ? null
                : values.stream().map(String::trim).map(v -> v.toLowerCase(Locale.ROOT)).filter(v -> !v.isEmpty())
                        .collect(Collectors.joining(COMMA));
    }

    private static String firstText(String requested, String fallback) {
        return StringUtils.hasText(requested) ? requested.trim() : fallback;
    }

    private String encrypt(String plain) {
        return crypto.encrypt(plain.getBytes(StandardCharsets.UTF_8)).serialize();
    }

    private String decrypt(String envelope) {
        return new String(crypto.decrypt(EncryptedValue.deserialize(envelope)), StandardCharsets.UTF_8);
    }

}
