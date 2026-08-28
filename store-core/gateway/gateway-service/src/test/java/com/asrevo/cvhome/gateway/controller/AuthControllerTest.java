package com.asrevo.cvhome.gateway.controller;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class AuthControllerTest {

    private static final String USER = "u1";

    private static final String SUB = "sub";

    private final AuthController controller = new AuthController();

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

    @Test
    void meReturnsTheOAuth2LoginFromTheReactiveContext() {
        DefaultOAuth2User user = new DefaultOAuth2User(null, Map.of(SUB, USER), SUB);
        OAuth2AuthenticationToken login = new OAuth2AuthenticationToken(user, null, "uaa");

        StepVerifier.create(controller.me().contextWrite(ReactiveSecurityContextHolder.withAuthentication(login)))
                .expectNext(login)
                .verifyComplete();
    }

    @Test
    void meIsEmptyWithoutASecurityContext() {
        StepVerifier.create(controller.me()).verifyComplete();
    }

}
