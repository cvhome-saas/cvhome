package com.asrevo.cvhome.gateway.controller;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import com.asrevo.cvhome.gateway.impersonation.ImpersonationService;
import com.asrevo.cvhome.gateway.impersonation.ImpersonationView;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    private static final String USER = "u1";

    private static final String SUB = "sub";

    private static final String ROLE = "ROLE_STORE_ADMIN";

    private static final String SOMEONE = "someone";

    private final ImpersonationService impersonation = mock(ImpersonationService.class);

    private final AuthController controller = new AuthController(impersonation);

    private static MockServerWebExchange exchange() {
        return MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/auth/me").build());
    }

    private static OAuth2AuthenticationToken login() {
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(ROLE));
        DefaultOAuth2User user = new DefaultOAuth2User(authorities, Map.of(SUB, USER, "preferred_username", SOMEONE), SUB);
        return new OAuth2AuthenticationToken(user, authorities, "uaa");
    }

    @Test
    void currentIsUnauthorizedWithoutAPrincipal() {
        assertThat(controller.current(null).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void currentEchoesThePrincipal() {
        TestingAuthenticationToken principal = new TestingAuthenticationToken(USER, "p");

        var response = controller.current(principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(principal);
    }

    /** The shape ui-kit's AuthService reads: {@code principal.claims.sub}, {@code preferredUsername}, {@code authorities}. */
    @Test
    void meDescribesTheLoginAndSaysItIsNotImpersonating() {
        when(impersonation.current(any())).thenReturn(Mono.empty());

        StepVerifier.create(controller.me(exchange()).contextWrite(ReactiveSecurityContextHolder.withAuthentication(login())))
                .assertNext(me -> {
                    assertThat(me.principal().claims()).containsEntry(SUB, USER);
                    assertThat(me.principal().name()).isEqualTo(USER);
                    assertThat(me.principal().preferredUsername()).isEqualTo(SOMEONE);
                    assertThat(me.authorities()).extracting(MeView.AuthorityView::authority).containsExactly(ROLE);
                    assertThat(me.impersonation()).isNull();
                })
                .verifyComplete();
    }

    @Test
    void meCarriesTheImpersonationSoAreloadKeepsTheBanner() {
        ImpersonationView acting = new ImpersonationView("org1-store1-admin", "t", "s", "read", "ticket", Instant.now());
        when(impersonation.current(any())).thenReturn(Mono.just(acting));

        StepVerifier.create(controller.me(exchange()).contextWrite(ReactiveSecurityContextHolder.withAuthentication(login())))
                .assertNext(me -> assertThat(me.impersonation()).isEqualTo(acting))
                .verifyComplete();
    }

    @Test
    void meIsEmptyWithoutASecurityContext() {
        StepVerifier.create(controller.me(exchange())).verifyComplete();
    }

}
