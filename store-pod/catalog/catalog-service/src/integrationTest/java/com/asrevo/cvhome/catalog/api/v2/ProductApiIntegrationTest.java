package com.asrevo.cvhome.catalog.api.v2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import com.asrevo.cvhome.catalog.api.CatalogApiSupport;
import com.asrevo.cvhome.catalog.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.testsupport.annotations.StorageIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.ADMIN;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.CODE;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.CONTENT;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.DESCRIPTION;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.DESCRIPTIONS;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.EXISTS;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.ID;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.MODERATOR;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.NAME;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.SKU;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.STORE_A;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.STORE_B;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.TOTAL_ELEMENTS;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.V1;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.V1_PRIVATE;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.V2;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.V2_PRIVATE;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.expect;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.json;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.path;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.query;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.scoped;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.slug;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The product surface end to end: the v2 definition writes, the storefront listing and product page, the small v1
 * writes (the inline switches, category membership, delete) and checkout's s2s read by sku.
 *
 * <p>
 * Catalog is a pure catalog since the inventory split, so nothing here asserts price or stock — they are the
 * inventory service's, keyed by the sku these endpoints answer.
 * </p>
 */
@StorageIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class ProductApiIntegrationTest {

    private static final String PRODUCT_SEGMENT = "product";

    private static final String SKU_FRAGMENT = "NK-RUN";

    private static final String CATEGORY_QUERY = "categoryIds=%d";

    private static final String PRODUCTS = path(V2, "products");

    private static final String BY_NAME = path(V2, PRODUCT_SEGMENT, "name");

    private static final String PRIVATE_PRODUCT_V2 = path(V2_PRIVATE, PRODUCT_SEGMENT);

    private static final String PRIVATE_PRODUCT_V1 = path(V1_PRIVATE, PRODUCT_SEGMENT);

    private static final String UNIQUE = path(PRIVATE_PRODUCT_V1, "unique");

    private static final String DETAILED = path(V1, "detailed-product");

    private static final String CATEGORY_SEGMENT = "category";

    private static final String CATEGORIES = "categories";

    private static final String VISIBLE = "visible";

    private static final String AVAILABLE = "available";

    private static final String MANUFACTURER = "manufacturer";

    private static final String TYPE = "type";

    private static final String IMAGES = "images";

    private static final String SPECIFICATIONS = "productSpecifications";

    private static final String CODE_QUERY = "code=%s";

    private static final String SKU_QUERY = "sku=%s";

    /** Seeded in store A: a Nike running shoe in the MEN_SHOES tree, brand NIKE, type SHOES. */
    private static final String SEEDED_SKU = "SKU-NK-RUN-001";

    private static final String SEEDED_SLUG = "nike-zoomx-invincible-run-3";

    private static final String NIKE = "NIKE";

    private static final String SHOES = "SHOES";

    /** Seeded category MEN, the root of the men's subtree. */
    private static final long MEN_ID = 1L;

    /** Seeded category MEN_SHOES, a child of MEN. */
    private static final long MEN_SHOES_ID = 7L;

    private static final String PRODUCT_BODY = """
            {"sku":"%s","visible":true,"shipeable":true,"virtual":false,"sortOrder":4,
             "productSpecifications":{"height":10.0,"width":20.0,"length":30.0,"weight":1.5},
             "type":%s,"manufacturer":%s,"categories":[%s],
             "descriptions":[{"language":"en","name":"%s","title":"T","description":"<p>d</p>",
                              "friendlyUrl":"%s","keyWords":"k","metaDescription":"m","highlights":"h"},
                             {"language":"ar","name":"منتج","friendlyUrl":"%s-ar"}]}""";

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

    // ------------------------------------------------------------------------------------------------- helpers

    private static String quoted(String value) {
        return value == null ? "null" : String.format("\"%s\"", value);
    }

    private static String body(String sku, String type, String manufacturer, String categories) {
        String friendly = sku.toLowerCase();
        return String.format(PRODUCT_BODY, sku, quoted(type), quoted(manufacturer), categories, sku, friendly,
                friendly);
    }

    private long create(String sku, String type, String manufacturer, String categories) {
        var created = api.send(HttpMethod.POST, scoped(PRIVATE_PRODUCT_V2, STORE_A), admin,
                body(sku, type, manufacturer, categories));
        expect(created, HttpStatus.CREATED);
        return json(created).get(ID).asLong();
    }

    private JsonNode definition(long id) {
        var response = api.get(scoped(path(PRIVATE_PRODUCT_V2, id), STORE_A), admin);
        expect(response, HttpStatus.OK);
        return json(response);
    }

    private static String newSku() {
        return slug("SKU-IT").toUpperCase();
    }

    // -------------------------------------------------------------------------------------------- the definition

    @Test
    void productLifecycleCreateReadUpdateDelete() {
        String sku = newSku();
        long id = create(sku, SHOES, NIKE, String.format("{\"id\":%d}", MEN_SHOES_ID));

        JsonNode created = definition(id);
        assertThat(created.get(SKU).asString()).isEqualTo(sku);
        // identifier is the same value under the name the console's form was built against
        assertThat(created.get("identifier").asString()).isEqualTo(sku);
        assertThat(created.get(VISIBLE).asBoolean()).isTrue();
        assertThat(created.get(MANUFACTURER).get(CODE).asString()).isEqualTo(NIKE);
        assertThat(created.get(TYPE).get(CODE).asString()).isEqualTo(SHOES);
        assertThat(created.get(CATEGORIES)).hasSize(1);
        assertThat(created.get(DESCRIPTIONS)).hasSize(2);
        // the store's units come from the merchant record, not from the product row
        assertThat(created.get(SPECIFICATIONS).get("dimensionUnitOfMeasure").asString()).isEqualTo("cm");
        assertThat(created.get(SPECIFICATIONS).get("weightUnitOfMeasure").asString()).isEqualTo("kg");

        assertThat(json(api.get(scoped(query(UNIQUE, String.format(CODE_QUERY, sku)), STORE_A), admin))
                .get(EXISTS).asBoolean()).isTrue();

        // an update merges descriptions by language and may drop the relations entirely
        long descriptionId = created.get(DESCRIPTIONS).get(0).get(ID).asLong();
        expect(api.send(HttpMethod.PUT, scoped(path(PRIVATE_PRODUCT_V2, id), STORE_A), admin,
                body(sku, null, null, "")), HttpStatus.OK);
        JsonNode updated = definition(id);
        assertThat(updated.get(MANUFACTURER).isNull()).isTrue();
        assertThat(updated.get(TYPE).isNull()).isTrue();
        // an empty categories list leaves the existing membership alone — the category endpoints own that
        assertThat(updated.get(CATEGORIES)).hasSize(1);
        assertThat(updated.get(DESCRIPTIONS).valueStream().anyMatch(d -> d.get(ID).asLong() == descriptionId))
                .isTrue();

        expect(api.send(HttpMethod.DELETE, path(scoped(path(PRIVATE_PRODUCT_V1, id), STORE_A)), admin, null),
                HttpStatus.OK);
        expect(api.get(scoped(path(PRIVATE_PRODUCT_V2, id), STORE_A), admin), HttpStatus.NOT_FOUND);
    }

    @Test
    void unresolvableReferencesAndBadSkusAreRefused() {
        String sku = newSku();
        var badBrand = api.send(HttpMethod.POST, scoped(PRIVATE_PRODUCT_V2, STORE_A), admin,
                body(sku, null, "NO_SUCH_BRAND", ""));
        expect(badBrand, HttpStatus.BAD_REQUEST);
        assertThat(json(badBrand).get(CODE).asString()).isEqualTo("CATALOG.MANUFACTURER.REFERENCE_UNRESOLVABLE");

        var badType = api.send(HttpMethod.POST, scoped(PRIVATE_PRODUCT_V2, STORE_A), admin,
                body(sku, "NO_SUCH_TYPE", null, ""));
        expect(badType, HttpStatus.BAD_REQUEST);
        assertThat(json(badType).get(CODE).asString()).isEqualTo("CATALOG.PRODUCT_TYPE.REFERENCE_UNRESOLVABLE");

        var badCategory = api.send(HttpMethod.POST, scoped(PRIVATE_PRODUCT_V2, STORE_A), admin,
                body(sku, null, null, "{\"code\":\"NO_SUCH_CATEGORY\"}"));
        expect(badCategory, HttpStatus.BAD_REQUEST);
        assertThat(json(badCategory).get(CODE).asString()).isEqualTo("CATALOG.CATEGORY.REFERENCE_UNRESOLVABLE");

        // the sku pattern is part of the contract: a slash would break every url built from it
        expect(api.send(HttpMethod.POST, scoped(PRIVATE_PRODUCT_V2, STORE_A), admin,
                body("not/a/sku", null, null, "")), HttpStatus.BAD_REQUEST);

        expect(api.get(scoped(path(PRIVATE_PRODUCT_V2, 999999L), STORE_A), admin), HttpStatus.NOT_FOUND);
        expect(api.send(HttpMethod.PUT, scoped(path(PRIVATE_PRODUCT_V2, 999999L), STORE_A), admin,
                body(newSku(), null, null, "")), HttpStatus.NOT_FOUND);
    }

    // ------------------------------------------------------------------------------------------- storefront reads

    @Test
    void theListingFiltersBySkuAvailabilityCategoryAndBrand() {
        JsonNode all = json(api.get(scoped(PRODUCTS, STORE_A), null));
        assertThat(all.get(TOTAL_ELEMENTS).asLong()).isGreaterThan(1);

        JsonNode bySku = json(api.get(scoped(query(PRODUCTS, String.format(SKU_QUERY, SKU_FRAGMENT)), STORE_A), null));
        assertThat(bySku.get(CONTENT)).isNotEmpty();
        assertThat(bySku.get(CONTENT).valueStream().allMatch(p -> p.get(SKU).asString().contains(SKU_FRAGMENT))).isTrue();

        // one category widens to its whole subtree: MEN_SHOES hangs under MEN, so asking for MEN finds it
        JsonNode inMen = json(api.get(scoped(query(PRODUCTS, String.format(CATEGORY_QUERY, MEN_ID)), STORE_A),
                null));
        JsonNode inShoes = json(api.get(scoped(query(PRODUCTS, String.format(CATEGORY_QUERY, MEN_SHOES_ID)),
                STORE_A), null));
        assertThat(inMen.get(TOTAL_ELEMENTS).asLong()).isGreaterThanOrEqualTo(inShoes.get(TOTAL_ELEMENTS).asLong());
        assertThat(inShoes.get(CONTENT)).isNotEmpty();

        long brand = json(api.get(scoped(path(BY_NAME, SEEDED_SLUG), STORE_A), null)).get(MANUFACTURER).get(ID)
                .asLong();
        JsonNode byBrand = json(api.get(scoped(query(PRODUCTS, String.format("manufacturerId=%d", brand)), STORE_A),
                null));
        assertThat(byBrand.get(CONTENT)).isNotEmpty();

        // a hidden product drops out of the available=true listing without being deleted
        String sku = newSku();
        long id = create(sku, null, null, "");
        String filtered = query(PRODUCTS, String.format("%s&available=true", String.format(SKU_QUERY, sku)));
        assertThat(json(api.get(scoped(filtered, STORE_A), null)).get(TOTAL_ELEMENTS).asLong()).isEqualTo(1);
        expect(api.send(HttpMethod.PATCH, scoped(path(PRIVATE_PRODUCT_V1, id), STORE_A), admin,
                "{\"available\":false,\"productShipeable\":false}"), HttpStatus.OK);
        assertThat(json(api.get(scoped(filtered, STORE_A), null)).get(TOTAL_ELEMENTS).asLong()).isZero();
        assertThat(definition(id).get(VISIBLE).asBoolean()).isFalse();

        expect(api.send(HttpMethod.DELETE, scoped(path(PRIVATE_PRODUCT_V1, id), STORE_A), admin, null),
                HttpStatus.OK);
    }

    @Test
    void theProductPageAnswersBySlugAndOnlyWhenVisible() {
        var page = api.get(scoped(path(BY_NAME, SEEDED_SLUG), STORE_A), null);
        expect(page, HttpStatus.OK);
        JsonNode product = json(page);
        assertThat(product.get(SKU).asString()).isEqualTo(SEEDED_SKU);
        assertThat(product.get(DESCRIPTION).get(NAME).asString()).isNotEmpty();
        assertThat(product.get(AVAILABLE).asBoolean()).isTrue();
        assertThat(product.get(IMAGES)).isNotEmpty();
        // the listing shape carries brand, type and categories; the minimal one does not
        assertThat(product.get(MANUFACTURER).get(CODE).asString()).isEqualTo(NIKE);
        assertThat(product.get(CATEGORIES)).isNotEmpty();

        // the same slug in another store is nothing
        expect(api.get(scoped(path(BY_NAME, SEEDED_SLUG), STORE_B), null), HttpStatus.NOT_FOUND);

        // a hidden product has no product page at all
        String sku = newSku();
        long id = create(sku, null, null, "");
        expect(api.get(scoped(path(BY_NAME, sku.toLowerCase()), STORE_A), null), HttpStatus.OK);
        expect(api.send(HttpMethod.PATCH, scoped(path(PRIVATE_PRODUCT_V1, id), STORE_A), admin,
                "{\"available\":false,\"productShipeable\":true}"), HttpStatus.OK);
        expect(api.get(scoped(path(BY_NAME, sku.toLowerCase()), STORE_A), null), HttpStatus.NOT_FOUND);

        expect(api.send(HttpMethod.DELETE, scoped(path(PRIVATE_PRODUCT_V1, id), STORE_A), admin, null),
                HttpStatus.OK);
    }

    @Test
    void checkoutReadsTheProductBehindACartLineBySku() {
        String s2s = api.token(ADMIN, STORE_A);
        var response = api.get(scoped(query(DETAILED, String.format(SKU_QUERY, SEEDED_SKU)), STORE_A), s2s);
        expect(response, HttpStatus.OK);
        JsonNode minimal = json(response);
        assertThat(minimal.get(SKU).asString()).isEqualTo(SEEDED_SKU);
        assertThat(minimal.get(DESCRIPTION).get(NAME).asString()).isNotEmpty();
        // the minimal shape stops at the box and the images — no brand, no categories
        assertThat(minimal.has(CATEGORIES)).isFalse();

        // an unknown sku keeps the catalog's own 404 code on the way out of the s2s carrier
        var missing = api.get(scoped(query(DETAILED, String.format(SKU_QUERY, slug("nope"))), STORE_A), s2s);
        expect(missing, HttpStatus.NOT_FOUND);
        assertThat(json(missing).get(CODE).asString()).isEqualTo("CATALOG.PRODUCT.NOT_FOUND");
    }

    // ----------------------------------------------------------------------------------------- category membership

    @Test
    void categoryMembershipIsAddedOnceAndRemoved() {
        String sku = newSku();
        long id = create(sku, null, null, "");
        String membership = scoped(path(PRIVATE_PRODUCT_V1, id, CATEGORY_SEGMENT, MEN_ID), STORE_A);

        expect(api.send(HttpMethod.POST, membership, admin, null), HttpStatus.CREATED);
        assertThat(definition(id).get(CATEGORIES)).hasSize(1);

        // attaching the same category twice is a conflict, not a silent no-op
        var again = api.send(HttpMethod.POST, membership, admin, null);
        expect(again, HttpStatus.CONFLICT);
        assertThat(json(again).get(CODE).asString()).isEqualTo("CATALOG.CATEGORY.ALREADY_ATTACHED");

        expect(api.send(HttpMethod.DELETE, membership, admin, null), HttpStatus.OK);
        assertThat(definition(id).get(CATEGORIES)).isEmpty();

        // an unknown category is a 404 on both directions
        String unknown = scoped(path(PRIVATE_PRODUCT_V1, id, CATEGORY_SEGMENT, 999999L), STORE_A);
        expect(api.send(HttpMethod.POST, unknown, admin, null), HttpStatus.NOT_FOUND);
        expect(api.send(HttpMethod.DELETE, unknown, admin, null), HttpStatus.NOT_FOUND);

        expect(api.send(HttpMethod.DELETE, scoped(path(PRIVATE_PRODUCT_V1, id), STORE_A), admin, null),
                HttpStatus.OK);
    }

    // ------------------------------------------------------------------------------------ tenancy + permissions

    @Test
    void anotherStoreCannotSeeOrTouchTheProduct() {
        String sku = newSku();
        long id = create(sku, null, null, "");
        String other = api.token(ADMIN, STORE_B);

        expect(api.get(scoped(path(PRIVATE_PRODUCT_V2, id), STORE_A), other), HttpStatus.FORBIDDEN);
        expect(api.get(scoped(path(PRIVATE_PRODUCT_V2, id), STORE_B), other), HttpStatus.NOT_FOUND);
        expect(api.send(HttpMethod.PUT, scoped(path(PRIVATE_PRODUCT_V2, id), STORE_B), other,
                body(sku, null, null, "")), HttpStatus.NOT_FOUND);
        expect(api.send(HttpMethod.DELETE, scoped(path(PRIVATE_PRODUCT_V1, id), STORE_B), other, null),
                HttpStatus.NOT_FOUND);
        // the sku exists in store A only
        assertThat(json(api.get(scoped(query(UNIQUE, String.format(CODE_QUERY, sku)), STORE_B), other))
                .get(EXISTS).asBoolean()).isFalse();

        expect(api.send(HttpMethod.DELETE, scoped(path(PRIVATE_PRODUCT_V1, id), STORE_A), admin, null),
                HttpStatus.OK);
    }

    @Test
    void aModeratorAndAnAnonymousCallerAreRefusedTheConsole() {
        String moderator = api.token(MODERATOR, STORE_A);
        expect(api.get(scoped(path(PRIVATE_PRODUCT_V2, 1L), STORE_A), moderator), HttpStatus.FORBIDDEN);
        expect(api.send(HttpMethod.POST, scoped(PRIVATE_PRODUCT_V2, STORE_A), moderator,
                body(newSku(), null, null, "")), HttpStatus.FORBIDDEN);
        expect(api.send(HttpMethod.DELETE, scoped(path(PRIVATE_PRODUCT_V1, 1L), STORE_A), moderator, null),
                HttpStatus.FORBIDDEN);
        expect(api.get(scoped(path(PRIVATE_PRODUCT_V2, 1L), STORE_A), null), HttpStatus.UNAUTHORIZED);
        // the storefront listing stays open to everyone
        expect(api.get(scoped(PRODUCTS, STORE_A), null), HttpStatus.OK);
    }

}
