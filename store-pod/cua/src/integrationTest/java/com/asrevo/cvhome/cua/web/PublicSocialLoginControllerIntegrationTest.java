package com.asrevo.cvhome.cua.web;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.asrevo.cvhome.testsupport.annotations.DatabaseIntegrationTest;
import com.asrevo.cvhome.testsupport.http.ApiClient;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.testsupport.http.ApiClient.expect;
import static com.asrevo.cvhome.testsupport.http.ApiClient.json;
import static com.asrevo.cvhome.testsupport.http.ApiClient.scoped;
import static org.assertj.core.api.Assertions.assertThat;

/** The provider list the storefront's login page renders: per store, and never carrying a credential. */
@DatabaseIntegrationTest
class PublicSocialLoginControllerIntegrationTest {

    private static final String PATH = "/api/v1/public/social-logins";

    /** Seeded with google, github and facebook enabled. */
    private static final String STORE = "65f023632bc46470c104b76f";

    /** Not in the seed at all. */
    private static final String UNKNOWN_STORE = "000000000000000000000000";

    private static final String GOOGLE = "google";

    @LocalServerPort
    private int port;

    @Test
    void theSeededStoreListsItsProvidersAsLinkTargets() {
        ResponseEntity<String> response = new ApiClient(port).get(scoped(PATH, STORE), null);

        expect(response, HttpStatus.OK);
        JsonNode logins = json(response);
        assertThat(logins.findValuesAsString("providerId")).containsExactlyInAnyOrder(GOOGLE, "github", "facebook");
        // The alias alone: it is unique within the realm, and the realm comes from the host.
        assertThat(logins.findValuesAsString("registrationId")).contains(GOOGLE);
        assertThat(logins.findValuesAsString("name")).contains("Google");
    }

    @Test
    void theBodyNeverCarriesAnAppIdOrSecret() {
        ResponseEntity<String> response = new ApiClient(port).get(scoped(PATH, STORE), null);

        expect(response, HttpStatus.OK);
        assertThat(response.getBody()).doesNotContain("appId", "appSecret", "ENC:");
    }

    @Test
    void aStoreWithNoProvidersGetsAnEmptyList() {
        ResponseEntity<String> response = new ApiClient(port).get(scoped(PATH, UNKNOWN_STORE), null);

        expect(response, HttpStatus.OK);
        assertThat(json(response)).isEmpty();
    }

}
