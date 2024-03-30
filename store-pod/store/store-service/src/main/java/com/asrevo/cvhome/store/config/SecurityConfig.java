package com.asrevo.cvhome.store.config;


import com.asrevo.cvhome.s2s.jwt.KeyClockJwtGrantedAuthoritiesConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(it -> it
                        .requestMatchers("api/v1/test/sign").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(it -> it.jwt(Customizer.withDefaults()))
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter converter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        KeyClockJwtGrantedAuthoritiesConverter keyClockJwtGrantedAuthoritiesConverter = new KeyClockJwtGrantedAuthoritiesConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(keyClockJwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}
