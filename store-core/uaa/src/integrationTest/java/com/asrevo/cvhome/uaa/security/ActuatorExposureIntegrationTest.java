package com.asrevo.cvhome.uaa.security;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.asrevo.cvhome.testsupport.annotations.DatabaseIntegrationTest;
import com.asrevo.cvhome.uaa.support.UaaClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Only health and info are public. The rest of the actuator used to be — and a heap dump of an authorization server
 * carries its signing key.
 */
@DatabaseIntegrationTest
class ActuatorExposureIntegrationTest {

    private static final String ENV = "/actuator/env";

    @LocalServerPort
    private int port;

    @Test
    void healthAndInfoAreOpen() throws IOException, InterruptedException {
        UaaClient uaa = new UaaClient(port);

        assertThat(uaa.anonymous(UaaClient.GET, "/actuator/health").statusCode()).isEqualTo(200);
        assertThat(uaa.anonymous(UaaClient.GET, "/actuator/info").statusCode()).isEqualTo(200);
    }

    @Test
    void everythingElseIsClosedToAnonymousAndUnmappedForEveryoneElse() throws IOException, InterruptedException {
        UaaClient uaa = new UaaClient(port);

        for (String endpoint : new String[] {ENV, "/actuator/heapdump", "/actuator/loggers", "/actuator"}) {
            assertThat(uaa.anonymous(UaaClient.GET, endpoint).statusCode()).as(endpoint).isEqualTo(401);
        }
        // A platform principal passes the gate, and finds the endpoint is not exposed at all.
        assertThat(uaa.bearer(UaaClient.GET, ENV, null, uaa.storeCoreToken()).statusCode()).isEqualTo(404);
    }

}
