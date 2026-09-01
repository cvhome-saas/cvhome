package com.asrevo.cvhome.merchant.api.v1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.asrevo.cvhome.testsupport.annotations.StorageIntegrationTest;
import com.asrevo.cvhome.testsupport.http.ApiClient;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.testsupport.security.Tokens;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.testsupport.http.ApiClient.expect;
import static com.asrevo.cvhome.testsupport.http.ApiClient.json;
import static com.asrevo.cvhome.testsupport.http.ApiClient.path;
import static com.asrevo.cvhome.testsupport.http.ApiClient.query;
import static com.asrevo.cvhome.testsupport.http.ApiClient.scoped;
import static com.asrevo.cvhome.testsupport.http.ApiClient.slug;
import static com.asrevo.cvhome.testsupport.security.Tokens.ROLE_STORE_ADMIN;
import static com.asrevo.cvhome.testsupport.security.Tokens.ROLE_STORE_MODERATOR;
import static com.asrevo.cvhome.testsupport.security.Tokens.STORE_1;
import static com.asrevo.cvhome.testsupport.security.Tokens.STORE_2;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Domain routing over HTTP: the two public answers the gateway relies on (may this host have a certificate; which
 * store is this host) and the private allocation of custom domains, with its tenant and role gates.
 */
@StorageIntegrationTest
@TestPropertySource(properties = {
        "com.asrevo.cvhome.pod-info.pod.name=pod-507f1f77",
        "com.asrevo.cvhome.pod-info.pod.domain=spg-507f1f77.gateway.com"})
class RouterControllerIntegrationTest {

    private static final String POD_DOMAIN = "spg-507f1f77.gateway.com";

    private static final String ROUTER = "/api/v1/router";

    private static final String PUBLIC = "public";

    private static final String PRIVATE = "private";

    private static final String ASK = path(ROUTER, PUBLIC, "ask-for-tls");

    private static final String LOOKUP = path(ROUTER, PUBLIC, "lookup-by-domain");

    private static final String ALLOCATES = path(ROUTER, PRIVATE, "allocates");

    private static final String ALLOCATE = path(ROUTER, PRIVATE, "allocate");

    private static final String REMOVE = path(ROUTER, PRIVATE, "remove");

    /** Seeded custom domain of {@link Tokens#STORE_1}. */
    private static final String STORE_1_DOMAIN = "org1-store1.asrevo.com";

    private static final String DOMAIN_QUERY = "domain=%s";

    private static final String CUSTOM_DOMAIN = "%s.example.com";

    private static final String STRANGER = "stranger.example.com";

    private static final String STORE_ID_HEADER = "Store-Id";

    private static final String ARABIC = "ar";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    private ApiClient api;

    private Tokens tokens;

    private String admin;

    @BeforeEach
    void setUp() {
        api = new ApiClient(port);
        tokens = new Tokens(signer);
        admin = tokens.staff(ROLE_STORE_ADMIN, STORE_1);
    }

    private static String domain(String base, String domain) {
        return query(base, String.format(DOMAIN_QUERY, domain));
    }

    private ResponseEntity<String> ask(String domain) {
        return api.get(domain(ASK, domain), null);
    }

    private ResponseEntity<String> allocate(HttpMethod method, String base, String store, String token, String domain) {
        return api.send(method, domain(scoped(base, store), domain), token, null);
    }

    @Test
    void thePodAndSeededDomainsGetCertificatesStrangersDoNot() {
        expect(ask(POD_DOMAIN), HttpStatus.OK);
        expect(ask(STORE_1_DOMAIN), HttpStatus.OK);
        // a store's sub-domain resolves under the pod domain
        expect(ask(String.format("org1-store1.%s", POD_DOMAIN)), HttpStatus.OK);
        expect(ask(STRANGER), HttpStatus.BAD_REQUEST);
    }

    @Test
    void lookupTellsTheGatewayWhichStoreAHostIs() {
        ResponseEntity<String> response = api.get(domain(LOOKUP, STORE_1_DOMAIN), null);

        expect(response, HttpStatus.OK);
        JsonNode headers = json(response);
        assertThat(headers.get(STORE_ID_HEADER).asString()).isEqualTo(STORE_1);
        assertThat(headers.get("Theme").asString()).isEqualTo("FASHION");
        assertThat(headers.get("Color-Theme").asString()).isEqualTo("DEFAULT");
        assertThat(headers.get("Default-Language").asString()).isEqualTo(ARABIC);
        assertThat(headers.get("Supported-Languages").asString()).contains(ARABIC).contains("en");

        ResponseEntity<String> unknown = api.get(domain(LOOKUP, STRANGER), null);
        expect(unknown, HttpStatus.OK);
        assertThat(json(unknown)).isEmpty();
    }

    @Test
    void customDomainLifecycleAllocateAskRemove() {
        String custom = String.format(CUSTOM_DOMAIN, slug("shop"));

        expect(allocate(HttpMethod.POST, ALLOCATE, STORE_1, admin, custom), HttpStatus.OK);
        ResponseEntity<String> allocated = api.get(scoped(ALLOCATES, STORE_1), admin);
        expect(allocated, HttpStatus.OK);
        assertThat(allocated.getBody()).contains(custom).contains(STORE_1_DOMAIN);
        expect(ask(custom), HttpStatus.OK);
        assertThat(json(api.get(domain(LOOKUP, custom), null)).get(STORE_ID_HEADER).asString()).isEqualTo(STORE_1);

        expect(allocate(HttpMethod.DELETE, REMOVE, STORE_1, admin, custom), HttpStatus.OK);
        assertThat(api.get(scoped(ALLOCATES, STORE_1), admin).getBody()).doesNotContain(custom);
        expect(ask(custom), HttpStatus.BAD_REQUEST);
    }

    @Test
    void anotherStoreCannotSeeOrChangeThisStoresDomains() {
        String other = tokens.staff(ROLE_STORE_ADMIN, STORE_2);
        String custom = String.format(CUSTOM_DOMAIN, slug("other"));

        expect(api.get(scoped(ALLOCATES, STORE_1), other), HttpStatus.FORBIDDEN);
        expect(allocate(HttpMethod.POST, ALLOCATE, STORE_1, other, custom), HttpStatus.FORBIDDEN);
        expect(allocate(HttpMethod.DELETE, REMOVE, STORE_1, other, STORE_1_DOMAIN), HttpStatus.FORBIDDEN);
        // and the other store's own list does not carry this store's domain
        assertThat(api.get(scoped(ALLOCATES, STORE_2), other).getBody()).doesNotContain(STORE_1_DOMAIN);
    }

    @Test
    void moderatorsAndAnonymousCallersAreRefused() {
        String moderator = tokens.staff(ROLE_STORE_MODERATOR, STORE_1);
        String custom = String.format(CUSTOM_DOMAIN, slug("mod"));

        expect(api.get(scoped(ALLOCATES, STORE_1), moderator), HttpStatus.FORBIDDEN);
        expect(allocate(HttpMethod.POST, ALLOCATE, STORE_1, moderator, custom), HttpStatus.FORBIDDEN);
        // no token at all: the resource server denies before any @PreAuthorize runs, and this API has no
        // authentication entry point of its own, so an anonymous caller is refused with 403 rather than 401.
        expect(api.get(scoped(ALLOCATES, STORE_1), null), HttpStatus.FORBIDDEN);
    }

}
