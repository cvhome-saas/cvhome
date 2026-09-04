package com.asrevo.cvhome.sso.config;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.savedrequest.RequestCache;

import com.asrevo.cvhome.sso.security.CsrfCookieFilter;
import com.asrevo.cvhome.sso.session.SessionMaxAgeFilter;
import com.asrevo.cvhome.sso.settings.SettingsService;

/**
 * The parts of the application chain that must be identical in every deployment.
 *
 * <p>
 * uaa and cua differ in what they authorize and in where they send someone who is not signed in — one serves an
 * admin console, the other hands shoppers to their storefront — so each shell writes its own rules. What they must
 * not differ in is the machinery underneath, and each item below was got wrong once already:
 * </p>
 * <ul>
 * <li>The CSRF cookie is pinned to {@code /}. Without a path it takes the request's directory, so a response from
 * {@code /login/oauth2/code/…} mints a <em>second</em> {@code XSRF-TOKEN} under {@code /login/oauth2}, and the next
 * POST elsewhere sends whichever the browser prefers — a 403 nobody can explain.</li>
 * <li>The request handler is the plain one, not the XOR one, because in cua the form is rendered by the storefront
 * from the cookie value: a page the server never rendered can only fill in a token that equals the cookie.</li>
 * <li>{@code /login/oauth2/code/*} is exempt from CSRF because Apple posts the authorization code back as a form.</li>
 * <li>{@code CsrfCookieFilter} touches the token so the cookie is written on every response, not only the ones that
 * happen to read it.</li>
 * <li>The request cache is shared with the authorization server's chain, so a login that started at
 * {@code /oauth2/authorize} resumes there whichever chain authenticated it.</li>
 * </ul>
 *
 * <p>
 * A shell assembles its chain by calling {@link #applyTo} and then adding its own authorization rules and login
 * handlers.
 * </p>
 */
public class SsoSecurityDefaults {

    private final RequestCache requestCache;

    private final CookieCsrfTokenRepository csrfCookies;

    private final SettingsService settings;

    public SsoSecurityDefaults(RequestCache requestCache, CookieCsrfTokenRepository csrfCookies,
                               SettingsService settings) {
        this.requestCache = requestCache;
        this.csrfCookies = csrfCookies;
        this.settings = settings;
    }

    public HttpSecurity applyTo(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfCookies)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers("/login/oauth2/code/*"))
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                .addFilterAfter(new SessionMaxAgeFilter(settings), BasicAuthenticationFilter.class)
                .requestCache(cache -> cache.requestCache(requestCache))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
    }

}
