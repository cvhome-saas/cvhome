package com.asrevo.cvhome.cua.security;

import java.io.IOException;

import jakarta.servlet.ServletException;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;

import static org.assertj.core.api.Assertions.assertThat;

/** The three places cua sends a browser, and that every one of them is the storefront. */
class StorefrontLoginHandlersTest {

    private static final String GET = "GET";

    private static final String AUTHORIZE = "/oauth2/authorize";

    private static final String LOGIN = "/login";

    private static final String LANG = "lang";

    private static final String EN = "en";

    private final RequestCache cache = new HttpSessionRequestCache();

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

        new StorefrontLoginEntryPoint(cache).commence(authorize, response, new BadCredentialsException("anonymous"));

        assertThat(response.getRedirectedUrl()).isEqualTo("http://shop.example.com/en/login?auth=1");
    }

    @Test
    void aFailedLoginGoesBackToThePendingPageWithItsErrorToken() throws IOException {
        new StorefrontLoginFailureHandler(cache, StorefrontLoginFailureHandler.INVALID)
                .onAuthenticationFailure(loginPost("ar"), response, new BadCredentialsException("wrong"));

        assertThat(response.getRedirectedUrl()).isEqualTo("http://shop.example.com/ar/login?auth=1&error=invalid");
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

        new StorefrontLoginSuccessHandler(cache).onAuthenticationSuccess(login, response, shopper);

        assertThat(response.getRedirectedUrl()).startsWith("http://shop.example.com/").contains("/oauth2/authorize?")
                .contains("client_id=store");
    }

    @Test
    void aSuccessfulLoginWithNothingSavedGoesToTheStorefrontWithoutTheMarker() throws IOException, ServletException {
        new StorefrontLoginSuccessHandler(cache).onAuthenticationSuccess(loginPost(EN), response, shopper);

        assertThat(response.getRedirectedUrl()).isEqualTo("http://shop.example.com/en/login");
    }

}
