package com.asrevo.cvhome.sso.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.asrevo.cvhome.sso.idp.IdpPreset;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * An external login brokered through uaa. The credentials are envelopes; the mapper is the only thing that opens them.
 */
@Entity
@Table(name = "identity_providers")
@Getter
@Setter
@NoArgsConstructor
public class IdentityProvider {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String alias;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private IdpType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private IdpPreset preset;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "hide_on_login", nullable = false)
    private boolean hideOnLogin;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "client_id_enc", nullable = false, columnDefinition = "text")
    private String clientIdEnc;

    @Column(name = "client_secret_enc", columnDefinition = "text")
    private String clientSecretEnc;

    @Column(name = "issuer_uri", length = 500)
    private String issuerUri;

    @Column(name = "authorization_uri", length = 500)
    private String authorizationUri;

    @Column(name = "token_uri", length = 500)
    private String tokenUri;

    @Column(name = "user_info_uri", length = 500)
    private String userInfoUri;

    @Column(name = "jwk_set_uri", length = 500)
    private String jwkSetUri;

    /** Space-separated. */
    @Column(length = 500)
    private String scopes;

    @Column(name = "user_name_attribute", length = 100)
    private String userNameAttribute;

    @Column(name = "client_auth_method", nullable = false, length = 32)
    private String clientAuthMethod = "client_secret_basic";

    /** Comma-separated, lower case. */
    @Column(name = "email_domains", length = 1000)
    private String emailDomains;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_linking", nullable = false, length = 16)
    private AccountLinking accountLinking = AccountLinking.CONFIRM;

    @Column(name = "jit_provisioning", nullable = false)
    private boolean jitProvisioning;

    /** Comma-separated role names granted at every brokered login. */
    @Column(name = "default_roles", length = 500)
    private String defaultRoles;

    @Column(name = "trust_email_verified", nullable = false)
    private boolean trustEmailVerified = true;

    /** {@code providerClaim=userAttribute} pairs, comma-separated. */
    @Column(name = "attribute_mapping", length = 1000)
    private String attributeMapping;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}
