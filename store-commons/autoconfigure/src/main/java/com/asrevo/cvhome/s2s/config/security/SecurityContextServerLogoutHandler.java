package com.asrevo.cvhome.s2s.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;

import reactor.core.publisher.Mono;

public class SecurityContextServerLogoutHandler {

    public Mono<Void> logout(ServerWebExchange exchange, Authentication authentication) {
        return exchange.getSession().flatMap(WebSession::invalidate);
    }

}
