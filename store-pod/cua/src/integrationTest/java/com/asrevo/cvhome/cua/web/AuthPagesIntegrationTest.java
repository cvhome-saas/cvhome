package com.asrevo.cvhome.cua.web;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

import com.asrevo.cvhome.cua.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.testsupport.annotations.DatabaseIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The customer-facing auth pages actually render.
 *
 * <p>
 * These are Thymeleaf templates reading a Java model, and nothing else checks that pairing: the compiler cannot
 * see inside a template, and a context-loads test never renders one. So when {@code logo} was removed from
 * {@code ReadableMerchantStore} — appearance moved to the content service — the templates went on asking for
 * {@code store.logo} and threw {@code EL1008E} at render time. The failure was dispatched to {@code /error},
 * {@code /error} required authentication, and security sent the shopper back to {@code /login}, which threw
 * again: clicking "Login" on the storefront reloaded forever, with nothing visible to say why.
 * </p>
 *
 * <p>
 * The store only reaches these pages through a saved request, so each case walks the real path —
 * {@code /oauth2/authorize} first, then the page it redirects to, carrying the session. Rendering the templates
 * directly would miss exactly the branch that broke.
 * </p>
 */
@DatabaseIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class AuthPagesIntegrationTest {

    /** A store in the {@code test-stores} seed; cua registers an OAuth2 client per store, keyed on the store id. */
    private static final String STORE = "65f023632bc46470c104b76f";

    private static final String LOGIN = "/login";

    private static final String REGISTER = "/register";

    @LocalServerPort
    private int port;

    /**
     * Redirects are not followed: the session cookie arrives on the 302 from {@code /oauth2/authorize}, and the
     * point of these cases is what the redirect target does with it.
     */
    private final HttpClient http = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();

    private String session;

    private HttpResponse<String> fetch(String path, String cookie) throws IOException, InterruptedException {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(String.format("http://localhost:%d%s",
                port, path)));
        if (cookie != null) {
            request.header("Cookie", cookie);
        }
        return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Walks {@code /oauth2/authorize} to get the session whose saved request carries the store, which is what
     * makes the pages render their store header at all.
     */
    @BeforeEach
    void startAuthorization() throws IOException, InterruptedException {
        /*
         * The redirect URI is not configured anywhere — `DynamicRegisteredClientRepository` derives it from the
         * incoming request as `scheme://host[:port]/{lang}/callback`. So it has to be built from the random test
         * port, and `lang` has to be on the authorize URL for the `/en` segment to be part of it.
         */
        String redirect = URLEncoder.encode(String.format("http://localhost:%d/en/callback", port),
                StandardCharsets.UTF_8);
        String authorize = String.format("""
                /oauth2/authorize?response_type=code&client_id=%s&redirect_uri=%s&scope=openid\
                &code_challenge=%s&code_challenge_method=S256&store=%s&lang=en""",
                STORE, redirect, "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM", STORE);

        HttpResponse<String> response = fetch(authorize, null);

        assertThat(response.statusCode()).as("authorize should redirect to the login page").isEqualTo(302);
        session = response.headers().firstValue("set-cookie").orElse(null);
        assertThat(session).as("authorize must start a session for the saved request").isNotNull();
    }

    @Test
    void theLoginPageRendersWithTheStoresNameAndLogo() throws IOException, InterruptedException {
        HttpResponse<String> page = fetch(LOGIN, session);

        assertThat(page.statusCode()).isEqualTo(200);
        assertThat(page.body()).contains(ExternalClientsTestConfiguration.STORE_NAME)
                .contains(ExternalClientsTestConfiguration.LOGO_URL);
    }

    @Test
    void theRegisterPageRendersWithTheStoresNameAndLogo() throws IOException, InterruptedException {
        HttpResponse<String> page = fetch(REGISTER, session);

        assertThat(page.statusCode()).isEqualTo(200);
        assertThat(page.body()).contains(ExternalClientsTestConfiguration.STORE_NAME)
                .contains(ExternalClientsTestConfiguration.LOGO_URL);
    }

    /**
     * The pages are reachable without a session too — that is the path a shopper takes when they open
     * {@code /login} directly rather than being sent there. It renders without a store header.
     */
    @Test
    void theLoginPageRendersWithoutAStoreBehindIt() throws IOException, InterruptedException {
        assertThat(fetch(LOGIN, null).statusCode()).isEqualTo(200);
    }

}
