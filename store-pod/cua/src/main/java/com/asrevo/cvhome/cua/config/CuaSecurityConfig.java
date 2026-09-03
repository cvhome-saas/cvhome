package com.asrevo.cvhome.cua.config;

import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.savedrequest.RequestCache;

import com.asrevo.cvhome.cua.security.StorefrontBrokeredLoginSuccessHandler;
import com.asrevo.cvhome.cua.security.StorefrontCsrfDeniedHandler;
import com.asrevo.cvhome.cua.security.StorefrontLoginEntryPoint;
import com.asrevo.cvhome.cua.security.StorefrontLoginFailureHandler;
import com.asrevo.cvhome.cua.security.StorefrontLoginSuccessHandler;
import com.asrevo.cvhome.s2s.jwt.MultiIssuerJwtDecoder;
import com.asrevo.cvhome.sso.config.SsoSecurityDefaults;
import com.asrevo.cvhome.sso.security.BrokeredLogin;
import com.asrevo.cvhome.sso.security.BrokeredLoginSuccessHandler;
import com.asrevo.cvhome.sso.session.SessionAdminService;
import com.asrevo.cvhome.sso.settings.SettingsService;

/**
 * cua's application chain: a headless authorization server behind a storefront.
 *
 * <p>
 * The difference from uaa is what this file is: cua serves no console and renders no HTML, so there is no admin
 * gate, no static assets and no sign-in page. Everything a shopper sees is the storefront's, and every refusal
 * here is a redirect back to it carrying a machine-readable reason — cua ships no strings, because the storefront
 * owns the translations.
 * </p>
 *
 * <p>
 * The machinery underneath is {@link SsoSecurityDefaults}, shared with uaa, so the two cannot drift apart on CSRF,
 * the request cache, the session ceiling or resource-server decoding.
 * </p>
 */
@Configuration
public class CuaSecurityConfig {

    private static final String LOGIN_PAGE = "/login";

    @Bean
    @Order(3)
    SecurityFilterChain appSecurity(HttpSecurity http, SsoSecurityDefaults defaults, RequestCache requestCache,
                                    CookieCsrfTokenRepository csrfCookies, BrokeredLogin brokered,
                                    BrokeredLoginSuccessHandler brokeredSuccess, SettingsService settings,
                                    SessionAdminService sessions) throws Exception {
        defaults.applyTo(http)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/.well-known/**").permitAll()
                        .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
                        .requestMatchers(LOGIN_PAGE, "/api/v1/auth/me").permitAll()
                        /*
                         * Permitted so a failure surfaces as a failure. /error was authenticated once, so any
                         * exception on a public path became a redirect to the login page — and when the login page
                         * itself failed, that redirect looped forever.
                         */
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/oauth2/authorization/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs/**").permitAll()
                        /*
                         * sso-core's platform administration is no longer denied here: it is claimed by the staff
                         * chain below, which is ordered ahead of this one and authenticates a uaa token. This
                         * chain never sees those paths, so a matcher for them here would be dead code that reads
                         * like a guard.
                         */
                        .anyRequest().authenticated())
                /*
                 * loginPage is kept even though cua renders none: it is what stops Spring generating one, and what
                 * fixes the processing URL at POST /login, which the storefront's form posts to. Where the browser
                 * actually goes on success and on failure is the handlers' business, and both answer "the
                 * storefront".
                 */
                .formLogin(login -> login.loginPage(LOGIN_PAGE)
                        .successHandler(new StorefrontLoginSuccessHandler(settings, sessions, requestCache))
                        .failureHandler(new StorefrontLoginFailureHandler(requestCache,
                                StorefrontLoginFailureHandler.INVALID)))
                .oauth2Login(login -> login.loginPage(LOGIN_PAGE)
                        .clientRegistrationRepository(brokered.getRegistrations())
                        .authorizationEndpoint(endpoint -> endpoint.authorizationRequestResolver(brokered.resolver()))
                        .userInfoEndpoint(userInfo -> userInfo.userService(brokered.getOauth2Users())
                                .oidcUserService(brokered.getOidcUsers()))
                        // Not the plain storefront handler: a brokered login has to swap its BrokeredPrincipal
                        // for the standard one before the authorization server writes it to oauth2_authorization.
                        .successHandler(new StorefrontBrokeredLoginSuccessHandler(requestCache, brokeredSuccess))
                        .failureHandler(new StorefrontLoginFailureHandler(requestCache,
                                StorefrontLoginFailureHandler.SOCIAL)))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(storefrontEntryPoint(requestCache, csrfCookies))
                        .accessDeniedHandler(new StorefrontCsrfDeniedHandler(requestCache, csrfCookies)));
        return http.build();
    }

    /**
     * The staff-facing API: the merchant configuration endpoints, addressed from the console, and sso-core's
     * platform administration. Both are reached with a uaa token — hence the multi-issuer decoder — and this is
     * the one chain in cua that authenticates staff rather than shoppers.
     *
     * <p>
     * The administration endpoints are here rather than denied because a platform operator has to be able to see
     * a pod's own SSO state. Two things bound what that opens. Authentication is a uaa token, so a shopper
     * principal — the only kind cua mints — can never satisfy this chain whatever it presents; and each endpoint
     * keeps its own {@code @PreAuthorize}, so reaching the chain is not reaching the data.
     * </p>
     *
     * <p>
     * They stay realm-scoped. {@code StoreRealmResolver} reads {@code ?store=}, so an operator sees one store's
     * realm at a time and the {@code @TenantId} filter does the scoping — an operator who names no store gets
     * {@code NO_REALM} and therefore nothing, which is the safe direction to fail.
     * </p>
     */
    @Bean
    @Order(2)
    SecurityFilterChain merchantApiSecurity(HttpSecurity http, MultiIssuerJwtDecoder decoder) throws Exception {
        http.securityMatcher("/api/v1/private/**", "/api/v1/admin/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(decoder)));
        return http.build();
    }

    /**
     * Replaces sso-core's default, which sends an unauthenticated {@code /oauth2/authorize} to a sign-in page of
     * its own. cua has none: the shopper goes to their storefront's, with a CSRF cookie planted on the way so the
     * page cua never rendered can post a form cua will accept.
     */
    @Bean("authorizationServerEntryPoint")
    AuthenticationEntryPoint storefrontEntryPoint(RequestCache requestCacheRef,
                                                  CookieCsrfTokenRepository csrfCookies) {
        return new StorefrontLoginEntryPoint(requestCacheRef, csrfCookies);
    }

}
