package com.asrevo.cvhome.gateway.controller;

import java.security.Principal;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.asrevo.cvhome.gateway.impersonation.ImpersonationService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import reactor.core.publisher.Mono;

/**
 * Who this session is.
 *
 * <p>
 * {@code me} answers with an explicit {@link MeView} rather than the serialised {@code OAuth2AuthenticationToken} it
 * used to return, because an impersonated session's principal is not an OIDC user and would not have serialised to
 * the shape ui-kit's {@code AuthService} reads. The view names the two fields that service reads today —
 * {@code principal} and {@code authorities} — and adds {@code impersonation}, so the console can render the banner
 * from the one call it already makes, and a reload keeps it.
 * </p>
 */
@RestController
@RequestMapping("api/v1/auth")
@Slf4j
@AllArgsConstructor
public class AuthController {

    private final ImpersonationService impersonation;

    @GetMapping("current")
    public ResponseEntity<Principal> current(@AuthenticationPrincipal Principal principal) {
        return Optional.ofNullable(principal)
                .map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }

    @GetMapping("me")
    public Mono<MeView> me(ServerWebExchange exchange) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(OAuth2AuthenticationToken.class::isInstance)
                .cast(OAuth2AuthenticationToken.class)
                .flatMap(login -> impersonation.current(exchange)
                        .map(acting -> MeView.of(login, acting))
                        .defaultIfEmpty(MeView.of(login, null)));
    }

}
