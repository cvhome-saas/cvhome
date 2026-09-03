package com.asrevo.cvhome.uaa.oauth;

import java.io.IOException;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.asrevo.cvhome.testsupport.annotations.DatabaseIntegrationTest;
import com.asrevo.cvhome.uaa.support.UaaClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The issuer is what the service registry says, not what the request's {@code Host} header says.
 *
 * <p>
 * Every request here arrives on {@code localhost:<random port>}; if the issuer still reads
 * {@code http://uaa.gateway.com:8001}, it is pinned. An unpinned server would answer with the localhost address and
 * mint tokens no resource server trusts the moment a proxy sat in front of it.
 * </p>
 */
@DatabaseIntegrationTest
class IssuerPinningIntegrationTest {

    /** {@code com.asrevo.cvhome.services.uaa} in common-config.yml, normalized. */
    static final String PINNED = "http://uaa.gateway.com:8001";

    @LocalServerPort
    private int port;

    @Test
    void discoveryDocumentNamesThePinnedIssuer() throws IOException, InterruptedException {
        UaaClient uaa = new UaaClient(port);

        HttpResponse<String> response = uaa.anonymous("GET", "/.well-known/openid-configuration");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(UaaClient.body(response).get("issuer").asText()).isEqualTo(PINNED);
    }

    @Test
    void issuedTokensCarryThePinnedIssuer() throws IOException, InterruptedException {
        UaaClient uaa = new UaaClient(port);

        String token = uaa.storeCoreToken();

        assertThat(UaaClient.claims(token).get("iss").asText()).isEqualTo(PINNED);
    }

}
