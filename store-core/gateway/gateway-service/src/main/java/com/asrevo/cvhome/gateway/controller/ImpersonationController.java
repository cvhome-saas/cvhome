package com.asrevo.cvhome.gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.asrevo.cvhome.gateway.errors.ImpersonationInvalidException;
import com.asrevo.cvhome.gateway.errors.SessionRequiredException;
import com.asrevo.cvhome.gateway.impersonation.ImpersonationService;
import com.asrevo.cvhome.gateway.impersonation.ImpersonationView;
import com.asrevo.cvhome.gateway.impersonation.StartImpersonation;

import lombok.RequiredArgsConstructor;

import reactor.core.publisher.Mono;

/**
 * Acting as a merchant, from the console.
 *
 * <p>
 * On the gateway, not a backend, because the gateway is what holds the session. Platform-scoped like
 * {@link AuthController} beside it: no {@code StoreMerchantId}, no {@code hasPermission} — the real gate is uaa's
 * refusal list, the only place that can see both principals, and the session check here is what any session-bound
 * endpoint on this gateway does. Same CSRF posture as every other gateway POST.
 * </p>
 */
@RestController
@RequestMapping("api/v1/impersonation")
@RequiredArgsConstructor
public class ImpersonationController {

    private final ImpersonationService impersonation;

    /** Starts acting as {@code userId} in {@code storeId}; the answer is what the banner shows. */
    @PostMapping
    public Mono<ImpersonationView> start(ServerWebExchange exchange, @RequestBody StartImpersonation request) {
        return operator().flatMap(operator -> {
            try {
                return impersonation.start(exchange, operator, request.validated());
            } catch (ImpersonationInvalidException invalid) {
                return Mono.error(invalid);
            }
        });
    }

    /** What this session is acting as — 200 with the view, or 204 when it is itself. */
    @GetMapping
    public Mono<ResponseEntity<ImpersonationView>> current(ServerWebExchange exchange) {
        return operator().then(impersonation.current(exchange).map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.noContent().build()));
    }

    /** Ends it. Idempotent: a session that is itself already answers the same 204. */
    @DeleteMapping
    public Mono<ResponseEntity<Void>> end(ServerWebExchange exchange) {
        return operator().then(impersonation.end(exchange)).thenReturn(ResponseEntity.noContent().build());
    }

    /**
     * The signed-in gateway session, or 401. Explicit rather than {@code @PreAuthorize}: the gateway's security
     * chain permits every exchange and enables no method security, so an annotation here would guard nothing.
     */
    private static Mono<OAuth2AuthenticationToken> operator() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(OAuth2AuthenticationToken.class::isInstance)
                .cast(OAuth2AuthenticationToken.class)
                .switchIfEmpty(Mono.error(SessionRequiredException::of));
    }

}
