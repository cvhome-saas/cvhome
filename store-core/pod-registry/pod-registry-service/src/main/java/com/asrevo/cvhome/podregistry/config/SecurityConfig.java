package com.asrevo.cvhome.podregistry.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.asrevo.cvhome.s2s.jwt.UaaJwtGrantedAuthoritiesConverter;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * The service's filter chain. The problem-detail advice, argument resolvers and permission evaluator all come from
 * {@code store-commons:autoconfigure} and must not be redeclared here.
 *
 * <p>
 * Nothing in this service is public. A pod's endpoint is infrastructure detail: knowing it tells an attacker exactly
 * which host to aim at, so even the list requires a token. The public-path rule the other services carry — the one
 * matching {@code public} under any v1 resource — is deliberately absent here.
 * </p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(it -> it.requestMatchers("/actuator", "/actuator/*/**")
                        .permitAll()
                        .requestMatchers("/swagger-ui.html", "/webjars/swagger-ui/**", "/v3/api-docs",
                                "/v3/api-docs/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .oauth2ResourceServer(it -> it.jwt(withDefaults()))
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    public JwtAuthenticationConverter converter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        UaaJwtGrantedAuthoritiesConverter uaaJwtGrantedAuthoritiesConverter = new UaaJwtGrantedAuthoritiesConverter();
        converter.setJwtGrantedAuthoritiesConverter(uaaJwtGrantedAuthoritiesConverter);
        return converter;
    }

}
