package com.asrevo.cvhome.cua.security;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.asrevo.cvhome.cua.domain.User;

import lombok.Getter;

@Getter
public class SecurityUser implements UserDetails, OAuth2User, OidcUser, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID id;

    private final String username;

    private final String password;

    private final boolean enabled;

    private final String clientId;

    private final String email;

    private final String firstName;

    private final String lastName;

    private final Map<String, Object> metadata;

    private final Collection<? extends GrantedAuthority> authorities;

    private final Map<String, Object> attributes;

    private final Map<String, Object> claims;

    private final OidcIdToken idToken;

    private final OidcUserInfo userInfo;

    public SecurityUser(User user) {
        this(user, null, null, null);
    }

    public SecurityUser(User user, Map<String, Object> attributes, OidcIdToken idToken, OidcUserInfo userInfo) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPasswordHash();
        this.enabled = user.isEnabled();
        this.clientId = user.getClientId();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.metadata = user.getMetadata();
        // In a real scenario, you might map roles from the user entity.
        // Defaulting to ROLE_USER for now.
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        // Map basic info to attributes for OAuth2AuthenticatedPrincipal
        this.attributes = new HashMap<>();
        if (this.metadata != null) {
            this.attributes.putAll(this.metadata);
        }
        if (attributes != null) {
            this.attributes.putAll(attributes);
        }
        this.attributes.put("sub", this.id.toString());
        this.attributes.put("username", this.username);
        this.attributes.put("email", this.email);
        this.attributes.put("given_name", this.firstName);
        this.attributes.put("family_name", this.lastName);
        this.attributes.put("name", String.format("%s %s", this.firstName, this.lastName));
        this.attributes.put("client_id", this.clientId);

        this.idToken = idToken;
        this.userInfo = userInfo;
        this.claims = (idToken != null) ? idToken.getClaims() : this.attributes;
    }

    @Override
    public Map<String, Object> getClaims() {
        return claims;
    }

    @Override
    public OidcIdToken getIdToken() {
        return idToken;
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return userInfo;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

}
