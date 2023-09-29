package com.asrevo.cvhome.domainownership.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequestMapping("api/v1/auth")
@Slf4j
@AllArgsConstructor
public class AuthController {
    @GetMapping("current")
    public Principal current(@AuthenticationPrincipal Principal principal) {
        return principal;
    }

    @GetMapping("me")
    public Mono<JwtAuthenticationToken> me() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .cast(JwtAuthenticationToken.class);
    }
}
