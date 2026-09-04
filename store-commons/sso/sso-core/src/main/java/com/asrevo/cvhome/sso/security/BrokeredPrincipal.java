package com.asrevo.cvhome.sso.security;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * What the OAuth2 login filter holds between the provider's answer and the success handler: the local username the
 * broker resolved, plus the provider's attributes. Short-lived by design — the success handler swaps it for the same
 * {@code UsernamePasswordAuthenticationToken} a password login produces, so the authorization server's own
 * serialisation and the token customizer only ever meet one principal shape.
 */
public final class BrokeredPrincipal implements OidcUser, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String username;

    private final String providerAlias;

    private final Map<String, Object> attributes;

    private final transient OidcIdToken idToken;

    private final transient OidcUserInfo userInfo;

    public BrokeredPrincipal(String username, String providerAlias, Map<String, Object> attributes, OidcIdToken idToken,
                             OidcUserInfo userInfo) {
        this.username = username;
        this.providerAlias = providerAlias;
        this.attributes = copyToleratingNulls(attributes);
        this.idToken = idToken;
        this.userInfo = userInfo;
    }

    /**
     * An unmodifiable copy that keeps null values.
     *
     * <p>
     * {@code Map.copyOf} rejects them, and providers send them: GitHub's {@code /user} answers with
     * {@code email}, {@code name}, {@code company} and half a dozen more set to null for any account that has not
     * filled them in, so brokering a GitHub sign-in threw a {@link NullPointerException} before the broker ever
     * saw the attributes. An absent attribute and a null one mean the same thing to everything downstream, but it
     * is the provider's answer that is recorded here rather than a tidied version of it.
     * </p>
     */
    private static Map<String, Object> copyToleratingNulls(Map<String, Object> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return Map.of();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
    }

    public String providerAlias() {
        return providerAlias;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /** Nothing authorises on this: the success handler reloads the real authorities from the account. */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_BROKERED"));
    }

    @Override
    public String getName() {
        return username;
    }

    @Override
    public Map<String, Object> getClaims() {
        return idToken == null ? attributes : idToken.getClaims();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return userInfo;
    }

    @Override
    public OidcIdToken getIdToken() {
        return idToken;
    }

}
