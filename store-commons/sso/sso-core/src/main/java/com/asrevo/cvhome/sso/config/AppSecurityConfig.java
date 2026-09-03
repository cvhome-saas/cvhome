package com.asrevo.cvhome.sso.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.asrevo.cvhome.sso.security.BrokeredLogin;
import com.asrevo.cvhome.sso.security.CsrfCookieFilter;
import com.asrevo.cvhome.sso.security.LoginFailureHandler;
import com.asrevo.cvhome.sso.security.LoginSuccessHandler;
import com.asrevo.cvhome.sso.security.ProblemAccessDeniedHandler;
import com.asrevo.cvhome.sso.security.SettingsAwareRememberMeServices;
import com.asrevo.cvhome.sso.session.SessionMaxAgeFilter;
import com.asrevo.cvhome.sso.settings.SettingsService;

/**
 * The two application chains beneath the authorization server's own.
 *
 * <p>
 * <strong>Public API</strong> ({@code /api/v1/public/**}, order 1): stateless, anonymous, no CSRF — the endpoints an
 * invitation or a reset link lands on, which by definition have no session yet. A refused request there is a plain
 * 401, never a redirect to a login page.
 * </p>
 *
 * <p>
 * <strong>Application</strong> (everything else): the SPA, the sign-in form and the admin, account and auth APIs.
 * </p>
 * <ul>
 * <li>Actuator: {@code health}, {@code info} and {@code prometheus} are open (the stack's health check and the scrape
 * need them); every other endpoint — {@code env}, {@code heapdump}, {@code loggers}, {@code sessions} — needs a
 * platform principal. They used to be public, and a heap dump of an authorization server contains its signing key.</li>
 * <li>CSRF is on, with the cookie repository the consoles read: {@code XSRF-TOKEN} in, {@code X-XSRF-TOKEN} back
 * (Angular does this unprompted), and the sign-in form carries {@code _csrf}. The authorization server ignores its own
 * endpoints itself, so token and introspection calls are unaffected.</li>
 * <li>An anonymous call to {@code /api/**} gets a 401, not a redirect, and a refused one a problem+json 403 written
 * directly rather than through the container's error page: the SPA's error stack turns those into the login page or
 * a message, where an HTML page would arrive as a parse error.</li>
 * <li>{@code /logout} accepts GET as well as POST for now: the shared {@code AuthService.logout()} navigates rather
 * than posts. Recorded as a known gap.</li>
 * <li>Sign-in success stamps the session and applies the realm's idle timeout and single-session rule; failure sends
 * the page a reason it can explain; remember-me and the absolute session ceiling follow the realm's settings.</li>
 * <li>Brokered logins ({@code oauth2Login}) use the same login page and the same request cache, so a login that
 * started at {@code /oauth2/authorize} resumes there whichever way the person authenticated. The callback path is
 * exempt from CSRF because Apple posts the code back as a form.</li>
 * </ul>
 */
@Configuration
@EnableMethodSecurity
public class AppSecurityConfig {

    private static final String LOGIN_PAGE = "/login";

    private static final String LOGOUT = "/logout";

    private static final String PUBLIC_API = "/api/v1/public/**";

    private static final String API = "/api/**";

    private static final String LOGOUT_SUCCESS = "/login?logout";

    private static final String SUPER_ADMIN = "ROLE_SUPER_ADMIN";

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
    SecurityFilterChain appSecurity(HttpSecurity http, RequestCache requestCache, ProblemAccessDeniedHandler denied,
                                    LoginSuccessHandler loginSuccess, LoginFailureHandler loginFailure,
                                    SettingsAwareRememberMeServices rememberMe, SettingsService settings,
                                    BrokeredLogin brokered) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/.well-known/**").permitAll()
                        .requestMatchers(EndpointRequest.to("health", "info", "prometheus")).permitAll()
                        .requestMatchers(EndpointRequest.toAnyEndpoint())
                        .hasAnyAuthority("SCOPE_store_core", "SCOPE_STORE_CORE", SUPER_ADMIN)
                        .requestMatchers(LOGIN_PAGE, LOGOUT, "/assets/**", "/media/**", "/img/**", "/webfonts/**", "/js/**",
                                "/css/**", "/*.css", "/*.js", "/favicon.ico")
                        .permitAll()
                        // The pages a one-time link lands on: the SPA route, whose API is the public chain above.
                        .requestMatchers("/accept-invitation", "/reset-password").permitAll()
                        // Brokered logins: the redirect out, the callback in, and the password step that links one.
                        .requestMatchers("/oauth2/authorization/**", "/login/oauth2/**", "/api/v1/auth/link-confirm")
                        .permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs/**").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasAnyAuthority("SCOPE_super_admin", SUPER_ADMIN)
                        .anyRequest().authenticated())
                .formLogin(login -> login.loginPage(LOGIN_PAGE).successHandler(loginSuccess).failureHandler(loginFailure))
                .oauth2Login(login -> login.loginPage(LOGIN_PAGE)
                        .clientRegistrationRepository(brokered.getRegistrations())
                        .authorizationEndpoint(endpoint -> endpoint.authorizationRequestResolver(brokered.resolver()))
                        .userInfoEndpoint(userInfo -> userInfo.userService(brokered.getOauth2Users())
                                .oidcUserService(brokered.getOidcUsers()))
                        .successHandler(brokered.getSuccess())
                        .failureHandler(brokered.getFailure()))
                .rememberMe(remember -> remember.rememberMeServices(rememberMe).key(rememberMe.getKey()))
                .addFilterAfter(new SessionMaxAgeFilter(settings), BasicAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutRequestMatcher(PathPatternRequestMatcher.withDefaults().matcher(LOGOUT))
                        .logoutSuccessUrl(LOGOUT_SUCCESS)
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "SESSION"))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfCookies())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers("/login/oauth2/code/*"))
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(denied)
                        .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                PathPatternRequestMatcher.withDefaults().matcher(API)))
                .requestCache(cache -> cache.requestCache(requestCache))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    /**
     * The CSRF cookie, pinned to the root path.
     *
     * <p>
     * Without an explicit path a cookie takes the request's directory, so a response from {@code /login/oauth2/code/…}
     * mints a <em>second</em> {@code XSRF-TOKEN} scoped to {@code /login/oauth2} — and the next POST elsewhere sends
     * whichever the browser prefers, which is a 403 nobody can explain. Found by the brokered-login test.
     * </p>
     */
    private static CookieCsrfTokenRepository csrfCookies() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieCustomizer(cookie -> cookie.path("/"));
        return repository;
    }

    /**
     * Remember-me, gated by the realm's settings. The key signs the cookie and must be the same on every instance,
     * so it is configuration, not something generated at start.
     */
    @Bean
    SettingsAwareRememberMeServices rememberMeServices(@Value("${com.asrevo.cvhome.uaa.remember-me.key}") String key,
                                                       UserDetailsService userDetailsService, SettingsService settings) {
        return new SettingsAwareRememberMeServices(key, userDetailsService, settings);
    }

    /**
     * Saves only GETs that are not the favicon, the error page or an API call, so what the login resumes is the page — or the
     * {@code /oauth2/authorize} — the person was actually going to. Shared with the authorization server's chain so a
     * login on either chain resumes a request saved by the other.
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
