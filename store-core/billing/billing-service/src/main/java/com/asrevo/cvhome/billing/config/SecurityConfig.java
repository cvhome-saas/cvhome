package com.asrevo.cvhome.billing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * The service's filter chain. Everything else the web layer needs — the problem-detail advice, the argument
 * resolvers, the permission evaluator — comes from {@code store-commons:autoconfigure} and must not be redeclared
 * here.
 *
 * <p>
 * Two paths are open without a token, for opposite reasons. The plan catalog is public because a pricing page is read
 * by people who have not signed up yet. The Stripe webhook is public because Stripe holds no credential of ours — it
 * authenticates by signing the payload, which the handler verifies before trusting a byte of it.
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
                        .requestMatchers("/api/v1/*/public/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .oauth2ResourceServer(it -> it.jwt(withDefaults()))
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }

}
