package com.asrevo.cvhome.cua.config;

import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.asrevo.cvhome.cua.security.CustomOAuth2UserService;
import com.asrevo.cvhome.cua.security.CustomOidcUserService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class AppSecurityConfig {

    private static final String LOGIN_PAGE = "/login";

    private static final String ERROR_PAGE = "/error";

    @Bean
    @Order(3)
    SecurityFilterChain appSecurity(HttpSecurity http, JwtDecoder jwtDecoder,
                                    CustomOAuth2UserService customOAuth2UserService, CustomOidcUserService customOidcUserService) {
        http.authorizeHttpRequests(auth -> auth.requestMatchers("/.well-known/**")
                        .permitAll()
                        .requestMatchers(EndpointRequest.toAnyEndpoint())
                        .permitAll()
                        .requestMatchers(LOGIN_PAGE, "/register", "/api/v1/auth/me")
                        .permitAll()
                        /*
                         * Permitted so a failure surfaces as a failure. `/error` was authenticated, so any
                         * exception on a public page became a redirect to `/login` — and when it was the login
                         * page that failed, that redirect looped forever. A shopper clicking "Login" on the
                         * storefront got an endlessly reloading tab and no error anywhere they could see.
                         */
                        .requestMatchers(ERROR_PAGE)
                        .permitAll()
                        .requestMatchers("/swagger-ui.html")
                        .permitAll()
                        .requestMatchers("/swagger-ui/**")
                        .permitAll()
                        .requestMatchers("/v3/api-docs/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .formLogin(it -> it.loginPage(LOGIN_PAGE))
                .oauth2Login(it -> it.loginPage(LOGIN_PAGE)
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService)
                                .oidcUserService(customOidcUserService)))
                .csrf(AbstractHttpConfigurer::disable)
                .requestCache(cache -> cache.requestCache(requestCache()))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder)));
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain customerApiSecurity(HttpSecurity http, JwtDecoder jwtDecoderByIssuerUri) {
        http.securityMatcher("/api/v1/private/social-login-config/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoderByIssuerUri)));
        return http.build();
    }

    @Bean
    public RequestCache requestCache() {
        HttpSessionRequestCache cache = new HttpSessionRequestCache();
        RequestMatcher getRequests = PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/**");
        RequestMatcher notFavicon = new NegatedRequestMatcher(
                PathPatternRequestMatcher.withDefaults().matcher("/favicon.*"));
        RequestMatcher notError = new NegatedRequestMatcher(PathPatternRequestMatcher.withDefaults().matcher(ERROR_PAGE));
        RequestMatcher saveRequestMatcher = new AndRequestMatcher(getRequests, notFavicon, notError);
        cache.setRequestMatcher(saveRequestMatcher);
        return cache;
    }

}
