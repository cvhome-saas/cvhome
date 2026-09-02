package com.asrevo.cvhome.cua.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;

import static org.assertj.core.api.Assertions.assertThat;

class StorefrontUrlsTest {

    private static final String HTTP = "http";

    private static final String HTTPS = "https";

    private static final String HOST = "shop.example.com";

    private static final String LANG = "lang";

    private static final String PENDING_EN = "http://shop.example.com/en/login?auth=1";

    private final RequestCache cache = new HttpSessionRequestCache();

    private final MockHttpServletResponse response = new MockHttpServletResponse();

    private static MockHttpServletRequest request(String scheme, int port) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme(scheme);
        request.setServerName(HOST);
        request.setServerPort(port);
        request.setContextPath("/cua");
        return request;
    }

    private static MockHttpServletRequest plainRequest(String lang) {
        MockHttpServletRequest request = request(HTTP, 80);
        request.setParameter(LANG, lang);
        return request;
    }

    @Test
    void originDropsTheSchemesDefaultPortAndTheContextPath() {
        assertThat(StorefrontUrls.origin(request(HTTP, 80))).isEqualTo("http://shop.example.com");
        assertThat(StorefrontUrls.origin(request(HTTPS, 443))).isEqualTo("https://shop.example.com");
    }

    @Test
    void originKeepsAShiftedPort() {
        assertThat(StorefrontUrls.origin(request(HTTP, 1080))).isEqualTo("http://shop.example.com:1080");
    }

    @Test
    void loginPageIsTheStorefrontsLocalePrefixedRouteWithTheMarker() {
        String url = StorefrontUrls.loginPage(plainRequest("ar"), response, cache, true, null);

        assertThat(url).isEqualTo("http://shop.example.com/ar/login?auth=1");
    }

    @Test
    void loginPageCarriesTheErrorTokenAfterTheMarker() {
        MockHttpServletRequest request = request(HTTPS, 443);
        request.setParameter(LANG, "en");

        String url = StorefrontUrls.loginPage(request, response, cache, true, "invalid");

        assertThat(url).isEqualTo("https://shop.example.com/en/login?auth=1&error=invalid");
    }

    @Test
    void loginPageWithoutTheMarkerHasNoQueryAtAll() {
        String url = StorefrontUrls.loginPage(plainRequest("fr"), response, cache, false, null);

        assertThat(url).isEqualTo("http://shop.example.com/fr/login");
    }

    @Test
    void languageFallsBackToTheSavedRequest() {
        MockHttpServletRequest authorize = plainRequest("es");
        authorize.setRequestURI("/oauth2/authorize");
        cache.saveRequest(authorize, response);
        MockHttpServletRequest callback = request(HTTP, 80);
        callback.setSession(authorize.getSession());

        String url = StorefrontUrls.loginPage(callback, response, cache, true, null);

        assertThat(url).isEqualTo("http://shop.example.com/es/login?auth=1");
    }

    @Test
    void languageDefaultsToEnglishWhenNothingCarriesOne() {
        String url = StorefrontUrls.loginPage(request(HTTP, 80), response, cache, true, null);

        assertThat(url).isEqualTo(PENDING_EN);
    }

    @Test
    void aLangThatIsNotALanguageCodeCannotBecomeAPathSegment() {
        String url = StorefrontUrls.loginPage(plainRequest("../../admin"), response, cache, true, null);

        assertThat(url).isEqualTo(PENDING_EN);
    }

}
