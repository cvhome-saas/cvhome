package com.asrevo.cvhome.certificatemanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(it -> it.anyExchange().authenticated())
                .oauth2ResourceServer(it -> it.jwt(withDefaults()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }
}