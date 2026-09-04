package com.asrevo.cvhome.uaa.security;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.asrevo.cvhome.uaa.config.ConsoleProperties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * uaa's two front doors: which one answers, and what the console is told when it does.
 *
 * <p>
 * The cases that matter are the pairs. Every rewrite here has a twin that must <em>not</em> happen when the
 * request arrived on uaa's own host, because that is the door a platform administrator signs in at and it has to
 * keep behaving exactly as it did before the console owned a sign-in page at all.
 * </p>
 */
class ConsoleHandoffTest {

    private static final String PREFIX = "/uaa";

    private static final String LOGIN = "/login";

    private static final String SIGN_IN = "http://gateway.com:8000/sign-in";

    private static final String GET = "GET";

    private static final String HTTP = "http";

    private static final String AUTHORIZE = "/oauth2/authorize";

    private static final String LOCKED = "/login?error=locked";

    private final ConsoleUrls console = new ConsoleUrls(properties());

    private final ConsoleRedirectStrategy strategy = new ConsoleRedirectStrategy(console);

    private final MockHttpServletResponse response = new MockHttpServletResponse();

    private static ConsoleProperties properties() {
        ConsoleProperties properties = new ConsoleProperties();
        properties.setPathPrefix(PREFIX);
        return properties;
    }

    private static MockHttpServletRequest behindTheConsole(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest(GET, uri);
        request.setScheme(HTTP);
        request.setServerName("gateway.com");
        request.setServerPort(8000);
        request.setContextPath(PREFIX);
        return request;
    }

    private static MockHttpServletRequest onUaasOwnHost(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest(GET, uri);
        request.setScheme(HTTP);
        request.setServerName("uaa.gateway.com");
        request.setServerPort(8001);
        request.setContextPath("");
        return request;
    }

    @Test
    void theContextPathIsWhatTellsTheTwoDoorsApart() {
        assertThat(console.isHandoff(behindTheConsole(AUTHORIZE))).isTrue();
        assertThat(console.isHandoff(onUaasOwnHost(AUTHORIZE))).isFalse();
    }

    /** Configuring no prefix turns the hand-off off, and every request goes back to uaa's own pages. */
    @Test
    void anUnconfiguredPrefixMeansThereIsNoConsoleDoorAtAll() {
        assertThat(new ConsoleUrls(new ConsoleProperties()).isHandoff(behindTheConsole(AUTHORIZE))).isFalse();
    }

    @Test
    void theHandoffMarksThePagePendingSoTheConsoleRendersTheFormRatherThanStartingAFlow() {
        String page = console.loginPage(behindTheConsole(AUTHORIZE), response, true, null);

        assertThat(page).isEqualTo(String.format("%s?auth=1", SIGN_IN));
    }

    @Test
    void anErrorTravelsAsATokenTheConsoleTranslates() {
        String page = console.loginPage(behindTheConsole(LOGIN), response, true, "locked");

        assertThat(page).isEqualTo(String.format("%s?auth=1&error=locked", SIGN_IN));
    }

    /**
     * The whole reason the redirect strategy is the seam rather than a second failure handler.
     *
     * <p>
     * {@code LoginFailureHandler} counts the attempts left and reports the lock that the crossing attempt caused;
     * none of that is repeated here, and none of it can drift, because the query it built travels through
     * untouched. {@code ?error} with no value is a wrong password and has to survive as one.
     * </p>
     */
    @Test
    void theFailureQueryUaaBuiltIsCarriedToTheConsoleWhole() throws IOException {
        strategy.sendRedirect(behindTheConsole(LOGIN), response, "/login?error&attemptsLeft=3");

        assertThat(response.getRedirectedUrl()).isEqualTo(String.format("%s?auth=1&error&attemptsLeft=3", SIGN_IN));
    }

    @Test
    void aSignedOutFarewellIsCarriedTheSameWay() throws IOException {
        strategy.sendRedirect(behindTheConsole(LOGIN), response, "/login?logout");

        assertThat(response.getRedirectedUrl()).isEqualTo(String.format("%s?auth=1&logout", SIGN_IN));
    }

    /** With nothing saved to resume, a sign-in lands on the console's page rather than uaa's root under the prefix. */
    @Test
    void theDefaultTargetBecomesTheConsolesOwnPage() throws IOException {
        strategy.sendRedirect(behindTheConsole(LOGIN), response, "/");

        assertThat(response.getRedirectedUrl()).isEqualTo(SIGN_IN);
    }

    /**
     * The one that would break sign-in if it were wrong: a saved request resumes to an absolute authorize URL, and
     * rewriting that would send the browser to the sign-in page forever instead of issuing a code.
     */
    @Test
    void anAbsoluteTargetIsNeverRewritten() throws IOException {
        String authorize = "http://gateway.com:8000/uaa/oauth2/authorize?response_type=code&client_id=web-app";

        strategy.sendRedirect(behindTheConsole(LOGIN), response, authorize);

        assertThat(response.getRedirectedUrl()).isEqualTo(authorize);
    }

    @Test
    void aPathThatIsNotOneOfUaasPagesIsLeftAlone() throws IOException {
        strategy.sendRedirect(behindTheConsole(LOGIN), response, "/accept-invitation?token=abc");

        assertThat(response.getRedirectedUrl()).isEqualTo("/uaa/accept-invitation?token=abc");
    }

    /** uaa's own host keeps uaa's own page, context path and all. This is the platform administrator's door. */
    @Test
    void onUaasOwnHostTheRedirectIsUnchanged() throws IOException {
        strategy.sendRedirect(onUaasOwnHost(LOGIN), response, LOCKED);

        assertThat(response.getRedirectedUrl()).isEqualTo(LOCKED);
    }

}
