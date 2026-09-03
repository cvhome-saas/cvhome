package com.asrevo.cvhome.sso.idp;

import java.util.List;
import java.util.Map;

import com.asrevo.cvhome.sso.domain.IdpType;

/**
 * The buttons the console draws, and what each fills in. A preset is a set of defaults, not a type of its own: the
 * stored provider keeps the resolved values, so a preset can change without moving a working registration.
 *
 * <p>
 * The well-known providers carry their endpoints here rather than being discovered at every lookup, so a sign-in never
 * waits on a discovery document for Google. The generic OIDC preset discovers from its issuer (cached); the generic
 * OAuth2 preset takes every endpoint by hand. <strong>Apple is scaffolded and unverified:</strong> it needs
 * {@code response_mode=form_post} and a client secret that is an ES256 JWT minted from a developer-account key, which
 * this preset accepts as the stored client secret rather than minting.
 * </p>
 */
public enum IdpPreset {

    GOOGLE(IdpType.OIDC, "Google",
            new Endpoints("https://accounts.google.com", "https://accounts.google.com/o/oauth2/v2/auth",
                    "https://oauth2.googleapis.com/token", "https://openidconnect.googleapis.com/v1/userinfo",
                    "https://www.googleapis.com/oauth2/v3/certs"),
            IdpDefaults.OIDC_SCOPES, IdpDefaults.SUB, IdpDefaults.SECRET_BASIC, IdpDefaults.OIDC_MAPPING),

    /**
     * The {@code common} endpoints, so any Microsoft account may sign in; the id token's issuer is then per tenant,
     * which is why no issuer is pinned — the signature is still checked against the JWKS.
     */
    MICROSOFT(IdpType.OIDC, "Microsoft",
            new Endpoints(null, "https://login.microsoftonline.com/common/oauth2/v2.0/authorize",
                    "https://login.microsoftonline.com/common/oauth2/v2.0/token", "https://graph.microsoft.com/oidc/userinfo",
                    "https://login.microsoftonline.com/common/discovery/v2.0/keys"),
            IdpDefaults.OIDC_SCOPES, IdpDefaults.SUB, IdpDefaults.SECRET_POST, IdpDefaults.OIDC_MAPPING),

    APPLE(IdpType.OIDC, "Apple",
            new Endpoints("https://appleid.apple.com", "https://appleid.apple.com/auth/authorize",
                    "https://appleid.apple.com/auth/token", null, "https://appleid.apple.com/auth/keys"),
            "openid email name", IdpDefaults.SUB, IdpDefaults.SECRET_POST, Map.of(IdpDefaults.EMAIL, IdpDefaults.EMAIL)),

    GITHUB(IdpType.OAUTH2, "GitHub",
            new Endpoints(null, "https://github.com/login/oauth/authorize", "https://github.com/login/oauth/access_token",
                    "https://api.github.com/user", null),
            "read:user user:email", IdpDefaults.ID, IdpDefaults.SECRET_BASIC, IdpDefaults.NAME_MAPPING),

    GENERIC_OIDC(IdpType.OIDC, "OpenID Connect", Endpoints.NONE, IdpDefaults.OIDC_SCOPES, IdpDefaults.SUB,
            IdpDefaults.SECRET_BASIC, IdpDefaults.OIDC_MAPPING),

    GENERIC_OAUTH2(IdpType.OAUTH2, "OAuth 2.0", Endpoints.NONE, "", IdpDefaults.ID, IdpDefaults.SECRET_BASIC,
            IdpDefaults.NAME_MAPPING);

    /** A preset's fixed endpoints; {@link #NONE} for the generic ones the administrator fills in. */
    public record Endpoints(String issuerUri, String authorizationUri, String tokenUri, String userInfoUri, String jwkSetUri) {

        static final Endpoints NONE = new Endpoints(null, null, null, null, null);

    }

    private final IdpType type;

    private final String displayName;

    private final Endpoints endpoints;

    private final String scopes;

    private final String userNameAttribute;

    private final String clientAuthMethod;

    private final Map<String, String> attributeMapping;

    IdpPreset(IdpType type, String displayName, Endpoints endpoints, String scopes, String userNameAttribute,
              String clientAuthMethod, Map<String, String> attributeMapping) {
        this.type = type;
        this.displayName = displayName;
        this.endpoints = endpoints;
        this.scopes = scopes;
        this.userNameAttribute = userNameAttribute;
        this.clientAuthMethod = clientAuthMethod;
        this.attributeMapping = attributeMapping;
    }

    public IdpType type() {
        return type;
    }

    public String displayName() {
        return displayName;
    }

    public String issuerUri() {
        return endpoints.issuerUri();
    }

    public String authorizationUri() {
        return endpoints.authorizationUri();
    }

    public String tokenUri() {
        return endpoints.tokenUri();
    }

    public String userInfoUri() {
        return endpoints.userInfoUri();
    }

    public String jwkSetUri() {
        return endpoints.jwkSetUri();
    }

    public String scopes() {
        return scopes;
    }

    public String userNameAttribute() {
        return userNameAttribute;
    }

    public String clientAuthMethod() {
        return clientAuthMethod;
    }

    public Map<String, String> attributeMapping() {
        return attributeMapping;
    }

    /** Whether the administrator has to supply an issuer (discovery) or the endpoints by hand. */
    public boolean generic() {
        return this == GENERIC_OIDC || this == GENERIC_OAUTH2;
    }

    /** Apple posts the code back, and refuses PKCE. */
    public boolean formPost() {
        return this == APPLE;
    }

    public boolean pkce() {
        return this != APPLE;
    }

    public static List<IdpPreset> catalogue() {
        return List.of(values());
    }

}
