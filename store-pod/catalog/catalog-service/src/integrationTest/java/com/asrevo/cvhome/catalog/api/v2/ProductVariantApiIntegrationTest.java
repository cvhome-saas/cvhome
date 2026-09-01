package com.asrevo.cvhome.catalog.api.v2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.asrevo.cvhome.catalog.api.CatalogApiSupport;
import com.asrevo.cvhome.catalog.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.ADMIN;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.ID;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.MODERATOR;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.SKU;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.STORE_A;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.STORE_B;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.V2_PRIVATE;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.expect;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.json;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.path;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.scoped;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The atomic axes+set replace over HTTP, against the seeded fashion store (color/size vocabulary; product 3 is
 * a seeded simple product this test turns into a multi-variant one and back).
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class ProductVariantApiIntegrationTest {

    /** A seeded simple product of store A (Adidas track pants). */
    private static final long PRODUCT = 3L;

    private static final String ORIGINAL_SKU = "SKU-AD-CL-TPT03";

    private static final String PRODUCT_SEGMENT = "product";

    private static final String VARIANTS = "variants";

    private static final String OPTION_VALUES = "optionValues";

    private static final String COMBINATIONS = """
            {"options":["color","size"],
             "variants":[
               {"sku":"%s","sortOrder":0,"defaultVariant":true,"optionValueIds":[1,3]},
               {"sku":"SKU-AD-CL-TPT03-BL-L","sortOrder":1,"defaultVariant":false,"optionValueIds":[2,4]}]}"""
            .formatted(ORIGINAL_SKU);

    private static final String BACK_TO_SIMPLE = """
            {"options":[],"variants":[{"sku":"%s"}]}""".formatted(ORIGINAL_SKU);

    private static final String DEFAULT_VARIANT = "defaultVariant";

    /** The product's own sku, so exercising the null path leaves the seeded product exactly as it was. */
    private static final String NULL_OPTION_VALUE_IDS = """
            {"options":[],"variants":[{"sku":"%s","optionValueIds":null}]}""".formatted(ORIGINAL_SKU);

    private static final String MOVE_DEFAULT = """
            {"options":["color","size"],
             "variants":[
               {"id":%d,"sku":"%s","sortOrder":0,"defaultVariant":%b,"optionValueIds":[1,3]},
               {"id":%d,"sku":"SKU-AD-CL-TPT03-BL-L","sortOrder":1,"defaultVariant":%b,"optionValueIds":[2,4]}]}""";


    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    private CatalogApiSupport api;

    private String admin;

    @BeforeEach
    void setUp() {
        api = new CatalogApiSupport(port, signer);
        admin = api.token(ADMIN, STORE_A);
    }

    /** The same two persisted rows, with the default on the first or on the second. */
    private static String moveDefault(long firstId, long secondId, boolean onFirst) {
        return MOVE_DEFAULT.formatted(firstId, ORIGINAL_SKU, onFirst, secondId, !onFirst);
    }

    private ResponseEntity<String> replace(String store, String token, long productId, String body) {
        return api.send(HttpMethod.PUT,
                scoped(path(V2_PRIVATE, PRODUCT_SEGMENT, productId, VARIANTS), store), token, body);
    }

    private JsonNode list(long productId) {
        var response = api.get(scoped(path(V2_PRIVATE, PRODUCT_SEGMENT, productId, VARIANTS), STORE_A), admin);
        expect(response, HttpStatus.OK);
        return json(response);
    }

    @Test
    void replaceReadAndRestoreRoundTrip() {
        try {
            expect(replace(STORE_A, admin, PRODUCT, COMBINATIONS), HttpStatus.OK);

            JsonNode variants = list(PRODUCT);
            assertThat(variants).hasSize(2);
            assertThat(variants.get(0).get(SKU).asString()).isEqualTo(ORIGINAL_SKU);
            assertThat(variants.get(0).get(DEFAULT_VARIANT).asBoolean()).isTrue();
            assertThat(variants.get(0).get(OPTION_VALUES)).hasSize(2);
            assertThat(variants.get(0).get(OPTION_VALUES).get(0).get("optionCode").asString())
                    .isEqualTo("color");
            assertThat(variants.get(0).get(OPTION_VALUES).get(0).get("valueName").asString())
                    .isEqualTo("Red");
            long keptId = variants.get(0).get(ID).asLong();

            // the product page carries the axes and the combinations
            var page = api.get(scoped("/api/v2/product/name/adidas-tiro-track-pants", STORE_A, "en"), null);
            if (page.getStatusCode() == HttpStatus.OK) {
                JsonNode product = json(page);
                assertThat(product.get("options")).hasSize(2);
                assertThat(product.get(VARIANTS)).hasSize(2);
                assertThat(product.get("variantCount").asInt()).isEqualTo(2);
            }

            // an id-addressed edit keeps the row
            String edited = """
                    {"options":["color","size"],
                     "variants":[
                       {"id":%d,"sku":"%s","sortOrder":0,"defaultVariant":true,"optionValueIds":[1,3]},
                       {"sku":"SKU-AD-CL-TPT03-BL-M","sortOrder":1,"defaultVariant":false,"optionValueIds":[2,3]}]}"""
                    .formatted(keptId, ORIGINAL_SKU);
            expect(replace(STORE_A, admin, PRODUCT, edited), HttpStatus.OK);
            JsonNode after = list(PRODUCT);
            assertThat(after).hasSize(2);
            assertThat(after.get(0).get(ID).asLong()).isEqualTo(keptId);
        } finally {
            expect(replace(STORE_A, admin, PRODUCT, BACK_TO_SIMPLE), HttpStatus.OK);
        }
        JsonNode restored = list(PRODUCT);
        assertThat(restored).hasSize(1);
        assertThat(restored.get(0).get(SKU).asString()).isEqualTo(ORIGINAL_SKU);
        assertThat(restored.get(0).get(OPTION_VALUES)).isEmpty();
    }

    @Test
    void theDefaultCanMoveInEitherDirectionAcrossPersistedRows() {
        /*
         * Against the DB, because the hazard is the DB's: uk_product_variant_default is a partial unique
         * INDEX, and Postgres never defers those. Flipping both flags in one dirty-checking pass left the
         * two UPDATEs in persistence-context order, so promoting an EARLIER row emitted "set true" while
         * the old default was still true and the save died on a duplicate key. The mocked unit test cannot
         * see an index, and the round-trip above only ever added a non-default row — between them the 500
         * was invisible. Both directions are asserted here; only one of them ever failed.
         */
        try {
            expect(replace(STORE_A, admin, PRODUCT, COMBINATIONS), HttpStatus.OK);
            JsonNode seeded = list(PRODUCT);
            long firstId = seeded.get(0).get(ID).asLong();
            long secondId = seeded.get(1).get(ID).asLong();
            assertThat(seeded.get(0).get(DEFAULT_VARIANT).asBoolean()).isTrue();

            // forward: promote the second (later) row — the direction that always worked
            expect(replace(STORE_A, admin, PRODUCT, moveDefault(firstId, secondId, false)), HttpStatus.OK);
            assertThat(list(PRODUCT).get(1).get(DEFAULT_VARIANT).asBoolean()).isTrue();

            // backward: promote the first (earlier) row — the direction that 500'd
            expect(replace(STORE_A, admin, PRODUCT, moveDefault(firstId, secondId, true)), HttpStatus.OK);
            JsonNode after = list(PRODUCT);
            assertThat(after.get(0).get(DEFAULT_VARIANT).asBoolean()).isTrue();
            assertThat(after.get(1).get(DEFAULT_VARIANT).asBoolean()).isFalse();
        } finally {
            expect(replace(STORE_A, admin, PRODUCT, BACK_TO_SIMPLE), HttpStatus.OK);
        }
    }

    @Test
    void invalidSetsAreRefusedWithTypedErrors() {
        // a variant missing an axis
        expect(replace(STORE_A, admin, PRODUCT,
                        "{\"options\":[\"color\",\"size\"],\"variants\":[{\"sku\":\"HALF\",\"optionValueIds\":[1]}]}"),
                HttpStatus.BAD_REQUEST);
        // duplicate combination
        expect(replace(STORE_A, admin, PRODUCT, """
                {"options":["color"],"variants":[
                  {"sku":"DUP-A","optionValueIds":[1]},{"sku":"DUP-B","optionValueIds":[1]}]}"""),
                HttpStatus.CONFLICT);
        // a sku owned by another product
        expect(replace(STORE_A, admin, PRODUCT, """
                {"options":["color"],"variants":[{"sku":"SKU-NK-RUN-001","optionValueIds":[1]}]}"""),
                HttpStatus.CONFLICT);
        // the same axis declared twice — a matrix cannot vary by one option along two dimensions
        expect(replace(STORE_A, admin, PRODUCT, """
                {"options":["color","color"],"variants":[{"sku":"TWICE","optionValueIds":[1]}]}"""),
                HttpStatus.BAD_REQUEST);

        // an unknown option code
        expect(replace(STORE_A, admin, PRODUCT, """
                {"options":["material"],"variants":[{"sku":"MAT","optionValueIds":[1]}]}"""),
                HttpStatus.NOT_FOUND);
        /*
         * An explicit null where the field defaults to an empty list. Jackson overwrites the default, and the
         * no-axes branch read it without a null check — a client sending the field as null got a 500 rather
         * than being told what was wrong with the request.
         */
        expect(replace(STORE_A, admin, PRODUCT, NULL_OPTION_VALUE_IDS), HttpStatus.OK);
        // the seeded product is untouched by all of it
        assertThat(list(PRODUCT)).hasSize(1);
    }

    @Test
    void theGuardrailsRefuseAMatrixNoConsoleCouldDrawAnyway() {
        /*
         * 4 options / 100 variants per product, enforced server-side. The console caps its picker and its
         * generator, so these are only reachable through the API — which is exactly why they are tested here:
         * the limits protect the matrix UI, the facet queries and the PDP availability call.
         */
        try {
            // A fifth axis. The guardrail is checked BEFORE the codes are resolved, so this answers the
            // limit error even though three of the five codes do not exist in the store — the cheap check
            // runs first, which is the right order and worth pinning.
            expect(replace(STORE_A, admin, PRODUCT,
                            """
                            {"options":["color","size","material","fit","finish"],"variants":[]}"""),
                    HttpStatus.BAD_REQUEST);

            // 101 combinations of one real axis: past the variant cap, refused before anything is written.
            StringBuilder variants = new StringBuilder();
            for (int i = 0; i < 101; i++) {
                variants.append(i == 0 ? "" : ",")
                        .append("{\"sku\":\"SKU-CAP-%d\",\"sortOrder\":%d,\"optionValueIds\":[1]}"
                                .formatted(i, i));
            }
            expect(replace(STORE_A, admin, PRODUCT,
                            "{\"options\":[\"color\"],\"variants\":[%s]}".formatted(variants)),
                    HttpStatus.BAD_REQUEST);

            // Nothing landed: the product still sells by its one default variant.
            assertThat(list(PRODUCT)).hasSize(1);
        } finally {
            expect(replace(STORE_A, admin, PRODUCT, BACK_TO_SIMPLE), HttpStatus.OK);
        }
    }

    @Test
    void aProductThatDoesNotExistInThisStoreHasNoVariants() {
        expect(api.get(scoped(path(V2_PRIVATE, PRODUCT_SEGMENT, 999999L, VARIANTS), STORE_A), admin),
                HttpStatus.NOT_FOUND);
    }

    @Test
    void anotherStoreAndLesserRolesAreRejected() {
        String otherAdmin = api.token(ADMIN, STORE_B);
        expect(api.get(scoped(path(V2_PRIVATE, PRODUCT_SEGMENT, PRODUCT, VARIANTS), STORE_B), otherAdmin),
                HttpStatus.NOT_FOUND);
        expect(api.get(scoped(path(V2_PRIVATE, PRODUCT_SEGMENT, PRODUCT, VARIANTS), STORE_A), otherAdmin),
                HttpStatus.FORBIDDEN);
        expect(api.get(scoped(path(V2_PRIVATE, PRODUCT_SEGMENT, PRODUCT, VARIANTS), STORE_A),
                api.token(MODERATOR, STORE_A)), HttpStatus.FORBIDDEN);
        expect(api.get(scoped(path(V2_PRIVATE, PRODUCT_SEGMENT, PRODUCT, VARIANTS), STORE_A), null),
                HttpStatus.UNAUTHORIZED);
    }
}
