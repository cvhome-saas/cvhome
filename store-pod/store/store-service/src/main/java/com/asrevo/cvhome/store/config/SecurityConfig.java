package com.asrevo.cvhome.store.config;


import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.s2s.jwt.KeyClockJwtGrantedAuthoritiesConverter;
import com.asrevo.cvhome.s2s.services.AccessEvaluator;
import com.asrevo.cvhome.s2s.services.AccessEvaluatorImpl;
import com.asrevo.cvhome.s2s.services.StoreSecurityServiceImpl;
import com.asrevo.cvhome.store.service.facade.store.StoreFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {
    private static CorsConfiguration buildCorsConfiguration() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost")); // www - obligatory
        configuration.setAllowedMethods(List.of("OPTIONS", "GET", "POST", "PUT", "DELETE"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return configuration;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(it -> it
                        .requestMatchers("api/v1/test/sign").permitAll()
                        .requestMatchers("api/*/private/**").authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2ResourceServer(it -> it.jwt(Customizer.withDefaults()))
                .csrf(AbstractHttpConfigurer::disable)
                .cors(httpSecurityCorsConfigurer ->
                        httpSecurityCorsConfigurer.configurationSource(request ->
                                buildCorsConfiguration()
                        )
                );
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter converter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        KeyClockJwtGrantedAuthoritiesConverter keyClockJwtGrantedAuthoritiesConverter = new KeyClockJwtGrantedAuthoritiesConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(keyClockJwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    @Lazy
    public AccessEvaluator accessEvaluator(StoreFacade storeFacade) {
        return new AccessEvaluatorImpl(new StoreSecurityServiceImpl((it) ->
                IdentityId.of(storeFacade.get(it.getId().toString()).getOrg())));
    }
/*    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost")); // www - obligatory
//        configuration.setAllowedOrigins(ImmutableList.of("*"));  //set access from all domains
        configuration.setAllowedMethods(List.of("OPTIONS","GET", "POST", "PUT", "DELETE"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }*/
}
