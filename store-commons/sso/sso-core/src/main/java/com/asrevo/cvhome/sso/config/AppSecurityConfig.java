package com.asrevo.cvhome.sso.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.asrevo.cvhome.sso.settings.SettingsService;

/**
 * The security every deployment shares: the public-API chain, and the pieces its own chain is assembled from.
 *
 * <p>
 * The application chain itself belongs to the shell, because that is where the two deployments genuinely differ —
 * uaa authorizes an admin console and serves its own sign-in page, cua renders no HTML and hands shoppers to their
 * storefront. The mechanisms they must not differ in live in {@link SsoSecurityDefaults}; a shell applies those and
 * then writes only its own rules.
 * </p>
 */
@Configuration
@EnableMethodSecurity
public class AppSecurityConfig {

    private static final String PUBLIC_API = "/api/v1/public/**";

    private static final String API = "/api/**";

    /**
     * The endpoints a one-time link and a storefront call land on: stateless and open, and on their own chain so
     * they never touch the session or the request cache. A registration must not be saved as the request to resume
     * after login, and a failure must be a problem body rather than a redirect to a login page.
     *
     * <p>
     * Identical in both deployments, so it is declared once here rather than copied into each shell.
     * </p>
     */
    @Bean
    @Order(1)
    SecurityFilterChain publicApiSecurity(HttpSecurity http) throws Exception {
        http.securityMatcher(PUBLIC_API)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
        return http.build();
    }

    @Bean
    SsoSecurityDefaults ssoSecurityDefaults(RequestCache requestCache, CookieCsrfTokenRepository csrfCookies,
                                            SettingsService settings) {
        return new SsoSecurityDefaults(requestCache, csrfCookies, settings);
    }

    /**
     * The CSRF cookie, pinned to the root path — see {@link SsoSecurityDefaults} for what an unpinned one did.
     */
    @Bean
    CookieCsrfTokenRepository csrfCookies() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieCustomizer(cookie -> cookie.path("/"));
        return repository;
    }

    /**
     * Saves only GETs that are not the favicon, the error page or an API call, so what the login resumes is the
     * page — or the {@code /oauth2/authorize} — the person was actually going to. Shared with the authorization
     * server's chain so a login on either chain resumes a request saved by the other.
     */
    @Bean
    RequestCache requestCache() {
        HttpSessionRequestCache cache = new HttpSessionRequestCache();
        RequestMatcher getRequests = PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/**");
        RequestMatcher notFavicon = new NegatedRequestMatcher(
                PathPatternRequestMatcher.withDefaults().matcher("/favicon.*"));
        RequestMatcher notError = new NegatedRequestMatcher(PathPatternRequestMatcher.withDefaults().matcher("/error"));
        // An API call is never the page a person was going to: resuming one after login lands them on raw JSON.
        RequestMatcher notApi = new NegatedRequestMatcher(PathPatternRequestMatcher.withDefaults().matcher(API));
        cache.setRequestMatcher(new AndRequestMatcher(getRequests, notFavicon, notError, notApi));
        return cache;
    }

}
