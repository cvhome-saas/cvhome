package com.asrevo.cvhome.content.api.v1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

import com.asrevo.cvhome.testsupport.annotations.StorageIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.ID;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.PRIVATE;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.ROLE_STORE_ADMIN;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.expect;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.json;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.path;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.query;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.scoped;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The media library as catalog sees it: resolving assets by id, and stating which of them a product uses.
 *
 * <p>
 * Integration tests run without pod-info, so the "same store pod" check has no pod to compare against and would
 * refuse every service token. The pod is configured here rather than globally because these are the only tests
 * that exercise a peer service calling in.
 * </p>
 */
@StorageIntegrationTest
@TestPropertySource(properties = {
        "com.asrevo.cvhome.pod-info.pod.id=507f1f77bcf86cd799439011",
        "com.asrevo.cvhome.pod-info.pod.name=pod-507f1f77",
        "com.asrevo.cvhome.pod-info.pod.domain=spg-507f1f77.gateway.com",
        "com.asrevo.cvhome.pod-info.pod.endpoint.type=EXTERNAL",
        "com.asrevo.cvhome.pod-info.pod.endpoint.endpoint=http://spg-507f1f77.gateway.com"})
class ExternalMediaApiIntegrationTest {

    private static final String POD_NAME = "pod-507f1f77";

    /** A seeded store. */
    private static final String STORE_A = "65f023632bc26470c104b75f";

    /** Another seeded store — how tenant isolation is proven rather than assumed. */
    private static final String STORE_B = "65f023632bc46470c104b75f";

    private static final String MEDIA_SEGMENT = "media";

    private static final String USAGE_SEGMENT = "usage";

    private static final String MEDIA = path(PRIVATE, MEDIA_SEGMENT);

    private static final String EXTERNAL_MEDIA = path(PRIVATE, "external", MEDIA_SEGMENT);

    private static final String USAGE = path(EXTERNAL_MEDIA, USAGE_SEGMENT);

    private static final String PRODUCT_TITLE = "Blue sneakers";

    private static final String IMAGE_FIELD = "image[0]";

    private static final String USAGE_BODY = """
            {"ownerKind":"PRODUCT","ownerRef":"42","ownerTitle":"%s",
             "refs":[{"field":"%s","assetId":%d}]}""";

    @LocalServerPort
    private int port;

    @org.springframework.beans.factory.annotation.Autowired
    private TestJwtSigner signer;

    private ApiTestSupport api;

    private String service;

    private String admin;

    @BeforeEach
    void setUp() {
        api = new ApiTestSupport(port, signer);
        service = api.s2s(POD_NAME);
        admin = api.token(ROLE_STORE_ADMIN, STORE_A);
    }

    private long upload(String store, String filename) {
        var uploaded = api.upload(scoped(MEDIA, store), api.token(ROLE_STORE_ADMIN, store), filename, png());
        expect(uploaded, HttpStatus.CREATED);
        return json(uploaded).get(0).get(ID).asLong();
    }

    /** A 1x1 PNG — enough for the probe to read dimensions. */
    private static byte[] png() {
        return java.util.Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");
    }

    @Test
    void resolveReturnsOnlyThisStoresAssets() {
        long mine = upload(STORE_A, "mine.png");
        long theirs = upload(STORE_B, "theirs.png");

        JsonNode out = json(api.get(query(scoped(EXTERNAL_MEDIA, STORE_A),
                String.format("ids=%d,%d", mine, theirs)), service));

        // The other store's id is absent rather than an error, which is what lets catalog use this call as its
        // ownership check before it saves a product image.
        assertThat(out.size()).isEqualTo(1);
        assertThat(out.get(0).get(ID).asLong()).isEqualTo(mine);
    }

    @Test
    void usageIsReplacedWholesaleAndProtectsTheAssetFromDeletion() {
        long asset = upload(STORE_A, "product.png");

        expect(api.send(HttpMethod.PUT, scoped(USAGE, STORE_A), service,
                String.format(USAGE_BODY, PRODUCT_TITLE, IMAGE_FIELD, asset)), HttpStatus.OK);

        JsonNode usage = json(api.get(scoped(path(MEDIA, asset, USAGE_SEGMENT), STORE_A), admin));
        assertThat(usage.size()).isEqualTo(1);
        assertThat(usage.get(0).get("ownerKind").asString()).isEqualTo("PRODUCT");
        assertThat(usage.get(0).get("ownerRef").asString()).isEqualTo("42");
        // The title came from the caller: content never asks catalog what a product is called.
        assertThat(usage.get(0).get("itemTitle").asString()).isEqualTo(PRODUCT_TITLE);

        var refused = api.send(HttpMethod.DELETE, scoped(path(MEDIA, asset), STORE_A), admin, null);
        expect(refused, HttpStatus.CONFLICT);
        assertThat(refused.getBody()).contains("MEDIA.REFERENCED");

        // Stating the set again changes nothing — a retry has to be harmless, because catalog retries.
        expect(api.send(HttpMethod.PUT, scoped(USAGE, STORE_A), service,
                String.format(USAGE_BODY, PRODUCT_TITLE, IMAGE_FIELD, asset)), HttpStatus.OK);
        assertThat(json(api.get(scoped(path(MEDIA, asset, USAGE_SEGMENT), STORE_A), admin)).size()).isEqualTo(1);

        // Releasing is the same call with nothing in it; there is no separate delete on purpose.
        expect(api.send(HttpMethod.PUT, scoped(USAGE, STORE_A), service,
                """
                {"ownerKind":"PRODUCT","ownerRef":"42","ownerTitle":"Blue sneakers","refs":[]}"""), HttpStatus.OK);
        expect(api.send(HttpMethod.DELETE, scoped(path(MEDIA, asset), STORE_A), admin, null), HttpStatus.NO_CONTENT);
    }

    /**
     * The usage index is written by peer services, not by sellers. A store admin holds CONTENT.* and would pass
     * the ordinary manage check, so this proves the separate token is actually doing something.
     */
    @Test
    void aStoreAdminCannotWriteTheUsageIndex() {
        long asset = upload(STORE_A, "guarded.png");

        expect(api.send(HttpMethod.PUT, scoped(USAGE, STORE_A), admin,
                String.format(USAGE_BODY, PRODUCT_TITLE, IMAGE_FIELD, asset)), HttpStatus.FORBIDDEN);
    }

    @Test
    void aServiceTokenForAnotherPodIsRefused() {
        expect(api.get(query(scoped(EXTERNAL_MEDIA, STORE_A), "ids=1"), api.s2s("pod-somewhere-else")),
                HttpStatus.FORBIDDEN);
    }

}
