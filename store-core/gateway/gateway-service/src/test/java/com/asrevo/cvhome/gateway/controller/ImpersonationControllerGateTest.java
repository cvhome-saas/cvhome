package com.asrevo.cvhome.gateway.controller;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.asrevo.cvhome.gateway.errors.SessionRequiredException;
import com.asrevo.cvhome.gateway.impersonation.ImpersonationService;
import com.asrevo.cvhome.gateway.impersonation.ImpersonationView;
import com.asrevo.cvhome.gateway.impersonation.StartImpersonation;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * The session gate on every impersonation handler.
 *
 * <p>
 * The gateway's chain permits every exchange and enables no method security, so the controller's own
 * {@code operator()} is the whole gate: a signed-in OAuth2 session, or {@code GATEWAY.SESSION.REQUIRED} before the
 * service is asked anything. Pinned per handler because CSRF is disabled on the gateway (authorization audit,
 * A11): the JSON preflight and the Lax session cookie keep a cross-site caller from having a session, and this is
 * what having no session buys them.
 * </p>
 */
class ImpersonationControllerGateTest {

    private static final String ROLE = "ROLE_SUPER_ADMIN";

    private static final String TOKEN = "t";

    private final ImpersonationService impersonation = mock(ImpersonationService.class);

    private final ImpersonationController controller = new ImpersonationController(impersonation);

    private final MockServerWebExchange exchange =
            MockServerWebExchange.from(MockServerHttpRequest.post("/api/v1/impersonation").build());

    private final StartImpersonation request = new StartImpersonation(TOKEN, "ticket");

    /** Flipped only if something subscribes to the service's answer. */
    private final AtomicBoolean subscribed = new AtomicBoolean();

    @BeforeEach
    void stubTheService() {
        // current() and end() are assembled with .then(service...), so the method itself is called while the
        // reactive chain is built, whatever the session says. What must not happen is a subscription: that is
        // where the work would be. A recording Mono is how the difference is asserted rather than assumed.
        Mono<ImpersonationView> recorded = Mono.<ImpersonationView>empty()
                .doOnSubscribe(subscription -> subscribed.set(true));
        when(impersonation.current(any())).thenReturn(recorded);
        when(impersonation.end(any())).thenReturn(recorded.then());
    }

    private void refusesWithoutTouchingTheService(Mono<?> handler) {
        StepVerifier.create(handler).expectError(SessionRequiredException.class).verify();

        assertThat(subscribed).isFalse();
    }

    @Test
    void startWithoutAsessionIs401AndTheServiceIsNeverAsked() {
        StepVerifier.create(controller.start(exchange, request))
                .expectError(SessionRequiredException.class)
                .verify();

        // start() is a flatMap, so the service is not even reached for its Mono.
        verifyNoInteractions(impersonation);
    }

    @Test
    void currentWithoutAsessionIs401AndNothingIsSubscribed() {
        refusesWithoutTouchingTheService(controller.current(exchange));
    }

    @Test
    void endWithoutAsessionIs401AndNothingIsSubscribed() {
        refusesWithoutTouchingTheService(controller.end(exchange));
    }

    /** An anonymous authentication is a security context, not a session. */
    @Test
    void anAnonymousContextIsNoSession() {
        AnonymousAuthenticationToken anonymous = new AnonymousAuthenticationToken("key", "anonymous",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));

        StepVerifier.create(controller.start(exchange, request)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(anonymous)))
                .expectError(SessionRequiredException.class)
                .verify();

        verifyNoInteractions(impersonation);
    }

    /** A bearer token is a backend's credential, not a gateway session — even a super admin's. */
    @Test
    void aBearerTokenIsNoSessionEvenForAsuperAdmin() {
        Jwt jwt = Jwt.withTokenValue(TOKEN).header("alg", "none").subject("s")
                .issuedAt(Instant.EPOCH).expiresAt(Instant.EPOCH.plusSeconds(60))
                .claim("roles", List.of(ROLE)).build();
        JwtAuthenticationToken bearer = new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority(ROLE)));

        refusesWithoutTouchingTheService(controller.end(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(bearer)));
        refusesWithoutTouchingTheService(controller.current(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(bearer)));
    }

}
