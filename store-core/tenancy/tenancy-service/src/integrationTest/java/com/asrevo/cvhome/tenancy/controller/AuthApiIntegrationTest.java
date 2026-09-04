package com.asrevo.cvhome.tenancy.controller;

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

import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.ORG_A;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.expect;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.json;
import static com.asrevo.cvhome.tenancy.support.TenancyApiTestSupport.path;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * "Who am I" — the console's first call after a sign-in.
 *
 * <p>
 * These endpoints took {@code @AuthenticationPrincipal Principal}, and the principal of a
 * {@code JwtAuthenticationToken} is a {@code Jwt}, which does not implement {@link java.security.Principal}.
 * Spring's resolver passes {@code null} for a parameter it cannot satisfy rather than failing, so {@code /current}
 * answered <strong>401 to a caller holding a valid token</strong> — the one answer an identity endpoint must never
 * give. Both now take the {@code Authentication}; the regression these tests catch is a 401 or a 500 where the
 * signed-in caller's own name should be.
 * </p>
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class AuthApiIntegrationTest {

    private static final String BASE = "/api/v1/auth";

    private static final String CURRENT = path(BASE, "current");

    private static final String ME = path(BASE, "me");

    private static final String NAME = "name";

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
    void asignedInCallerIsToldWhoTheyAreRatherThanBeingRefused() {
        var response = api.get(CURRENT, api.orgAdmin(ORG_A));

        expect(response, HttpStatus.OK);
        assertThat(json(response).get(NAME).asString()).isNotBlank();
    }

    @Test
    void everyKindOfPrincipalGetsAnAnswerFromCurrent() {
        for (String token : new String[] {api.superAdmin(), api.orgAdmin(ORG_A), api.service()}) {
            expect(api.get(CURRENT, token), HttpStatus.OK);
        }
    }

    @Test
    void acallerWithNoTokenIsUnauthenticated() {
        expect(api.get(CURRENT, null), HttpStatus.UNAUTHORIZED);
    }

    @Test
    void themeEndpointCarriesTheTokensClaims() {
        var response = api.get(ME, api.orgAdmin(ORG_A));

        expect(response, HttpStatus.OK);
        assertThat(json(response).get(NAME).asString()).isNotBlank();
    }

    @Test
    void themeEndpointIsAuthenticatedOnly() {
        expect(api.get(ME, null), HttpStatus.UNAUTHORIZED);
    }

}
