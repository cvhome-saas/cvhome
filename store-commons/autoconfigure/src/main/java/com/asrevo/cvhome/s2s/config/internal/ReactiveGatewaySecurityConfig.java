package com.asrevo.cvhome.s2s.config.internal;

import com.asrevo.cvhome.s2s.config.security.UaaLogoutSuccessHandler;
import com.asrevo.cvhome.s2s.config.security.SecurityContextServerLogoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ReactiveGatewaySecurityConfig {

	@Bean
	public UaaLogoutSuccessHandler logoutSuccessHandler(Environment environment) {
		return new UaaLogoutSuccessHandler(
				environment.getProperty("spring.security.oauth2.client.provider.uaa.end-session-endpoint"));
	}

	@Bean
	public SecurityContextServerLogoutHandler logoutHandler() {
		return new SecurityContextServerLogoutHandler();
	}

}
