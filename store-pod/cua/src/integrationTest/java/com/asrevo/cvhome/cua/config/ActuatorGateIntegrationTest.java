package com.asrevo.cvhome.cua.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.asrevo.cvhome.testsupport.annotations.DatabaseIntegrationTest;
import com.asrevo.cvhome.testsupport.http.ApiClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * cua's actuator over the real filter chain.
 *
 * <p>
 * cua is an authorization server: its heap holds the keys that sign every shopper's token, so {@code /heapdump}
 * and {@code /env} are the two endpoints that must never answer a stranger. They did — the chain permitted every
 * actuator endpoint (authorization audit, A7). The probes stay open because a platform polls them.
 * </p>
 *
 * <p>
 * The anonymous cases are here; the shopper case is not, because this context wires no token signer and cua's own
 * shopper tokens come from a login. That a shopper cannot hold the authority the rule asks for is
 * {@code RealmAwareJwtGrantedAuthoritiesConverterTest}'s subject: the realm's grants cap a cua token at
 * {@code ROLE_CUSTOMER} and {@code SCOPE_OPENID} whatever it claims.
 * </p>
 */
@DatabaseIntegrationTest
class ActuatorGateIntegrationTest {

    private static final String HEALTH = "/actuator/health";

    private static final String ENV = "/actuator/env";

    @LocalServerPort
    private int port;

    @Test
    void theProbesAnswerWithoutACredential() {
        ResponseEntity<String> health = new ApiClient(port).get(HEALTH, null);

        // Whether the aggregate is UP depends on what this context wires; that security let it through does not.
        assertThat(health.getStatusCode()).isNotIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
        assertThat(health.getBody()).contains("status");
    }

    @Test
    void everythingElseIsRefusedWithoutACredential() {
        for (String endpoint : new String[] {ENV, "/actuator/heapdump", "/actuator/configprops", "/actuator/beans"}) {
            ResponseEntity<String> response = new ApiClient(port).get(endpoint, null);
            // A refusal, never a redirect: an endpoint the exposure does not map answers like one it does.
            assertThat(response.getStatusCode())
                    .as("%s -> %s", endpoint, response.getHeaders().getLocation())
                    .isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
        }
    }


}
