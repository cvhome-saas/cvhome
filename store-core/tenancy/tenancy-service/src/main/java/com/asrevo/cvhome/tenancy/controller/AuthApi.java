package com.asrevo.cvhome.tenancy.controller;

import java.security.Principal;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("api/v1/auth")
@Slf4j
@AllArgsConstructor
public class AuthApi {

    /**
     * Who the caller is.
     *
     * <p>
     * Takes the {@link Authentication} rather than {@code @AuthenticationPrincipal Principal}. The principal of a
     * {@code JwtAuthenticationToken} is a {@code Jwt}, which does not implement {@link Principal}, so Spring's
     * resolver handed the method {@code null} and this endpoint answered 401 to a caller who was signed in — the
     * one answer it must never give. {@code Authentication} extends {@code Principal} and is resolved directly.
     * </p>
     */
    @GetMapping("current")
    public ResponseEntity<Principal> current(Authentication authentication) {
        return Optional.ofNullable((Principal) authentication)
                .map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }

    @GetMapping("me")

    public JwtAuthenticationToken me() {
        return (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    }

}
