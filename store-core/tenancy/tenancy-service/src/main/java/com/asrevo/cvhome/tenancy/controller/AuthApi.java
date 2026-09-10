package com.asrevo.cvhome.tenancy.controller;

import java.security.Principal;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * "Who am I", as tenancy sees the caller.
 *
 * <p>
 * There used to be a second handler, {@code me}, that returned the whole {@code JwtAuthenticationToken} — every
 * claim of the bearer token, echoed back to whoever holds it. Nothing consumed it: the console asks the gateway's
 * own {@code /api/v1/auth/me}, which answers with a DTO. It is gone rather than gated.
 * </p>
 */
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
     *
     * <p>
     * No permission token: there is no store or organization to check against, only the fact of being signed in.
     * The filter chain already demands that for every non-public path; the gate says so where the handler is read.
     * </p>
     */
    @GetMapping("current")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Principal> current(Authentication authentication) {
        return Optional.ofNullable((Principal) authentication)
                .map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }

}
