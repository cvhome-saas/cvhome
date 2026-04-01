package com.asrevo.cvhome.cua.security;

import com.asrevo.cvhome.cua.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class SecurityUser implements UserDetails, OAuth2AuthenticatedPrincipal {

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

	public SecurityUser(User user) {
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
		this.attributes.put("sub", this.id.toString());
		this.attributes.put("username", this.username);
		this.attributes.put("email", this.email);
		this.attributes.put("given_name", this.firstName);
		this.attributes.put("family_name", this.lastName);
		this.attributes.put("name", this.firstName + " " + this.lastName);
		this.attributes.put("client_id", this.clientId);
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
