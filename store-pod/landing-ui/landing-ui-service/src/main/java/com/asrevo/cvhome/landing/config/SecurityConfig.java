package com.asrevo.cvhome.landing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
  @Bean
  SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    // @formatter:off
        return http
                .authorizeExchange(it ->
                        it
                                .pathMatchers("/actuator", "/actuator/*/**").permitAll()
                                .pathMatchers("swagger-ui.html","webjars/swagger-ui/**","api-docs","api-docs/**").permitAll()
                                .pathMatchers("api/v1/test/sign").permitAll()
                                .pathMatchers("api/v1/public/**").permitAll()
                                .anyExchange().permitAll()
//                                .anyExchange().authenticated()
                )
                .oauth2ResourceServer(it ->
                        it.jwt(withDefaults())
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
        // @formatter:on
  }

  @Bean
  public ReactiveJwtAuthenticationConverter converter() {
    ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
    KeyClockJwtGrantedAuthoritiesConverter keyClockJwtGrantedAuthoritiesConverter = new KeyClockJwtGrantedAuthoritiesConverter();
    converter.setJwtGrantedAuthoritiesConverter(source -> Flux.fromIterable(keyClockJwtGrantedAuthoritiesConverter.convert(source)));
    return converter;
  }
}
