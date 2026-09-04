package com.asrevo.cvhome.cua.security;

import java.io.IOException;

import jakarta.servlet.ServletException;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;

import com.asrevo.cvhome.commons.domain.RealmId;
import com.asrevo.cvhome.sso.realm.RealmContext;
import com.asrevo.cvhome.sso.security.HandoffCsrfDeniedHandler;
import com.asrevo.cvhome.sso.security.HandoffLoginEntryPoint;
import com.asrevo.cvhome.sso.security.HandoffLoginFailureHandler;
import com.asrevo.cvhome.sso.security.LockoutService;
import com.asrevo.cvhome.sso.security.LoginPageLocator;
import com.asrevo.cvhome.sso.session.SessionAdminService;
import com.asrevo.cvhome.sso.session.SessionMetadata;
import com.asrevo.cvhome.sso.settings.RealmSettings;
import com.asrevo.cvhome.sso.settings.SettingsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** The three places cua sends a browser, and that every one of them is the storefront. */
class StorefrontLoginHandlersTest {

    private static final String USER_AGENT = "Mozilla/5.0 (QA)";

    private static final String STORE = "65f023632bc46470c104b76f";

    private static final String GET = "GET";

    private static final String AUTHORIZE = "/oauth2/authorize";

    private static final String LOGIN = "/login";

    private static final String LANG = "lang";

    private static final String EN = "en";

    private static final String XSRF = "XSRF-TOKEN";

    private final RequestCache cache = new HttpSessionRequestCache();

    private final CsrfTokenRepository csrfTokens = CookieCsrfTokenRepository.withHttpOnlyFalse();

    /*
     * The handlers are sso-core's now, shared with uaa; what stays cua's is this one line — the rule that every
     * hand-off lands on the storefront. Binding it here is what these cases are really asserting.
     */
    private final LoginPageLocator loginPages = StorefrontUrls.locator(cache);

    private final MockHttpServletResponse response = new MockHttpServletResponse();

    private final Authentication shopper = new TestingAuthenticationToken("user", "revo");

    private static MockHttpServletRequest request(String method, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setScheme("http");
        request.setServerName("shop.example.com");
        request.setServerPort(80);
        request.setContextPath("/cua");
        return request;
    }

    private static MockHttpServletRequest loginPost(String lang) {
        MockHttpServletRequest login = request("POST", LOGIN);
        login.setParameter(LANG, lang);
        return login;
    }

    @Test
    void theEntryPointSendsTheShopperToTheStorefrontLoginPageMarkedPending() throws IOException {
        MockHttpServletRequest authorize = request(GET, AUTHORIZE);
        authorize.setParameter(LANG, EN);

        new HandoffLoginEntryPoint(loginPages, csrfTokens).commence(authorize, response,
                new BadCredentialsException("anonymous"));

        assertThat(response.getRedirectedUrl()).isEqualTo("http://shop.example.com/en/login?auth=1");
        assertThat(response.getCookie(XSRF)).as("the hand-off plants the CSRF cookie").isNotNull();
    }

    @Test
    void aStaleFormIsSentBackWithAFreshCookieAndTheExpiredToken() throws IOException {
        new HandoffCsrfDeniedHandler(loginPages, cache, csrfTokens)
                .handle(loginPost(EN), response, new org.springframework.security.access.AccessDeniedException("csrf"));

        assertThat(response.getRedirectedUrl()).isEqualTo("http://shop.example.com/en/login?error=expired");
        assertThat(response.getCookie(XSRF)).isNotNull();
    }

    @Test
    void aFailedLoginGoesBackToThePendingPageWithItsErrorToken() throws IOException {
        new HandoffLoginFailureHandler(loginPages, HandoffLoginFailureHandler.INVALID)
                .onAuthenticationFailure(loginPost("ar"), response, new BadCredentialsException("wrong"));

        assertThat(response.getRedirectedUrl()).isEqualTo("http://shop.example.com/ar/login?auth=1&error=invalid");
    }

    /**
     * The real one, wired as cua wires it: it extends the shared handler, so a password sign-in stamps the
     * session and takes the realm's policy exactly as a brokered one does. cua used to extend Spring's handler
     * here and skip all of it — which is why a merchant's session list said "address not recorded" for every row.
     */
    private StorefrontLoginSuccessHandler successHandler() {
        SettingsService settings = mock(SettingsService.class);
        RealmSettings realm = mock(RealmSettings.class);
        when(settings.current()).thenReturn(realm);
        when(realm.sessions()).thenReturn(new RealmSettings.Sessions(1800, 43200, false, 2592000, false));
        return new StorefrontLoginSuccessHandler(settings, mock(SessionAdminService.class), cache);
    }

    @Test
    void aSuccessfulLoginResumesTheSavedAuthorizeRequest() throws IOException, ServletException {
        MockHttpServletRequest authorize = request(GET, AUTHORIZE);
        authorize.setQueryString("client_id=store&lang=en");
        authorize.setParameter("client_id", "store");
        authorize.setParameter(LANG, EN);
        cache.saveRequest(authorize, response);
        MockHttpServletRequest login = loginPost(EN);
        login.setSession(authorize.getSession());

        successHandler().onAuthenticationSuccess(login, response, shopper);

        assertThat(response.getRedirectedUrl()).startsWith("http://shop.example.com/").contains("/oauth2/authorize?")
                .contains("client_id=store");
    }

    /**
     * The session a password sign-in leaves behind.
     *
     * <p>
     * A merchant's session list shows where an account is signed in — address, browser, when it started — and
     * {@code SessionRealmFilter} refuses a session that arrives in another store by reading the realm off it.
     * Both need the stamp, and cua's own handler used to skip it: the list said "address not recorded" for every
     * row, and the realm guard had nothing to compare.
     * </p>
     */
    @Test
    void aPasswordLoginStampsTheSession() throws IOException, ServletException {
        MockHttpServletRequest login = loginPost(EN);
        login.addHeader("User-Agent", USER_AGENT);

        RealmContext.runIn(RealmId.of(STORE), () -> {
            try {
                successHandler().onAuthenticationSuccess(login, response, shopper);
            } catch (IOException | ServletException e) {
                throw new IllegalStateException(e);
            }
        });

        assertThat(login.getSession().getAttribute(SessionMetadata.VIA)).isEqualTo(LockoutService.VIA_PASSWORD);
        assertThat(login.getSession().getAttribute(SessionMetadata.USER_AGENT)).isEqualTo(USER_AGENT);
        assertThat(login.getSession().getAttribute(SessionMetadata.CREATED_AT)).isNotNull();
        assertThat(login.getSession().getAttribute(SessionMetadata.REALM)).isEqualTo(STORE);
        assertThat(login.getSession().getMaxInactiveInterval()).isEqualTo(1800);
    }

    @Test
    void aSuccessfulLoginWithNothingSavedGoesToTheStorefrontWithoutTheMarker() throws IOException, ServletException {
        successHandler().onAuthenticationSuccess(loginPost(EN), response, shopper);

        assertThat(response.getRedirectedUrl()).isEqualTo("http://shop.example.com/en/login");
    }

}
