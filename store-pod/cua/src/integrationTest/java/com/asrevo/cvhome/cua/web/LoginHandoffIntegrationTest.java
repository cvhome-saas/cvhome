package com.asrevo.cvhome.cua.web;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import com.asrevo.cvhome.testsupport.annotations.DatabaseIntegrationTest;
import com.asrevo.cvhome.testsupport.http.ApiClient;

import static com.asrevo.cvhome.testsupport.http.ApiClient.expect;
import static com.asrevo.cvhome.testsupport.http.ApiClient.scoped;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The browser-facing half of cua, walked as a browser would: cua renders nothing, so every step must end in a
 * redirect to the storefront or back into the OAuth2 flow.
 *
 * <p>
 * Each case starts at the real {@code /oauth2/authorize} — with {@code prompt=login}, as the storefront sends it
 * — because that is what stores the saved request the hand-off depends on. A form posted without one is the one
 * edge case this file also pins down.
 * </p>
 */
@DatabaseIntegrationTest
class LoginHandoffIntegrationTest {

    /** A store in the {@code test-stores} seed with the shopper {@code user} / {@code revo}. */
    private static final String STORE = "65f023632bc46470c104b76f";

    /** Another store of the seed. It has shoppers of its own, which is exactly why isolation uses a fresh one. */
    private static final String OTHER_STORE = "65f023632bc46470c104b75f";

    private static final String LOGIN = "/login";

    private static final String USER = "user";

    private static final String PASSWORD = "revo";

    private static final String COOKIE = "Cookie";

    private static final String SET_COOKIE = "set-cookie";

    private static final String LOCATION = "location";

    private static final String PENDING = "/en/login?auth=1";

    private static final String INVALID = "/en/login?auth=1&error=invalid";

    private static final String NOT_PENDING = "/en/login";

    private static final String OLD_LOGIN_PAGE = "/login?lang=en";

    private static final String RESUMED_AUTHORIZE = "/oauth2/authorize?";

    /** Long enough for the registration body's size rule; the seeded {@code revo} is not. */
    private static final String NEW_PASSWORD = "secret-1";

    @LocalServerPort
    private int port;

    /** Redirects are never followed: the redirect target is the assertion. */
    private final HttpClient http = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();

    private String session;

    private String url(String path) {
        return String.format("http://localhost:%d%s", port, path);
    }

    private HttpResponse<String> get(String path, String cookie) throws IOException, InterruptedException {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url(path)));
        if (cookie != null) {
            request.header(COOKIE, cookie);
        }
        return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> postLogin(String cookie, String store, String username, String password)
            throws IOException, InterruptedException {
        String form = String.format("username=%s&password=%s&client_id=%s&lang=en", username, password, store);
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url(LOGIN)))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form));
        if (cookie != null) {
            request.header(COOKIE, cookie);
        }
        return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String location(HttpResponse<String> response) {
        return response.headers().firstValue(LOCATION).orElse(null);
    }

    /** Signing in rotates the session id (fixation protection), so the cookie to carry on with is the newest one. */
    private static String cookieAfter(HttpResponse<String> response, String previous) {
        return response.headers().firstValue(SET_COOKIE).orElse(previous);
    }

    /** Walks {@code /oauth2/authorize}, keeping the session cookie that now carries the saved request. */
    @BeforeEach
    void startAuthorization() throws IOException, InterruptedException {
        String redirect = URLEncoder.encode(url("/en/callback"), StandardCharsets.UTF_8);
        String authorize = String.format("""
                /oauth2/authorize?response_type=code&client_id=%s&redirect_uri=%s&scope=openid\
                &code_challenge=%s&code_challenge_method=S256&prompt=login&store=%s&lang=en""",
                STORE, redirect, "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM", STORE);

        HttpResponse<String> response = get(authorize, null);

        assertThat(response.statusCode()).as("authorize must hand the browser to the storefront").isEqualTo(302);
        assertThat(location(response)).isEqualTo(url(PENDING));
        Optional<String> cookie = response.headers().firstValue(SET_COOKIE);
        assertThat(cookie).as("authorize must start the session that holds the saved request").isPresent();
        session = cookie.get();
    }

    @Test
    void theSessionCookieIsHttpOnlyWithAPath() {
        assertThat(session).startsWith("SESSION=").contains("Path=/").contains("HttpOnly");
    }

    @Test
    void aWrongPasswordGoesBackToThePendingStorefrontPageWithTheErrorToken()
            throws IOException, InterruptedException {
        HttpResponse<String> response = postLogin(session, STORE, USER, "wrong");

        assertThat(response.statusCode()).isEqualTo(302);
        assertThat(location(response)).isEqualTo(url(INVALID));
    }

    @Test
    void theRightPasswordResumesTheAuthorizeRequestAndReachesTheCallback() throws IOException, InterruptedException {
        HttpResponse<String> login = postLogin(session, STORE, USER, PASSWORD);

        assertThat(login.statusCode()).isEqualTo(302);
        assertThat(location(login)).startsWith(url(RESUMED_AUTHORIZE));

        URI resumed = URI.create(location(login));
        HttpResponse<String> authorize = get(String.format("%s?%s", resumed.getRawPath(), resumed.getRawQuery()),
                cookieAfter(login, session));

        assertThat(authorize.statusCode()).isEqualTo(302);
        assertThat(location(authorize)).as("prompt=login must not send an authenticated shopper round again")
                .startsWith(url("/en/callback?code="));
    }

    /** A shopper is the store's: an account registered on one store is nobody on another. */
    @Test
    void aShopperOfOneStoreIsNobodyOnAnother() throws IOException, InterruptedException {
        String username = ApiClient.slug("jane");
        expect(new ApiClient(port).send(HttpMethod.POST, scoped("/api/v1/public/registration", STORE), null,
                String.format("""
                        {"username": "%s", "email": "%s@example.com", "password": "%s"}""", username, username,
                        NEW_PASSWORD)), HttpStatus.CREATED);

        HttpResponse<String> elsewhere = postLogin(session, OTHER_STORE, username, NEW_PASSWORD);
        HttpResponse<String> home = postLogin(session, STORE, username, NEW_PASSWORD);

        assertThat(location(elsewhere)).isEqualTo(url(INVALID));
        assertThat(location(home)).startsWith(url(RESUMED_AUTHORIZE));
    }

    /** No session, nothing saved: the shopper is still sent to the storefront, without the pending marker. */
    @Test
    void aPostWithoutASavedRequestLandsOnTheStorefrontWithoutTheMarker() throws IOException, InterruptedException {
        HttpResponse<String> response = postLogin(null, STORE, USER, PASSWORD);

        assertThat(response.statusCode()).isEqualTo(302);
        assertThat(location(response)).isEqualTo(url(NOT_PENDING));
    }

    @Test
    void theOldLoginPageUrlRedirectsToTheStorefront() throws IOException, InterruptedException {
        HttpResponse<String> withFlow = get(OLD_LOGIN_PAGE, session);
        HttpResponse<String> withoutFlow = get(OLD_LOGIN_PAGE, null);

        assertThat(withFlow.statusCode()).isEqualTo(302);
        assertThat(location(withFlow)).isEqualTo(url(PENDING));
        assertThat(withoutFlow.statusCode()).isEqualTo(302);
        assertThat(location(withoutFlow)).isEqualTo(url(NOT_PENDING));
    }

}
