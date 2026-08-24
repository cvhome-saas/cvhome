package com.asrevo.cvhome.inventory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.asrevo.cvhome.s2s.jwt.UaaJwtGrantedAuthoritiesConverter;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) {
        http.authorizeHttpRequests(
                        it -> it.requestMatchers("/api/*/private/**").authenticated().anyRequest().permitAll())
                .oauth2ResourceServer(it -> it.jwt(Customizer.withDefaults()))
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter converter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        UaaJwtGrantedAuthoritiesConverter uaaJwtGrantedAuthoritiesConverter = new UaaJwtGrantedAuthoritiesConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(uaaJwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

}
