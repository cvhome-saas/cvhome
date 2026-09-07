package com.asrevo.cvhome.gateway.controller;

import java.util.Optional;

import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ServerWebExchange;

import com.asrevo.cvhome.gateway.impersonation.ImpersonationService;
import com.asrevo.cvhome.s2s.config.internal.ReactiveGatewaySecurityConfig;
import com.asrevo.cvhome.s2s.config.security.SecurityContextServerLogoutHandler;
import com.asrevo.cvhome.s2s.config.security.UaaLogoutSuccessHandler;

import reactor.core.publisher.Mono;

/**
 * Ends the gateway session, then uaa's.
 *
 * <p>
 * An impersonation is ended <em>first</em>. The end-session redirect needs the operator's ID token for its
 * {@code id_token_hint}, and the swapped principal has none: logging out while acting as a merchant would have
 * ended the gateway session and left uaa's alive, so the next navigation signed the operator straight back in.
 * </p>
 */
@Controller
@Import(ReactiveGatewaySecurityConfig.class)
public class LogoutController {

    private final UaaLogoutSuccessHandler successHandler;

    private final SecurityContextServerLogoutHandler logoutHandler;

    private final ImpersonationService impersonation;

    public LogoutController(UaaLogoutSuccessHandler successHandler, SecurityContextServerLogoutHandler logoutHandler,
                            ImpersonationService impersonation) {
        this.successHandler = successHandler;
        this.logoutHandler = logoutHandler;
        this.impersonation = impersonation;
    }

    @GetMapping("/logout")
    public Mono<Void> logout(ServerWebExchange exchange,
                             @AuthenticationPrincipal OAuth2AuthenticationToken authentication) {
        return this.impersonation.originalOf(exchange)
                .map(Optional::of)
                .defaultIfEmpty(Optional.ofNullable(authentication))
                .flatMap(operator -> {
                    Authentication who = operator.orElse(null);
                    return this.logoutHandler.logout(exchange, who).then(this.successHandler.onLogoutSuccess(exchange, who));
                })
                .contextWrite(ReactiveSecurityContextHolder.clearContext());
    }

}
