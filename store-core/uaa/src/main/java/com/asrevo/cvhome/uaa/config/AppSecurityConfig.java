package com.asrevo.cvhome.uaa.config;

import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.asrevo.cvhome.s2s.jwt.UaaJwtGrantedAuthoritiesConverter;

@Configuration
@EnableMethodSecurity
public class AppSecurityConfig {

    private static final String LOGIN_PAGE = "/login";

    @Bean
    SecurityFilterChain appSecurity(HttpSecurity http) {
        http.authorizeHttpRequests(auth -> auth.requestMatchers("/.well-known/**")
                        .permitAll()
                        .requestMatchers(EndpointRequest.toAnyEndpoint())
                        .permitAll()
                        .requestMatchers(LOGIN_PAGE, "/assets/**", "/media/**", "/img/**", "/webfonts/**", "/js/**", "/css/**",
                                "/*.css", "/*.js", "/favicon.ico", "/api/v1/me")
                        .permitAll()
                        .requestMatchers("/swagger-ui.html")
                        .permitAll()
                        .requestMatchers("/swagger-ui/**")
                        .permitAll()
                        .requestMatchers("/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers("/api/v1/admin/**")
                        .hasAnyAuthority("SCOPE_super_admin", "ROLE_SUPER_ADMIN")
                        .anyRequest()
                        .authenticated())
                .formLogin(it -> it.loginPage(LOGIN_PAGE))
                .csrf(AbstractHttpConfigurer::disable)
                .requestCache(cache -> cache.requestCache(requestCache()))

                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    /**
     * Maps the {@code roles} claim onto {@code ROLE_*} authorities, the way every other service on the
     * platform does.
     *
     * <p>
     * uaa <em>mints</em> that claim — {@code JwtCustomizerConfig.addUserClaims} writes the user's roles
     * onto every access token — and, as a resource server, never read it back: the default converter
     * maps only {@code scope} to {@code SCOPE_*}. So {@code hasRole('SUPER_ADMIN')} above, and the
     * identical expression on every method of {@code AdminUserController}, could be satisfied by no user
     * token in existence. Only the {@code SCOPE_super_admin} half worked, and only for the
     * client-credentials token the SDK uses service to service — which is why the gap survived: uaa's
     * admin API had exactly one caller, and it came in the one way that worked.
     * </p>
     *
     * <p>
     * The converter is {@code store-commons:autoconfigure}'s, already registered identically by
     * tenancy, billing, pod-registry and the gateway. It only ever <em>adds</em> authorities, so the
     * authorization-server chain is unaffected.
     * </p>
     */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new UaaJwtGrantedAuthoritiesConverter());
        return converter;
    }

    public RequestCache requestCache() {
        HttpSessionRequestCache cache = new HttpSessionRequestCache();
        RequestMatcher getRequests = PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/**");
        RequestMatcher notFavicon = new NegatedRequestMatcher(
                PathPatternRequestMatcher.withDefaults().matcher("/favicon.*"));
        RequestMatcher notError = new NegatedRequestMatcher(PathPatternRequestMatcher.withDefaults().matcher("/error"));
        RequestMatcher saveRequestMatcher = new AndRequestMatcher(getRequests, notFavicon, notError);
        cache.setRequestMatcher(saveRequestMatcher);
        return cache;
    }

}
