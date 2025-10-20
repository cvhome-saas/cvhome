package com.asrevo.cvhome.manager.controller;

import com.asrevo.cvhome.commons.annotation.ApiUsage;
import com.asrevo.cvhome.commons.annotation.ConditionalOnApiStatus;
import java.security.Principal;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/auth")
@Slf4j
@AllArgsConstructor
public class AuthController {
    @GetMapping("current")
    @ConditionalOnApiStatus(usage = ApiUsage.WILL_USED)
    public ResponseEntity<Principal> current(@AuthenticationPrincipal Principal principal) {
        return Optional.ofNullable(principal)
                .map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }

    @GetMapping("me")
    @ConditionalOnApiStatus(usage = ApiUsage.WILL_USED)
    public Mono<JwtAuthenticationToken> me() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .cast(JwtAuthenticationToken.class);
    }
}
