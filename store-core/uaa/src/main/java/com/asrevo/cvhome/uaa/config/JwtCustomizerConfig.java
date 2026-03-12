package com.asrevo.cvhome.uaa.config;

import com.asrevo.cvhome.uaa.domain.Role;
import com.asrevo.cvhome.uaa.domain.User;
import com.asrevo.cvhome.uaa.repo.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class JwtCustomizerConfig {

	private final UserRepository userRepository;

	public JwtCustomizerConfig(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Bean
	OAuth2TokenCustomizer<JwtEncodingContext> oauth2TokenCustomizer() {
		return context -> {
			// Only enrich access tokens
			if (!"access_token".equals(context.getTokenType().getValue())) {
				return;
			}

			Authentication principal = context.getPrincipal();
			if (principal == null)
				return;

			String username = principal.getName();
			userRepository.findByUsername(username).ifPresent(user -> addUserClaims(context, user));
		};
	}

	private void addUserClaims(JwtEncodingContext context, User user) {
		// roles
		Set<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
		if (!roles.isEmpty()) {
			context.getClaims().claim("roles", roles);
		}
	}

}
