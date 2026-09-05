package com.asrevo.cvhome.uaa.security;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.util.UriComponentsBuilder;

import com.asrevo.cvhome.testsupport.annotations.DatabaseIntegrationTest;
import com.asrevo.cvhome.uaa.support.UaaClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * uaa's two front doors, over real HTTP.
 *
 * <p>
 * uaa renders its own sign-in page on its own host, and hands the page to the seller console when the request
 * arrived through store-core-gateway. The only signal that tells them apart is the context path — set from
 * {@code X-Forwarded-Prefix} by {@code PathPrefixFilter} — and that is deliberate: it cannot be spoofed by a query
 * parameter, and it is the same value the container scoped the session cookie to.
 * </p>
 *
 * <p>
 * The unit tests cover the URL building; what only a booted server proves is that the whole chain agrees. The
 * entry point, the access-denied handler and the redirect strategy each consult the same switch, and if one of
 * them disagreed a person would be bounced between two origins, or be shown uaa's own page on the console's host
 * with no way back.
 * </p>
 */
@DatabaseIntegrationTest
@TestPropertySource(properties = "com.asrevo.cvhome.uaa.console.path-prefix=/uaa")
class ConsoleHandoffIntegrationTest {

    private static final String OAUTH2_AUTHORIZE = "/oauth2/authorize";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String CSRF = "_csrf";
    private static final String PREFIX_HEADER = "X-Forwarded-Prefix";

    private static final String PREFIX = "/uaa";

    private static final String SIGN_IN = "/sign-in";

    private static final String PENDING = "auth=1";

    /** The registered console redirect; an unregistered one is refused before authentication is considered. */
    private static final String REDIRECT = "http://gateway.com:8000/login/oauth2/code/uaa";

    /** PKCE is mandatory for {@code web-app}; without a challenge the request never reaches authentication. */
    private static final String VERIFIER = "console-handoff-verifier-0123456789";

    @LocalServerPort
    private int port;

    private UaaClient uaa;

    @BeforeEach
    void setUp() {
        uaa = new UaaClient(port);
    }

    @Test
    void anUnauthenticatedAuthorizeBehindTheConsoleIsHandedToTheConsolesSignInPage() throws Exception {
        HttpResponse<String> response = behindConsole(authorize());

        assertThat(response.statusCode()).isEqualTo(302);
        // Back to the origin the browser started on, not to a configured host: the console answers on three.
        assertThat(UaaClient.location(response)).contains(SIGN_IN)
                .startsWith(String.format("http://localhost:%d", port));
    }

    @Test
    void asavedAuthorizeRequestTellsTheConsoleToRenderTheForm() throws Exception {
        HttpResponse<String> response = behindConsole(authorize());

        // `auth=1` is how the console knows uaa is holding a saved authorize request rather than showing a
        // sign-in page nobody asked for.
        assertThat(UaaClient.location(response)).contains(PENDING);
    }

    @Test
    void thesameAuthorizeOnUaasOwnHostGetsUaasOwnSignInPage() throws Exception {
        HttpResponse<String> response = uaa.anonymous(UaaClient.GET, authorize());

        // No forwarded prefix, so the context path is empty and the hand-off does not apply — which is what keeps
        // a platform administrator able to sign in at uaa's own address.
        assertThat(response.statusCode()).isEqualTo(302);
        assertThat(UaaClient.location(response)).endsWith(UaaClient.LOGIN);
    }

    @Test
    void thehandoffPlantsTheCsrfCookieBecauseSomebodyElseRendersTheForm() throws Exception {
        HttpResponse<String> response = behindConsole(authorize());

        // The redirect is the last response uaa sends before the console renders the form, so if uaa does not
        // plant the cookie here the console has no token to post back.
        assertThat(response.headers().allValues("set-cookie").toString()).contains(UaaClient.XSRF_COOKIE);
    }

