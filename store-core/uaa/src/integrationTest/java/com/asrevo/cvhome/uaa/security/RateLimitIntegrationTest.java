package com.asrevo.cvhome.uaa.security;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import com.asrevo.cvhome.testsupport.annotations.DatabaseIntegrationTest;
import com.asrevo.cvhome.uaa.support.UaaClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The fourth sign-in attempt from one address in a minute is a 429 before Spring Security sees it (the limit is
 * lowered to three for this context; the default is ten).
 */
@DatabaseIntegrationTest
@TestPropertySource(properties = "com.asrevo.cvhome.uaa.rate-limit.login.limit=3")
class RateLimitIntegrationTest {

    /** Not an account, so nothing is locked by the attempts — only the address is counted. */
    private static final String NOBODY = "nobody-here";

    @LocalServerPort
    private int port;

    @Test
    void aBurstOfSignInsIsRefusedWithAProblem() throws IOException, InterruptedException {
        UaaClient uaa = new UaaClient(port);
        uaa.anonymous(UaaClient.GET, UaaClient.LOGIN);
        Map<String, String> form = Map.of("username", NOBODY, "password", "x", "_csrf", uaa.csrfToken());

        HttpResponse<String> last = null;
        for (int i = 0; i < 3; i++) {
            last = uaa.postForm(UaaClient.LOGIN, form);
            assertThat(last.statusCode()).as("attempt %d", i + 1).isEqualTo(302);
        }
        HttpResponse<String> refused = uaa.postForm(UaaClient.LOGIN, form);

        assertThat(refused.statusCode()).isEqualTo(429);
        assertThat(refused.headers().firstValue("Retry-After")).contains("60");
        assertThat(UaaClient.body(refused).get("code").asText()).isEqualTo("UAA.AUTH.RATE_LIMITED");
        assertThat(UaaClient.body(refused).get("traceId")).isNotNull();
        // Loading the page is not an attempt.
        assertThat(uaa.anonymous(UaaClient.GET, UaaClient.LOGIN).statusCode()).isEqualTo(200);
    }

}
