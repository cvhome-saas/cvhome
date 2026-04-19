package com.asrevo.cvhome.checkout.controller.v1.auth;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
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
public class AuthController {

    @GetMapping("current")

    public ResponseEntity<Jwt> current(JwtAuthenticationToken jwtAuthenticationToken) {
        return Optional.ofNullable((Jwt) jwtAuthenticationToken.getPrincipal())
                .map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }

    @GetMapping("me")

    public JwtAuthenticationToken me() {
        return ((JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication());
    }

}
