package com.asrevo.cvhome.sso.config;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What the server calls its own address when a proxy has put it under a path.
 *
 * <p>
 * Both deployments depend on this and both fail quietly without it: a redirect to the sign-in page drops the
 * prefix and lands on the fronting application's own route, and the session cookie is written at {@code /} where
 * it collides with the front end's. Neither shows up as an error.
 * </p>
 */
class PathPrefixFilterTest {

    private static final String PREFIX = "X-Forwarded-Prefix";

    private static final String UAA = "/uaa";

    private static final String LOGIN = "/login";

    private final PathPrefixFilter filter = new PathPrefixFilter();

    private final MockHttpServletResponse response = new MockHttpServletResponse();

    private static MockHttpServletRequest request(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setScheme("http");
        request.setServerName("gateway.com");
        request.setServerPort(8000);
        return request;
    }

    private HttpServletRequest through(MockHttpServletRequest request) throws ServletException, IOException {
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        return (HttpServletRequest) chain.getRequest();
    }

    @Test
    void theForwardedPrefixBecomesTheContextPath() throws Exception {
        MockHttpServletRequest request = request(String.format("%s/oauth2/authorize", UAA));
        request.addHeader(PREFIX, UAA);

        assertThat(through(request).getContextPath()).isEqualTo(UAA);
    }

    /** Absolute URLs the server builds have to carry the prefix, or a redirect lands on the front end's own route. */
    @Test
    void theRequestUrlCarriesThePrefixAndThePort() throws Exception {
        MockHttpServletRequest request = request(String.format("%s%s", UAA, LOGIN));
        request.addHeader(PREFIX, UAA);

        assertThat(through(request).getRequestURL()).hasToString("http://gateway.com:8000/uaa/login");
    }

    /** A default port is left off, so the URL matches what the browser actually asked for. */
    @Test
    void aDefaultPortIsNotWrittenOut() throws Exception {
        MockHttpServletRequest request = request(String.format("/cua%s", LOGIN));
        request.setServerPort(80);
        request.addHeader(PREFIX, "/cua");

        assertThat(through(request).getRequestURL()).hasToString("http://gateway.com/cua/login");
    }

    /**
     * No header, no wrapper. This is the case that keeps uaa's own address working: reached directly the context
     * path stays empty, which is exactly what {@code ConsoleUrls.isHandoff} reads to decide it is not behind the
     * console.
     */
    @Test
    void withoutTheHeaderTheRequestIsUntouched() throws Exception {
        MockHttpServletRequest request = request(LOGIN);

        HttpServletRequest passed = through(request);

        assertThat(passed).isSameAs(request);
        assertThat(passed.getContextPath()).isEmpty();
    }

    @Test
    void anEmptyHeaderIsIgnoredRatherThanTreatedAsAPrefix() throws Exception {
        MockHttpServletRequest request = request(LOGIN);
        request.addHeader(PREFIX, "");

        assertThat(through(request)).isSameAs(request);
    }

}
