package com.asrevo.cvhome.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;
import reactor.core.publisher.Mono;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher.MatchResult.match;
import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher.MatchResult.notMatch;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    private static Mono<ServerWebExchangeMatcher.MatchResult> matches(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        // @formatter:off
        return (
                request.getMethod() != HttpMethod.GET &&
                        !request.getPath().toString().startsWith("/auth") &&
                        !request.getPath().toString().startsWith("/realms") &&
                        !request.getPath().toString().startsWith("/resources") &&
                        !request.getURI().getHost().startsWith("auth.")
        ) ? match() : notMatch();
        // @formatter:on
    }

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, CookieServerCsrfTokenRepository tokenRepository) {
        // @formatter:off
        return http.authorizeExchange(it ->
                        it.anyExchange().permitAll()
                )
                .oauth2Login(withDefaults())
                .oauth2Client(withDefaults())
                .csrf(it ->
                        it.disable()
//                                .csrfTokenRequestHandler(new ServerCsrfTokenRequestAttributeHandler())
//                                .csrfTokenRepository(tokenRepository)
//                                .requireCsrfProtectionMatcher(SecurityConfig::matches)
                )
                .build();
        // @formatter:on
    }

    @Bean
    public CookieServerCsrfTokenRepository cookieServerCsrfTokenRepository() {
        return CookieServerCsrfTokenRepository.withHttpOnlyFalse();
    }

    @Bean
    public WebSessionIdResolver webSessionIdResolver() {
        CookieWebSessionIdResolver resolver = new CookieWebSessionIdResolver();
        resolver.setCookieName("CORE-GATEWAY-JSESSIONID");
        resolver.addCookieInitializer((builder) -> builder.path("/"));
        return resolver;
    }
}
