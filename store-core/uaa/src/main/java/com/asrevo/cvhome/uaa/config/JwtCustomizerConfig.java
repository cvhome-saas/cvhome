package com.asrevo.cvhome.uaa.config;

import com.asrevo.cvhome.uaa.domain.Role;
import com.asrevo.cvhome.uaa.domain.User;
import com.asrevo.cvhome.uaa.mapper.ClientClientDetailsMapper;
import com.asrevo.cvhome.uaa.repo.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
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
			if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
				return;
			}

			addClientSettingClaims(context);

			addTokenSettingsClaims(context);

			addUserClaims(context);
		};
	}

	private static void addTokenSettingsClaims(JwtEncodingContext context) {
		var tokenSettings = context.getRegisteredClient().getTokenSettings();
		tokenSettings.getSettings().forEach((s, o) -> {
			if (!ClientClientDetailsMapper.KNOWN_TOKEN_SETTINGS.contains(s)) {
				context.getClaims().claim(s, o);
			}
		});
	}

	private static void addClientSettingClaims(JwtEncodingContext context) {
		var clientSettings = context.getRegisteredClient().getClientSettings();
		clientSettings.getSettings().forEach((s, o) -> {
			if (!ClientClientDetailsMapper.KNOWN_CLIENT_SETTINGS.contains(s)) {
				context.getClaims().claim(s, o);
			}
		});
	}

	private void addUserClaims(JwtEncodingContext context) {

		Authentication principal = context.getPrincipal();
		if (principal == null)
			return;

		String username = principal.getName();
		userRepository.findByUsername(username).ifPresent(user -> {
			// roles
			Set<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
			if (!roles.isEmpty()) {
				context.getClaims().claim("roles", roles);
			}
			user.getMetadata().forEach((s, o) -> context.getClaims().claim(s, o));

		});

	}

}
