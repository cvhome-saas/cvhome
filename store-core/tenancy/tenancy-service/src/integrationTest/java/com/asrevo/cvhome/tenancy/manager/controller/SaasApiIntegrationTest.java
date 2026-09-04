package com.asrevo.cvhome.tenancy.manager.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

import com.asrevo.cvhome.tenancy.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;

import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.expect;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.json;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.path;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The two values the sign-up page needs before anyone has an account: the pod suffix a new store's host is built
 * from, and the platform domain it hangs off.
 *
 * <p>
 * Deliberately public — it is read by a browser with no token, which is why it carries nothing but those two
 * values. The case worth keeping is that it stays reachable unauthenticated: put behind the filter chain, the
 * sign-up page cannot tell a prospective merchant what their store's address will be.
 * </p>
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class SaasApiIntegrationTest {

    private static final String SAAS_PROPERTIES = path("/api/v1/saas", "public", "saas-properties");

    private static final String DOMAIN = "domain";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    private TenancyApiTestSupport api;

    @BeforeEach
    void setUp() {
        api = new TenancyApiTestSupport(port, signer);
    }

    @Test
    void thesaasPropertiesAreReadableWithNoTokenAtAll() {
        var response = api.get(SAAS_PROPERTIES, null);

        expect(response, HttpStatus.OK);
        assertThat(json(response).get("alis").asString()).isNotBlank();
        assertThat(json(response).get(DOMAIN).asString()).isNotBlank();
    }

    @Test
    void thesaasPropertiesCarryNothingButThePodSuffixAndTheDomain() {
        var response = api.get(SAAS_PROPERTIES, null);

        expect(response, HttpStatus.OK);
        // A public endpoint's response is its whole attack surface; anything else here would be a leak.
        assertThat(json(response).size()).isEqualTo(2);
    }

    @Test
    void asignedInCallerGetsTheSameAnswer() {
        expect(api.get(SAAS_PROPERTIES, api.superAdmin()), HttpStatus.OK);
    }

}
