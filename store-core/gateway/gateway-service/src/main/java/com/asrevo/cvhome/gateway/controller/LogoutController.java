package com.asrevo.cvhome.gateway.controller;

import com.asrevo.cvhome.s2s.config.internal.ReactiveGatewaySecurityConfig;
import com.asrevo.cvhome.s2s.config.security.KeycloakLogoutSuccessHandler;
import com.asrevo.cvhome.s2s.config.security.SecurityContextServerLogoutHandler;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Controller
@Import(ReactiveGatewaySecurityConfig.class)
public class LogoutController {

	private final KeycloakLogoutSuccessHandler successHandler;

	private final SecurityContextServerLogoutHandler logoutHandler;

	public LogoutController(KeycloakLogoutSuccessHandler successHandler,
			SecurityContextServerLogoutHandler logoutHandler) {
		this.successHandler = successHandler;
		this.logoutHandler = logoutHandler;
	}

	@GetMapping("/logout")
	public Mono<Void> logout(ServerWebExchange exchange,
			@AuthenticationPrincipal OAuth2AuthenticationToken authentication) {
		return this.logoutHandler.logout(exchange, authentication)
			.then(this.successHandler.onLogoutSuccess(exchange, authentication))
			.contextWrite(ReactiveSecurityContextHolder.clearContext());
	}

}
