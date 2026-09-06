package com.asrevo.cvhome.metrics;

import java.io.IOException;

import jakarta.servlet.ServletException;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every 401/403 is counted once, named after the bearer challenge or the advice's exception, and nothing else is.
 */
class AuthRejectionMetricsFilterTest {

    private static final String ROUTE = "/api/v1/private/product/{id}";

    private static final String UNAUTHORIZED = "401";

    private static final String UNKNOWN_ROUTE = "UNKNOWN";

    private static final String ACCESS_DENIED = "AccessDeniedException";

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();

    private final AuthRejectionMetricsFilter filter = new AuthRejectionMetricsFilter(registry);

    private double count(String status, String reason, String source, String uri) {
        var counter = registry.find(AuthRejectionMetricsFilter.METER)
                .tags("status", status, "reason", reason, "source", source, "uri", uri).counter();
        return counter == null ? 0 : counter.count();
    }

    private void run(MockHttpServletRequest request, MockHttpServletResponse response, int status, String challenge)
            throws ServletException, IOException {
        filter.doFilter(request, response, new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res) {
                response.setStatus(status);
                if (challenge != null) {
                    response.setHeader("WWW-Authenticate", challenge);
                }
            }
        });
    }

    @Test
    void aBearerChallengeWithAnErrorNamesTheReason() throws Exception {
        run(new MockHttpServletRequest(), new MockHttpServletResponse(), 401,
                "Bearer error=\"invalid_token\", error_description=\"Jwt expired\"");

        assertThat(count(UNAUTHORIZED, "invalid_token", AuthRejectionMetricsFilter.SOURCE_FILTER, UNKNOWN_ROUTE)).isEqualTo(1);
    }

    @Test
    void aChallengeWithoutAnErrorMeansNoTokenWasSent() throws Exception {
        run(new MockHttpServletRequest(), new MockHttpServletResponse(), 401,
                "Bearer resource_metadata=\"http://localhost/.well-known/oauth-protected-resource\"");

        assertThat(count(UNAUTHORIZED, AuthRejectionMetricsFilter.REASON_MISSING_TOKEN,
                AuthRejectionMetricsFilter.SOURCE_FILTER, UNKNOWN_ROUTE)).isEqualTo(1);
    }

    @Test
    void theAdvicesReasonAndTheMatchedRouteWinWhenPresent() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(AuthRejectionMetricsFilter.REASON_ATTRIBUTE, ACCESS_DENIED);
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, ROUTE);

        run(request, new MockHttpServletResponse(), 403, null);

        assertThat(count("403", ACCESS_DENIED, AuthRejectionMetricsFilter.SOURCE_ADVICE, ROUTE))
                .isEqualTo(1);
    }

    @Test
    void otherStatusesAreNotCounted() throws Exception {
        run(new MockHttpServletRequest(), new MockHttpServletResponse(), 200, null);
        run(new MockHttpServletRequest(), new MockHttpServletResponse(), 404, null);

        assertThat(registry.find(AuthRejectionMetricsFilter.METER).counters()).isEmpty();
    }

    @Test
    void bearerErrorParsing() {
        assertThat(AuthRejectionMetricsFilter.bearerError(null)).isEqualTo(AuthRejectionMetricsFilter.REASON_UNKNOWN);
        assertThat(AuthRejectionMetricsFilter.bearerError("Bearer")).isEqualTo(AuthRejectionMetricsFilter.REASON_MISSING_TOKEN);
        assertThat(AuthRejectionMetricsFilter.bearerError("Bearer error=\"insufficient_scope\"")).isEqualTo("insufficient_scope");
    }

}
