package com.asrevo.cvhome.s2s.config.internal;

import com.asrevo.cvhome.s2s.config.security.KeycloakLogoutSuccessHandler;
import com.asrevo.cvhome.s2s.config.security.SecurityContextServerLogoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ReactiveGatewaySecurityConfig {

	@Bean
	public KeycloakLogoutSuccessHandler keycloakLogoutSuccessHandler(Environment environment) {
		return new KeycloakLogoutSuccessHandler(
				environment.getProperty("spring.security.oauth2.client.provider.keycloak.end-session-endpoint"));
	}

	@Bean
	public SecurityContextServerLogoutHandler logoutHandler() {
		return new SecurityContextServerLogoutHandler();
	}

}
