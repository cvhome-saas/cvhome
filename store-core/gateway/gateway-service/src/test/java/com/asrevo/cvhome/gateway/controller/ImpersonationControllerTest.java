package com.asrevo.cvhome.gateway.controller;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import com.asrevo.cvhome.gateway.errors.ImpersonationInvalidException;
import com.asrevo.cvhome.gateway.errors.SessionRequiredException;
import com.asrevo.cvhome.gateway.impersonation.ImpersonationService;
import com.asrevo.cvhome.gateway.impersonation.ImpersonationView;
import com.asrevo.cvhome.gateway.impersonation.StartImpersonation;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The session gate is explicit here because the gateway enables no method security; a request with no signed-in
 * session is 401 before the service is asked anything.
 */
class ImpersonationControllerTest {

    private static final String SUB = "sub";

    private static final String TARGET = "t";

    private static final String STORE = "s";

    private static final String READ = "read";

    private static final String TICKET = "ticket";

    private final ImpersonationService impersonation = mock(ImpersonationService.class);

    private final ImpersonationController controller = new ImpersonationController(impersonation);

    private final MockServerWebExchange exchange =
            MockServerWebExchange.from(MockServerHttpRequest.post("/api/v1/impersonation").build());

    private final OAuth2AuthenticationToken login = new OAuth2AuthenticationToken(
            new DefaultOAuth2User(null, Map.of(SUB, "op"), SUB), List.of(), "uaa");

    private final StartImpersonation request = new StartImpersonation(TARGET, STORE, READ, TICKET);

    @Test
    void startWithoutAsessionIs401BeforeTheServiceIsAsked() {
        StepVerifier.create(controller.start(exchange, request)).expectError(SessionRequiredException.class).verify();

        verify(impersonation, never()).start(any(), any(), any());
    }

    @Test
    void startHandsAvalidatedRequestToTheService() {
        ImpersonationView view = new ImpersonationView("m", TARGET, STORE, READ, TICKET, Instant.now());
        when(impersonation.start(eq(exchange), eq(login), any())).thenReturn(Mono.just(view));

        StepVerifier.create(controller.start(exchange, request).contextWrite(ReactiveSecurityContextHolder.withAuthentication(login)))
                .expectNext(view).verifyComplete();
    }

    @Test
    void startRefusesAnIncompleteRequestAsValidation() {
        StepVerifier.create(controller.start(exchange, new StartImpersonation(TARGET, STORE, READ, " "))
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(login)))
                .expectError(ImpersonationInvalidException.class).verify();
    }

    @Test
    void currentIs204WhenTheSessionIsItself() {
        when(impersonation.current(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(controller.current(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(login)))
                .assertNext(response -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT))
                .verifyComplete();
    }

    @Test
    void endIs204AndEndsIt() {
        when(impersonation.end(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(controller.end(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(login)))
                .assertNext(response -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT))
                .verifyComplete();
        verify(impersonation).end(exchange);
    }

}
