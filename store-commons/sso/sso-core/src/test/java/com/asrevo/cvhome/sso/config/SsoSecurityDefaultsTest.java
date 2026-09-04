package com.asrevo.cvhome.sso.config;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.Filter;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;

import com.asrevo.cvhome.sso.security.CsrfCookieFilter;
import com.asrevo.cvhome.sso.session.SessionMaxAgeFilter;
import com.asrevo.cvhome.sso.settings.SettingsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * The parts of the application chain that must be identical in every deployment.
 *
 * <p>
 * Every item asserted here was got wrong once. The CSRF request handler must be the plain attribute one and not
 * the XOR one, because in cua the sign-in form is rendered by the storefront from the cookie value — a page the
 * server never rendered can only send back a token that equals the cookie, and the XOR handler expects one that
 * does not.
 * </p>
 *
 * <p>
 * {@code /login/oauth2/code/*} is exempt from CSRF because Apple posts the authorization code back as a form.
 * {@link CsrfCookieFilter} is added so the cookie is written on every response rather than only the ones that
 * happen to read the token. And the request cache is the one passed in, shared with the authorization server's
 * chain, so a login that started at {@code /oauth2/authorize} resumes there whichever chain authenticated it.
 * </p>
 */
class SsoSecurityDefaultsTest {

    private static final String APPLE_CALLBACK = "/login/oauth2/code/*";

    private final RequestCache requestCache = new HttpSessionRequestCache();
    private final CookieCsrfTokenRepository csrfCookies = CookieCsrfTokenRepository.withHttpOnlyFalse();
    private final SettingsService settings = mock(SettingsService.class);
    private final SsoSecurityDefaults defaults =
            new SsoSecurityDefaults(requestCache, csrfCookies, settings);

    @SuppressWarnings("unchecked")
    private final CsrfConfigurer<HttpSecurity> csrf = mock(CsrfConfigurer.class, Answers.RETURNS_SELF);
    @SuppressWarnings("unchecked")
    private final RequestCacheConfigurer<HttpSecurity> cache =
            mock(RequestCacheConfigurer.class, Answers.RETURNS_SELF);
    @SuppressWarnings("unchecked")
    private final OAuth2ResourceServerConfigurer<HttpSecurity> resourceServer =
            mock(OAuth2ResourceServerConfigurer.class, Answers.RETURNS_DEEP_STUBS);

    private final List<Filter> added = new ArrayList<>();

    @Test
    void thecsrfCookieRepositoryAndThePlainRequestHandlerAreBothInstalled() throws Exception {
        defaults.applyTo(http());

        verify(csrf).csrfTokenRepository(csrfCookies);
        // The XOR handler expects a token that differs from the cookie; cua's storefront-rendered form cannot
        // produce one, because it only ever has the cookie to copy.
        verify(csrf).csrfTokenRequestHandler(Mockito.any(CsrfTokenRequestAttributeHandler.class));
    }

    @Test
    void theOauthCallbackIsExemptFromCsrfBecauseApplePostsTheCodeBackAsAform() throws Exception {
        defaults.applyTo(http());

        verify(csrf).ignoringRequestMatchers(APPLE_CALLBACK);
    }

    @Test
    void thecookieTouchingFilterAndTheSessionCeilingAreBothAddedAfterBasicAuth() throws Exception {
        defaults.applyTo(http());

        assertThat(added).hasSize(2)
                .anyMatch(CsrfCookieFilter.class::isInstance)
                .anyMatch(SessionMaxAgeFilter.class::isInstance);
    }

    @Test
    void therequestCacheIsTheOneSharedWithTheAuthorizationServersChain() throws Exception {
        defaults.applyTo(http());

        // Otherwise a login that started at /oauth2/authorize resumes only if the same chain authenticated it.
        verify(cache).requestCache(requestCache);
    }

    @Test
    void thechainAlsoAcceptsBearerTokensSoTheAdminApiWorksForAservice() throws Exception {
        HttpSecurity http = http();

        assertThat(defaults.applyTo(http)).isSameAs(http);
        verify(resourceServer).jwt(Mockito.any());
    }

    /**
     * An {@link HttpSecurity} whose customizer-taking methods actually run their customizer against a mocked
     * configurer, and which records the filters added, so the wiring can be asserted without booting a context.
     */
    @SuppressWarnings("unchecked")
    private HttpSecurity http() {
        return mock(HttpSecurity.class, invocation -> {
            Object[] args = invocation.getArguments();
            switch (invocation.getMethod().getName()) {
                case "csrf" -> ((Customizer<CsrfConfigurer<HttpSecurity>>) args[0]).customize(csrf);
                case "requestCache" -> ((Customizer<RequestCacheConfigurer<HttpSecurity>>) args[0]).customize(cache);
                case "oauth2ResourceServer" ->
                        ((Customizer<OAuth2ResourceServerConfigurer<HttpSecurity>>) args[0]).customize(resourceServer);
                case "addFilterAfter" -> {
                    assertThat(args[1]).isEqualTo(BasicAuthenticationFilter.class);
                    added.add((Filter) args[0]);
                }
                default -> {
                    // Nothing else is called; returning the builder is enough.
                }
            }
            return invocation.getMock();
        });
    }

}
