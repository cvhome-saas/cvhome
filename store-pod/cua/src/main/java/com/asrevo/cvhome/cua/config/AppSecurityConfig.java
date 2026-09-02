package com.asrevo.cvhome.cua.config;

import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.asrevo.cvhome.cua.security.CustomOAuth2UserService;
import com.asrevo.cvhome.cua.security.CustomOidcUserService;
import com.asrevo.cvhome.cua.security.StorefrontLoginEntryPoint;
import com.asrevo.cvhome.cua.security.StorefrontLoginFailureHandler;
import com.asrevo.cvhome.cua.security.StorefrontLoginSuccessHandler;
import com.asrevo.cvhome.s2s.jwt.MultiIssuerJwtDecoder;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class AppSecurityConfig {

    private static final String LOGIN_PAGE = "/login";

    private static final String ERROR_PAGE = "/error";

    @Bean
    @Order(3)
    SecurityFilterChain appSecurity(HttpSecurity http, JwtDecoder jwtDecoder, StorefrontLoginEntryPoint storefrontLogin,
                                    CustomOAuth2UserService customOAuth2UserService, CustomOidcUserService customOidcUserService) {
        http.authorizeHttpRequests(auth -> auth.requestMatchers("/.well-known/**")
                        .permitAll()
                        .requestMatchers(EndpointRequest.toAnyEndpoint())
                        .permitAll()
                        .requestMatchers(LOGIN_PAGE, "/api/v1/auth/me")
                        .permitAll()
                        /*
                         * Permitted so a failure surfaces as a failure. `/error` was authenticated, so any
                         * exception on a public path became a redirect to the login page — and when the login
                         * page itself failed (it was rendered here, then) that redirect looped forever. The page
                         * is the storefront's now, but a public endpoint that fails must still answer with a
                         * problem body rather than a redirect.
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
                /*
                 * `loginPage` is kept even though cua renders no login page: it is what stops Spring generating one
                 * and what fixes the processing URL at POST /login, which the storefront's form posts to. Where the
                 * browser actually goes on success and on failure is the handlers' business, and both answer
                 * "the storefront".
                 */
                .formLogin(it -> it.loginPage(LOGIN_PAGE)
                        .successHandler(new StorefrontLoginSuccessHandler(requestCache()))
                        .failureHandler(new StorefrontLoginFailureHandler(requestCache(),
                                StorefrontLoginFailureHandler.INVALID)))
                .oauth2Login(it -> it.loginPage(LOGIN_PAGE)
                        .successHandler(new StorefrontLoginSuccessHandler(requestCache()))
                        .failureHandler(new StorefrontLoginFailureHandler(requestCache(),
                                StorefrontLoginFailureHandler.SOCIAL))
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService)
                                .oidcUserService(customOidcUserService)))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(storefrontLogin))
                .requestCache(cache -> cache.requestCache(requestCache()))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder)));
        return http.build();
    }

    /**
     * The storefront's own endpoints: registration and the social-provider list. Stateless and open, and on their
     * own chain so they never touch the session or the request cache — a registration must not be saved as the
     * request to resume after login, and a failure must be a problem body, never a redirect to a login page.
     */
    @Bean
    @Order(1)
    SecurityFilterChain publicApiSecurity(HttpSecurity http) {
        http.securityMatcher("/api/v1/public/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain customerApiSecurity(HttpSecurity http, MultiIssuerJwtDecoder multiIssuerJwtDecoder) {
        http.securityMatcher("/api/v1/private/social-login-config/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(multiIssuerJwtDecoder)));
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
