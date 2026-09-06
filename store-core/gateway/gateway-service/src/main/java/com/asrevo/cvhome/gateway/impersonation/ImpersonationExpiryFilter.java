package com.asrevo.cvhome.gateway.impersonation;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import lombok.RequiredArgsConstructor;

import reactor.core.publisher.Mono;

/**
 * Hands an expired impersonation back to the operator before the request is looked at.
 *
 * <p>
 * Its own clock, on every request, rather than a check at issuance: the ceiling is what makes an impersonation
 * something that ends, and it must end even if the operator never clicks anything. Ahead of the security chain
 * so the context that chain loads is already the restored one; and it never falls through to the merchant,
 * because past the ceiling there is no refresh token and {@code tokenRelay()} would forward a dead token.
 * </p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class ImpersonationExpiryFilter implements WebFilter {

    private final ImpersonationService impersonation;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return impersonation.expireIfDue(exchange).then(chain.filter(exchange));
    }

}
