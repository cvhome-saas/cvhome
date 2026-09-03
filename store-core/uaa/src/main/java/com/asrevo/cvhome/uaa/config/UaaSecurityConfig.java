package com.asrevo.cvhome.uaa.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import com.asrevo.cvhome.sso.config.SsoSecurityDefaults;
import com.asrevo.cvhome.sso.security.BrokeredLogin;
import com.asrevo.cvhome.sso.security.LoginFailureHandler;
import com.asrevo.cvhome.sso.security.LoginSuccessHandler;
import com.asrevo.cvhome.sso.security.ProblemAccessDeniedHandler;
import com.asrevo.cvhome.sso.security.SettingsAwareRememberMeServices;
import com.asrevo.cvhome.sso.settings.SettingsService;

/**
 * uaa's application chain: what a platform administrator is allowed to reach, and how they sign in.
 *
 * <p>
 * It lives in the shell rather than in sso-core because this is the half that genuinely differs between the two
 * deployments — uaa serves an admin console and its own sign-in page, cua serves neither. The machinery both must
 * share (CSRF, the request cache, the session ceiling, resource-server decoding) comes from
 * {@link SsoSecurityDefaults}, so the two cannot drift apart on anything security-critical.
 * </p>
 *
 * <ul>
 * <li>Actuator is narrowed: health, info and prometheus are open, everything else needs a platform principal.</li>
 * <li>An anonymous call to {@code /api/**} gets a 401 rather than a redirect, and a refused one a problem+json 403
 * written directly: the SPA turns those into a login page or a message, where HTML arrives as a parse error.</li>
 * <li>{@code /logout} accepts GET as well as POST for now, because the shared {@code AuthService.logout()}
 * navigates rather than posts. Recorded as a known gap.</li>
 * <li>Brokered logins use the same login page and request cache, so a login that started at
 * {@code /oauth2/authorize} resumes there whichever way the person authenticated.</li>
 * </ul>
 */
@Configuration
public class UaaSecurityConfig {

    private static final String LOGIN_PAGE = "/login";

    private static final String LOGOUT = "/logout";

    private static final String API = "/api/**";

    private static final String LOGOUT_SUCCESS = "/login?logout";

    private static final String SUPER_ADMIN = "ROLE_SUPER_ADMIN";

    @Bean
    SecurityFilterChain appSecurity(HttpSecurity http, SsoSecurityDefaults defaults, ProblemAccessDeniedHandler denied,
                                    LoginSuccessHandler loginSuccess, LoginFailureHandler loginFailure,
                                    SettingsAwareRememberMeServices rememberMe, BrokeredLogin brokered)
            throws Exception {
        defaults.applyTo(http)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/.well-known/**").permitAll()
                        .requestMatchers(EndpointRequest.to("health", "info", "prometheus")).permitAll()
                        .requestMatchers(EndpointRequest.toAnyEndpoint())
                        .hasAnyAuthority("SCOPE_store_core", "SCOPE_STORE_CORE", SUPER_ADMIN)
                        .requestMatchers(LOGIN_PAGE, LOGOUT, "/assets/**", "/media/**", "/img/**", "/webfonts/**",
                                "/js/**", "/css/**", "/*.css", "/*.js", "/favicon.ico")
                        .permitAll()
                        // The pages a one-time link lands on: the SPA route, whose API is the public chain.
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
                .logout(logout -> logout
                        .logoutRequestMatcher(PathPatternRequestMatcher.withDefaults().matcher(LOGOUT))
                        .logoutSuccessUrl(LOGOUT_SUCCESS)
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "SESSION"))
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(denied)
                        .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                PathPatternRequestMatcher.withDefaults().matcher(API)));
        return http.build();
    }

    /**
     * Remember-me, gated by the realm's settings. The key signs the cookie and must be the same on every instance,
     * so it is configuration rather than something generated at start.
     */
    @Bean
    SettingsAwareRememberMeServices rememberMeServices(@Value("${com.asrevo.cvhome.uaa.remember-me.key}") String key,
                                                       UserDetailsService userDetailsService, SettingsService settings) {
        return new SettingsAwareRememberMeServices(key, userDetailsService, settings);
    }

}