    /** A GET that arrived through store-core-gateway, which is the only thing the forwarded prefix means here. */
    private static String authorize() throws NoSuchAlgorithmException {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(VERIFIER.getBytes(StandardCharsets.US_ASCII));
        String challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        return UriComponentsBuilder.fromPath(OAUTH2_AUTHORIZE)
                .queryParam("response_type", "code")
                .queryParam("client_id", UaaClient.WEB_APP)
                .queryParam("scope", "openid")
                .queryParam("redirect_uri", REDIRECT)
                .queryParam("code_challenge", challenge)
                .queryParam("code_challenge_method", "S256")
                .build().toUriString();
    }

    private HttpResponse<String> behindConsole(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(uaa.url(PREFIX + path)))
                .header(PREFIX_HEADER, PREFIX)
                .GET()
                .build();
        return uaa.send(request);
    }

    /**
     * A failed sign-in behind the console: uaa redirects to its own {@code /login?error}, and the redirect
     * strategy rewrites it to the console's page carrying the same query. Without the rewrite the person lands on
     * uaa's own sign-in form on the console's origin, with no way back to where they started.
     */
    @Test
    void awrongPasswordBehindTheConsoleGoesBackToTheConsolesPageCarryingTheError() throws Exception {
        HttpResponse<String> refused = loginBehindConsole(UaaClient.SUPER_ADMIN, "not-the-password");

        assertThat(refused.statusCode()).isEqualTo(302);
        assertThat(UaaClient.location(refused)).contains(SIGN_IN).contains("error");
    }

    /** A sign-in that had a saved authorize request resumes it rather than going to either sign-in page. */
    @Test
    void asuccessfulSignInBehindTheConsoleResumesTheSavedAuthorizeRequest() throws Exception {
        HttpResponse<String> signedIn = loginBehindConsole(UaaClient.SUPER_ADMIN, UaaClient.PASSWORD);

        assertThat(signedIn.statusCode()).isEqualTo(302);
        assertThat(UaaClient.location(signedIn)).contains(OAUTH2_AUTHORIZE).contains(PREFIX);
    }

    /**
     * With nothing saved, uaa would redirect to its own root; behind the console that has to become the console's
     * page, or the person lands on uaa's SPA on the console's origin.
     */
    @Test
    void asignInWithNothingSavedLandsOnTheConsolesPageRatherThanUaasRoot() throws Exception {
        behindConsole(UaaClient.LOGIN);
        HttpResponse<String> signedIn = postLoginBehindConsole(
                Map.of(USERNAME, UaaClient.SUPER_ADMIN, PASSWORD, UaaClient.PASSWORD, CSRF, uaa.csrfToken()));

        assertThat(signedIn.statusCode()).isEqualTo(302);
        assertThat(UaaClient.location(signedIn)).contains(SIGN_IN);
    }

    /**
     * A tab left open past the session posts a stale CSRF token. On uaa's own host that is a problem body; behind
     * the console it has to be a redirect back to the console's form, because JSON is the wrong answer to a form
     * post and the person has no way to read it.
     */
    @Test
    void astaleCsrfTokenOnTheFormBehindTheConsoleGoesBackToTheConsolesForm() throws Exception {
        behindConsole(authorize());

        HttpResponse<String> stale = postLoginBehindConsole(
                Map.of(USERNAME, UaaClient.SUPER_ADMIN, PASSWORD, UaaClient.PASSWORD));

        assertThat(stale.statusCode()).isEqualTo(302);
        assertThat(UaaClient.location(stale)).contains(SIGN_IN);
    }

    /** Signs in through the form as it arrives behind store-core-gateway, prefix header and all. */
    private HttpResponse<String> loginBehindConsole(String username, String password) throws Exception {
        behindConsole(authorize());
        return postLoginBehindConsole(
                Map.of(USERNAME, username, PASSWORD, password, CSRF, uaa.csrfToken()));
    }

    private HttpResponse<String> postLoginBehindConsole(Map<String, String> fields)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(uaa.url(PREFIX + UaaClient.LOGIN)))
                .header(PREFIX_HEADER, PREFIX)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(UaaClient.form(fields)))
                .build();
        return uaa.send(request);
    }

}
