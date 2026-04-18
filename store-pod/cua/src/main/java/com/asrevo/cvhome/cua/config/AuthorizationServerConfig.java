package com.asrevo.cvhome.cua.config;

import java.util.Objects;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.s2s.model.PodInfoProperties;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
@EnableWebSecurity
public class AuthorizationServerConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    SecurityFilterChain authorizationServerSecurity(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        OAuth2AuthorizationServerConfigurer serverConfigurer = new OAuth2AuthorizationServerConfigurer();
        return http.with(serverConfigurer, configurer -> configurer.oidc(Customizer.withDefaults()))
                .securityMatcher(serverConfigurer.getEndpointsMatcher())
                .authorizeHttpRequests(auth -> auth.requestMatchers(serverConfigurer.getEndpointsMatcher()).authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder)))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> {
                    ex.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"));
                })
                .build();
    }

    @Bean
    DynamicRegisteredClientRepository registeredClientRepository() {
        return new DynamicRegisteredClientRepository();
    }

    @Bean
    AuthorizationServerSettings authorizationServerSettings(PodInfoProperties properties) {
        Pod pod = properties.pod();
        AuthorizationServerSettings.Builder builder = AuthorizationServerSettings.builder();
        if (Objects.nonNull(pod) && Objects.nonNull(pod.endpoint()) && Objects.nonNull(pod.endpoint().endpoint())) {
            builder.issuer(pod.endpoint().endpoint() + "/cua");
        }
        return builder.build();
    }

    @Bean
    JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

}
