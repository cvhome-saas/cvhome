package com.asrevo.cvhome.s2s.config.internal;

import com.asrevo.cvhome.s2s.config.security.KeycloakLogoutSuccessHandler;
import com.asrevo.cvhome.s2s.config.security.SecurityContextServerLogoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;

@Configuration
public class ReactiveGatewaySecurityConfig {

	@Bean
	public KeycloakLogoutSuccessHandler keycloakLogoutSuccessHandler(
			ReactiveClientRegistrationRepository reactiveClientRegistrationRepository) {
		return new KeycloakLogoutSuccessHandler(reactiveClientRegistrationRepository);
	}

	@Bean
	public SecurityContextServerLogoutHandler logoutHandler() {
		return new SecurityContextServerLogoutHandler();
	}

}
